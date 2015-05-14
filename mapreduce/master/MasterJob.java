package mapreduce.master;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mapreduce.Mapper;
import mapreduce.Utils;

public class MasterJob<K extends Serializable, 
					   IV extends Serializable,
					   OV extends Serializable> {

	protected Master master;
	protected Mapper<K, IV, OV> job;
	protected int jobID;
	protected int keysSent = 0;  // keeps track of number of workers who have sent keys
	protected int shuffled = 0;  // keeps track of the workers who finished shuffle & sort
	protected int finished = 0;  // keeps track of the workers who are completed with reduce
	protected Map<K, OV> results;
	protected Map<K, Integer> keyCounts; 
	//Map b/w key and list of Worker Ids it came from
	protected Map<K, List<Integer>> keyToWorkers; 
	// Map b/w WorkerId (Integer) and List of Transfer Messages for this workerId 
	// i.e List<<Key, AddressOfWorkerPeer>>
	protected Map<Integer, List<Object[]>> workerToKeyMessages; 
	protected List<WorkerConnection> jobWorkers;
	protected List<String> files;
	protected Object queueLock = new Object(); 
	protected static Object printLock = new Object();
	
	public MasterJob(int jobID, Mapper<K, IV, OV> mr, Master master, List<String> files, 
			Collection<WorkerConnection> currentCluster) {
		
		System.out.println("Here in  Master.MasterJob.Constructor\nLength of files: "+files.size());
		this.jobID = jobID;
		this.master = master;
		this.job = mr;
		this.files = files;
		keyCounts = new ConcurrentHashMap<>();
		keyToWorkers = new ConcurrentHashMap<>();
		workerToKeyMessages = new ConcurrentHashMap<>();
		results = new ConcurrentHashMap<>();
		jobWorkers = new ArrayList<>(currentCluster);
	}
	
    /**
     * Asynchronously publish a byte message to all workers registered on this job
     * 
     * @param message byte message to be sent to all worker nodes
     */
	protected void writeAllWorkers(final byte message){
    	synchronized(queueLock) {
	    	for (final WorkerConnection wc : jobWorkers)
	    		Utils.writeCommand(wc.out, message, jobID);				
    	}
    }

    protected synchronized void remove(int workerID) {
		// TODO remove a worker while a job is in progress
	}
	
	protected synchronized void receiveKeyComplete() {
		if (++keysSent == jobWorkers.size()) 
			coordinateKeysOnWorkers();
	}

	protected synchronized void receiveKeyShuffle() {
		if (++shuffled == jobWorkers.size())
			writeAllWorkers(Utils.M2W_BEGIN_REDUCE);
	}
	
	protected synchronized void receiveJobDone() {
		if (++finished == jobWorkers.size())
			printResults();
	}
		
	//////////////////////////////////////////////////////////
	//
	// This follows map at the workers to coordinate which 
	// worker will work on what keys
	//
	/////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	protected synchronized void receiveWorkerKey(InputStream in, int id) {
		try {
			ObjectInputStream objInStream = new ObjectInputStream(in);
			Object[] o = (Object[]) objInStream.readObject();		
			K key = (K) o[0];
			Integer size = (Integer) o[1];
			aggregateKeyCounts(key, size); 
			storeKeyToWorker(key, id);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected void aggregateKeyCounts(K key, int count) {
		if (keyCounts.containsKey(key)) 
			keyCounts.put(key, keyCounts.get(key)+count);
		else
			keyCounts.put(key, count);
	}
	
	protected void storeKeyToWorker(K key, int workerID) {
		if(keyToWorkers.containsKey(key))
			keyToWorkers.get(key).add(workerID); // append worker ID its corresponding key mapping
		else {
			List<Integer> l = new ArrayList<>();
			l.add(workerID);
			keyToWorkers.put(key, l);
		}
	}
	
	protected void addTransferMessage(int workerID, Object[] transferMsg) {
		if (workerToKeyMessages.containsKey(workerID)) 
			workerToKeyMessages.get(workerID).add(transferMsg);
		else {
			List<Object[]> messages = new ArrayList<Object[]>();
			messages.add(transferMsg);
			workerToKeyMessages.put(workerID, messages);
		}
	}
	
	protected void coordinateKeysOnWorkers(){

		int wIdx = 0;
		
		// use this array to count out the messages intended for each worker
		int maxID = findMaxWorker();
		int[] counts = new int[maxID+1];
		for(int i = 0; i < counts.length; i++)
			counts[i] = 0;

		for (K key : keyToWorkers.keySet()) {
			// TODO use a heap for load balancing so worker with most keys gets his largest key to analyze, etc 
			// andoid metrics for assigning jobs
			WorkerConnection receiver = jobWorkers.get(wIdx);
			// message contains key, ipaddress and port to send 
			Object[] transferMessage = new Object[] { key,  
					receiver.clientSocket.getInetAddress().getHostAddress(),
					receiver.workerPort }; 
			for (Integer workerID : keyToWorkers.get(key))
				if (receiver.id != workerID) {  // this key will be transferred out of the worker
					addTransferMessage(workerID, transferMessage);
					// need to count how many transfers this worker can expect
					counts[receiver.id]++;
				}
			// for now go around in a circle assigning keys
			if (++wIdx == jobWorkers.size())
				wIdx = 0;
		} //workerToKeyMessages is now populated
		
		// if there were less keys than workers we need to make blank messages 
		// for the workers assigned zero keys so they will not hang
		for (WorkerConnection wc : jobWorkers)
			if (!workerToKeyMessages.containsKey(wc.id)) 
				workerToKeyMessages.put(wc.id, new ArrayList<Object[]>());

		// notify each worker of the number of their assigned keys
		// and where to send keys not assigned to them
		for (Map.Entry<Integer, List<Object[]>> entry : workerToKeyMessages.entrySet()) {
			WorkerConnection wc = master.getWorker(entry.getKey());
			Utils.writeCommand(wc.out, Utils.M2W_COORD_KEYS, jobID);
			Utils.writeInt(wc.out, counts[wc.id]);  // tell worker how many messages to expect
			Utils.writeObject(wc.out, entry.getValue());  // tell worker where to send his other keys
		}
	}
	
	private int findMaxWorker() {
		int max = 0;
		for( WorkerConnection wc : jobWorkers)
			if (wc.id > max)
				max = wc.id;
		return max;
	}
	
	//////////////////////////////////////////////////////////
	//
	// This follows reduce at the workers
	//
	/////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	public void receiveWorkerResults(InputStream in) {
		try {
			ObjectInputStream objInStream = new ObjectInputStream(in);
			Object[] o = (Object[]) objInStream.readObject();		
			K k = (K) o[0];
			OV v = (OV) o[1];
			results.put(k, v);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void printResults() {
		synchronized(printLock) {
			System.out.println("***Final Results For Job " + jobID + "***");
			for (K key: results.keySet()) {
				System.out.println("Key: " + key + " Value: " + results.get(key));
			}
			System.out.print("> ");
		}
		master.jobComplete(this.jobID);
	}
}

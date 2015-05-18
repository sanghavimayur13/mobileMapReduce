package LoadTracker;

import java.util.*;
import java.util.Arrays;

class Load{
	int id;
	long load;
	
	public Load(int _id){
		id = _id;
	}
	
	public void addLoad(long _load){
		load += _load;
	}
	
	public long getLoad(){
		return load;
	}
}

public class LoadTracker {
	
	Load[] nodes;
	public LoadTracker(){
		
	}
	
	public LoadTracker(int _nodes){
		nodes = new Load[_nodes];
		for(int i=0;i<_nodes;i++){
			nodes[i] = new Load(i);
		}
	}
	
	public void assignLoad(int id, long size){
		if(id<nodes.length){
			nodes[id].addLoad(size);
		}
	}
	
	public int[][] getResults(){
		Arrays.sort(nodes,new Comparator<Load>(){
			public int compare(Load a, Load b){
				if(a.load > b.load){
					return -1;
				}
				else 
					return 1;
			}
		});
		long total = 0;
		for(int i=0;i<nodes.length;i++){
			total += nodes[i].getLoad();
		}
		int[][] result = new int[nodes.length][2]; //id, ratio
		for(int i=0;i<nodes.length;i++){
			result[i][0] = nodes[i].id;
			result[i][1] = (int)(100 * nodes[i].getLoad() / total);
		}
		return result;
	}
}

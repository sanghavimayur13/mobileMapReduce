import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

public class MRTest4 extends mapreduce.Mapper<Integer, Integer, Integer> {

	public MRTest4() {
		System.out.println("MR4 Constructor");
	}

	public HashMap<Integer, Integer> map(File resource) {
		try{
			FileReader fin = new FileReader(resource);
			BufferedReader br = new BufferedReader(fin);
			int length = Integer.parseInt(br.readLine());
			//HashMap<Integer, Integer> hmap = new HashMap();
			for(int i=0;i<length;i++){
				String line = br.readLine();
				String[] fields = line.split(" ");
				System.out.println(fields.length + line);
				if(fields.length>8){
					//USA - 236
					//regionID, eventType
					if(fields[3].equals("236") && !fields[8].equals("236")) {
						if(!fields[8].equals("_")){
							emit(Integer.parseInt(fields[8]),1);
						}
					}
					else if(!fields[3].equals("236") && fields[8].equals("236")){
						if(!fields[3].equals("_")){
							emit(Integer.parseInt(fields[3]),1);
						}
					}
				}
			}
			fin.close();
			//emit(5, 1);
			//emit(3, 5);
			//emit(5, 1);
			return null;
		}catch (Exception e){
			System.out.println("Error in map(): "+resource.getName()+" : "+e);
			e.printStackTrace();
			return null;
		}
		
	}

	public Integer reduce(Integer key, List<Integer> listOfValues) {
		return listOfValues.size();
	}

}

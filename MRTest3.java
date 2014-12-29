import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

public class MRTest3 extends mapreduce.Mapper<Integer, Integer, Integer> {

	public MRTest3() {
		System.out.println("MRTest3 Constructor");
	}

	public HashMap<Integer, Integer> map(File resource) {
		try{
			System.out.println("map called");
			FileReader fin = new FileReader(resource);
			BufferedReader br = new BufferedReader(fin);
			int length = Integer.parseInt(br.readLine());
			System.out.println("Length: "+length);
			//HashMap<Integer, Integer> hmap = new HashMap();
			for(int i=0;i<length;i++){
				String line = br.readLine();
				String[] fields = line.split(" ");
				System.out.println(fields.length + line);
				//regionID, eventType
				if(fields.length > 15){
					if(fields[15].equalsIgnoreCase("t")){
						if(!fields[14].equals("_")){
							emit(Integer.parseInt(fields[14]),1);
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
			System.out.println("Error in map(): "+resource.getName()+" : "+e.toString());
			e.printStackTrace();
			return null;
		}
		
	}

	public Integer reduce(Integer key, List<Integer> listOfValues) {
		return listOfValues.size();
	}
}

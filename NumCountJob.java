import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class NumCountJob extends mapreduce.Mapper<Integer, Integer, Integer>{
	
	public NumCountJob(){
		System.out.println("NumCountJob Constructor: Job Created");
	}
	
	public HashMap<Integer, Integer> map(File resource){
		try{
			System.out.println("Started here..");
				BufferedReader br = new BufferedReader(new FileReader(resource));
				String str = null;
				while((str = br.readLine())!=null){
					//arr[Integer.parseInt(str)]++;
					emit(Integer.parseInt(str),1);
				}
				br.close();
			}catch(Exception e){
				System.out.println(e);
			}
		return null;
	}
	
	public Integer reduce(Integer key, List<Integer> listOfValues) {
		int count = 0;
		for (Integer val : listOfValues)
			count += val;
		return count;
	}
}
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class WordCountJob extends mapreduce.Mapper<String, Integer, Integer>{
	
	public WordCountJob(){
		System.out.println("WordCountJob Constructor: Job Created");
	}
	
	public HashMap<String, Integer> map(File resource){
		try{
			System.out.println("Started here..");
				BufferedReader br = new BufferedReader(new FileReader(resource));
				String str = null;
				while((str = br.readLine())!=null){
					//arr[Integer.parseInt(str)]++;
					String[] words = str.split("");
					for(String s : words){
						emit(s,1);
					}
				}
				br.close();
			}catch(Exception e){
				System.out.println(e);
			}
		return null;
	}
	
	public Integer reduce(String key, List<Integer> listOfValues) {
		int count = 0;
		for (Integer val : listOfValues)
			count += val;
		return count;
	}
}
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class MRTest extends mapreduce.Mapper<String, Integer, Integer> {

	public MRTest() {
	}

	public HashMap<String, Integer> map(File resource) {
		emit("resource", 1);
		emit("dog", 5);
		emit("hair", 1);
		return null;
	}

	public Integer reduce(String key, List<Integer> listOfValues) {
		int count = 0;
		for (Integer val : listOfValues)
			count += val;
		return count;
	}
}

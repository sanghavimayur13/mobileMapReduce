import java.io.File;
import java.util.HashMap;
import java.util.List;

public class MRTest2 extends mapreduce.Mapper<Integer, Integer, Integer> {

	public MRTest2() {
		System.out.println("MR2 Constructor");
	}

	public HashMap<Integer, Integer> map(File resource) {
		emit(5, 1);
		emit(3, 5);
		emit(5, 1);
		return null;
	}

	public Integer reduce(Integer key, List<Integer> listOfValues) {
		int count = 0;
		for (Integer val : listOfValues)
			count += val;
		return count;
	}
}

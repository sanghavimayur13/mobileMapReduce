package DataSplitter;

import java.util.*;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DataSplitter {
	public DataSplitter(){
		
	}
	
	public File[] splitData(File... path){
		if(path.length == 1){
			//It's a directory
			if(path[0].isDirectory()){
				path = path[0].listFiles();
			}
		}
		Arrays.sort(path, new Comparator<File>(){
			public int compare(File a, File b){
				if(a.length() > b.length())
					return 1;
				else
					return -1;
			}
		});
		
		return path;
	}
	
	
	public long calcSize(){
		return 0;
	}
	
	
	public File[][] assignFile(int nodes, File... path){
		int length = path.length / nodes;
		if(path.length%nodes != 0){
			length++;
		}
		File[][] assignment = new File[nodes][length];
		for(int i=0;i<length;i++){
			for(int j=0;j<nodes;j++){
				
			}
		}
		return null;
	}
	
	
	public void compareAssignment(File dir, int nodes){
		File[] unsorted = dir.listFiles();
		File[] sorted = splitData(dir);
		
	}
	
	
	public static void main(String args[]){
		System.out.println("Compiled");
		if(args.length==1){
			File dir = new File(args[0]);
			if(dir.isDirectory()){
				new DataSplitter().compareAssignment(dir,5);
			}
			
		}
	}
}

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
	
	
	public long calcSize(File... path){
		long size=0;
		for(int i=0;i<path.length;i++)
			size+=path[i].length();
		return size;
	}
	
	
	public File[][] assignFile(int nodes, File... path){
		int length = path.length / nodes;
		if(path.length%nodes != 0){
			length++;
		}
		File[][] assignment = new File[nodes][length];
		int index = 0;
		for(int i=0;i<length;i++){
			for(int j=0;j<nodes;j++){
				if(index >= path.length){
					break;
				}
				assignment[j][i] = path[index++];  
			}
		}
		return assignment;
	}
	
	
	public void compareAssignment(File dir, int nodes){
		File[] unsorted = dir.listFiles();
		File[] sorted = splitData(dir);
		long[][] sizes = new long[2][nodes];
		long totalU =0, totalS = 0;
		for(int i=0;i<sorted.length;i++){
			sizes[0][i%nodes]+= unsorted[i].length();
			sizes[1][i%nodes]+= sorted[i].length();
			totalU += unsorted[i].length();
			totalS += sorted[i].length();
					
		}
		for(int i=0;i<nodes;i++){
			System.out.println("\t"+sizes[0][i]+"\t"+sizes[1][i]);
		}
		System.out.println("Total:\t"+totalU+"\t"+totalS);
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

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
	
	public HashMap<String, Long> splitData(String... path){
		if(path.length == 1){
			File dir = new File(path[0]);//It's a directory
			if(dir.isDirectory()){
				path = dir.list();
			}
		}
		HashMap<String, Long> sortedFiles = new HashMap<String, Long>();
		
		return null;
	}
	
	public static void main(String args[]){
		System.out.println("Compiled");
	}
}

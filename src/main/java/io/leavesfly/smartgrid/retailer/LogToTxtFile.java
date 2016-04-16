package io.leavesfly.smartgrid.retailer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class LogToTxtFile {
	private static FileOutputStream fileOut;
	private  final static PrintWriter writeLogToFile;
	
	static{
		try {
			fileOut=new FileOutputStream(new File(RetailerInitArgs.logFile));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		writeLogToFile=new PrintWriter(fileOut);
	}
	
	public synchronized static PrintWriter getWritelogtofile() {
		return writeLogToFile;
	}


	public static void main(String[] args){
		LogToTxtFile.writeLogToFile.println("hello");
		LogToTxtFile.writeLogToFile.flush();
	}
	
}

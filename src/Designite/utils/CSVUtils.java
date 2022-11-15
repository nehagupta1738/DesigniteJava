package Designite.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class CSVUtils {
	public static void initializeCSVDirectory(String projectName, String dirPath) {
		File dir = new File(dirPath);
		createDirIfNotExists(dir);
		cleanup(dir);
		initializeNeededFiles(dir);
	}
	
	private static void createDirIfNotExists(File dir) {
		if (!dir.exists()) {
			try {
				if(dir.mkdirs()==false)
					System.out.print("oops, couldn't create the directory " + dir);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.log(e.getMessage());
			}
		}
	}
	
	private static void cleanup(File dir) {
		if (dir.listFiles() != null) {
			for (File file : dir.listFiles()) {
				file.delete();
			}
		}
	}
	
	private static void initializeNeededFiles(File dir) {
		createCSVFile(dir.getPath() + File.separator + Constants.TYPE_METRICS_PATH_SUFFIX, Constants.TYPE_METRICS_HEADER);
		createCSVFile(dir.getPath() + File.separator + Constants.METHOD_METRICS_PATH_SUFFIX, Constants.METHOD_METRICS_HEADER);
	}
	
	private static void createCSVFile(String path, String header) {
		try {
			File file = new File(path);
	        file.createNewFile(); 
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append(header);
			bufferedWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
		}
	}
	
	public static void addToCSVFile(String path, String row) {
		try {
			File file = new File(path);
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append(row);
			bufferedWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
		}
	}

}

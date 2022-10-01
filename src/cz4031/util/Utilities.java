package cz4031.util;

import java.io.*;
import java.util.*;

import cz4031.storage.Record;

public class Utilities{
    public static List<Record> loadRecord(String path) throws Exception {
		File df = new File(path);
		System.out.println("Insert data from: " + path);
		if (!df.exists()) {
			df = new File(Constants.PROJECT_DIRECTORY, path); // use current directory
			System.out.println("Reattempt to insert data from: " + df.getAbsolutePath());
			
			if (!df.exists()){
				throw new FileNotFoundException("- File does not exist! -");
			}
		}

		BufferedReader br = null;
		List<Record> records = new ArrayList<>();
		String line;
		String[] fields = null;
		try {
			br = new BufferedReader(new FileReader(df));
			br.readLine(); // Ignoring header
			while((line = br.readLine()) != null) {
				fields = line.split("\\t"); //tab sepeprated fields in record
				Record record = new Record(fields[0], Float.parseFloat(fields[1]), Integer.parseInt( fields[2]));
				records.add( record );
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				}catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		System.out.println(" Total number of Records: " + records.size());
		return records;
	}

	//format to Byte size
	public static String formatFileSize(int size){
		String[] suffix = { "B", "KB", "MB", "GB", "TB" };
		int order = 0;
		if (size > 0){
			order = (int) (Math.log(size)/Math.log(1000));
		}
		double normSize = size / Math.pow(1000, order);
		return String.format("%.2f %s", normSize, suffix[order]);
	}

}
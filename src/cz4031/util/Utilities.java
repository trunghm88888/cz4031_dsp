package cz4031.util;
import java.io.*;
import java.util.*;

import cz4031.Constants;
import cz4031.storage.Record;

public class Utilities{
    private static final String TAG = "Utilities";
    public static List<Record> loadRecord(String path) throws Exception {
        //df = datafile
		File df = new File(path);
		Log.defaut(TAG, "Inserting data records from " + path);
		if (!df.exists()) {
			//try appeding with  current directory
			df = new File(Constants.PROJECT_DIRECTORY, path);
			Log.defaut(TAG,"Retry and load record from instead" + df.getAbsolutePath());

            //file does not exist
			if (!df.exists()){
				throw new FileNotFoundException("File does not exist");
			}
		}

		BufferedReader br = null;
		List<Record> records = new ArrayList<>();
		String line;
		String[] fields = null;
		try {
			br = new BufferedReader(new FileReader(df));
			// reading header first (to be skipped)
			br.readLine();
			while((line = br.readLine()) != null) {
                //attributes of record split by tab
				fields = line.split("\\t");
				Record record = new Record(fields[0], Float.parseFloat(fields[1]), Integer.parseInt( fields[2]));
				records.add( record );
				//Analyzer.analysis(record);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				}catch (IOException e) {
					Log.defaut(e.getMessage());
				}
			}
		}
		Log.defaut(TAG, "total records: "+records.size());
		//Analyzer.log();
		return records;
	}

    //format to byte-size
	public static String formatFileSize(int size){
		String[] suffix = { "B", "KB", "MB", "GB", "TB" };
		int order = 0;
		if (size > 0){
			order = (int) (Math.log(size)/Math.log(1000));
		}
		double normSize = size / Math.pow(1000, order);
		return String.format("%.2f %s", normSize, suffix[order]);
	}


	public static List<Record> generateRecords(int num){
		ArrayList<Record> records = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			String tconst = String.format("tt%08d", i+1);
			records.add( new Record(tconst, 0f, i+1));
		}
		return records;
	}

	public static List<Record> generateRecords(int num, int duplicates){
		ArrayList<Record> records = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			String tconst = String.format("tt%08d", i+1);
			records.add( new Record(tconst, 0f, i/duplicates));
		}
		return records;
	}
}
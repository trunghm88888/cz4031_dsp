package cz4031.util;

import java.text.SimpleDateFormat;
import java.util.Date;


// custom log class to allow showing/hiding of log message.
public class Log {
	private static final String TAG = "Log";
    public static final int LEVEL_Default  = 0;
	// public static final int LEVEL_NONE  = 0;
	// public static final int LEVEL_ASSERTION = 1; // log that should never be printed!!!!
	// public static final int LEVEL_ERROR = 2; // error or exception, please quickly fix it
	// public static final int LEVEL_WARN  = 3; // might lead to error, but works fine now.
	// public static final int LEVEL_INFO  = 4; // important process flow, good to show in log
	// public static final int LEVEL_DEBUG = 5; // messages for debugging purpose
	// public static final int LEVEL_VERBOSE = 6; // detail and redundant step by step messages
	// public static final int LEVEL_ALL = 10;
	public static final String FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String FORMAT_SIMPLE = "HH:mm:ss";
	private static int level = LEVEL_Default;
	private static boolean timestampEnabled = false;
	private static String dateformat = FORMAT_DETAIL;
	private static SimpleDateFormat sdf = new SimpleDateFormat(dateformat);


    //setting to determine logging
	public static void setLevel(int level) {
		Log.level = level;
	}

	public static boolean isTimestampEnabled(){
		return timestampEnabled;
	}

    //allow logger to attach timestamp for each log
	public static void setTimestampEnabled(boolean enabled){
		Log.timestampEnabled = enabled;
	}

    //time stamp format specified
	public static String getTimestampFormat(){
		return dateformat;
	}

    //change the timestamp format
	public static void setTimestampFormat(String format){
		dateformat = format;
		sdf = new SimpleDateFormat(format);
	}

	private static void print(int logLevel, String msg){
		if (level >= logLevel) {
			if (timestampEnabled){
				System.out.print(sdf.format(new Date())+" ");
			}
			System.out.println(msg);
		}
	}
	
    
    public static void defaut(String tag,String msg){
        print(LEVEL_Default,String.format("%s: %s", tag, msg));
    }
    
    // default tag is used when no tag is provided
	 public static void defaut(String msg) {
	 	defaut(TAG, msg);
	}


    
	// public static String getLogLevelString(){
	// 	switch (level) {
	// 		// case LEVEL_NONE : return "None";
	// 		// case LEVEL_ASSERTION: return "Assertion";
	// 		// case LEVEL_ERROR: return  "Error";
	// 		// case LEVEL_WARN: return  "Warn";
	// 		// case LEVEL_INFO: return  "Info";
	// 		// case LEVEL_DEBUG: return  "Debug";
	// 		// case LEVEL_VERBOSE: return  "Verbose";
	// 		// default: return "Unknown";
	// 	}
	// }
}

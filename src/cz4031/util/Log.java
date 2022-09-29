package cz4031.util;

import java.text.SimpleDateFormat;
import java.util.Date;

// custom log class to allow showing/hiding of log message.
public class Log {
	private static String dateformat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
	private static final String TAG = "Log";
    public static final int STATE_Default  = 0;
	private static int state = STATE_Default;

    //setting to determine logging
	public static void setState(int state) {
		Log.state = state;
	}

	private static void print(int logState, String msg){
		if (state >= logState) {
			if (true){
				System.out.print(sdf.format(new Date())+" ");
			}
			System.out.println(msg);
		}
	}
	
    public static void defaultPrint(String tag,String msg){
        print(STATE_Default,String.format("%s: %s", tag, msg));
    }
    
    // default tag is used when no tag is provided
	 public static void defaultPrint(String msg) {
	 	defaultPrint(TAG, msg);
	}

}

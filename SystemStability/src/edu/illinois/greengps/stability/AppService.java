package edu.illinois.greengps.stability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import android.content.Intent;
import android.os.Environment;

/**
 * Adapted from https://github.com/commonsguy/cw-advandroid
 *
 */
public class AppService extends WakefulIntentService {
	
	private static final String FILENAME="AlarmLog.txt";
	private static final String TAG = "AppService";
	
	public AppService() {
		super(TAG);
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		logToFile(TAG, "Received alarm. Restarting GreenGPS");

		try {
			android.util.Log.d("AppService", "Trying to reboot");
			Runtime.getRuntime().exec("su -c reboot");
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public boolean logToFile(String tag, String str) {
		android.util.Log.i(tag, System.currentTimeMillis() + ":" + str);
		String state = Environment.getExternalStorageState();                   
		if (Environment.MEDIA_MOUNTED.equals(state)) {                          
			try {                                                                 
				File path = this.getBaseContext().getExternalFilesDir(null);
				File file = new File(path, FILENAME);    

		        PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
		        out.println("<"+tag+"> " + new Date().toString() + " " + str);
		        out.flush();
		        out.close();
		        return true;
			} catch (Exception e) {                                             
			}                                                                     
		}
		return false;
	}
}

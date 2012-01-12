package edu.illinois.greengps.monitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;

public class AppMonitorService extends Service {
	
	private static final String FILENAME="MonitorLog.txt";
	private static final String TAG = "AppMonitorService";
	private static final String FUSION_SUITE= "edu.illinois.greengps";
	private static final String FUSION_SUITE_ACTIVITY = "edu.illinois.greengps.activity.FusionSuite";
	
	private Timer timer = new Timer(); 
	private static final int INTERVAL = 600000; // 10 minutes

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.i(TAG, "onCreate()");
		startService();
    }
    
    public void onDestroy() {
		super.onDestroy();
		android.util.Log.i(TAG, "onDestroy()");
		stopService();
	}
    
    private void startService() {
		timer.scheduleAtFixedRate( new TimerTask() {

			public void run() {
			    acquireStaticLock(AppMonitorService.this.getBaseContext());
				if (checkApplicationStatus(FUSION_SUITE) == null) {
					startApplication(FUSION_SUITE, FUSION_SUITE_ACTIVITY);
					try { //give greengps time to launch
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				releaseStaticLock();
			}

		}, 0, INTERVAL);
    }
    
    private void stopService() {
		if (timer != null) {
			timer.cancel();
		}
		try {
			releaseStaticLock();
		} catch (Exception e) {
			
		}
    }
    
    public static final String LOCK_NAME_STATIC="edu.illinois.greengps.stability.AppService.Static";
	private static PowerManager.WakeLock lockStatic=null;
	
	public static void acquireStaticLock(Context context) {
	    getLock(context).acquire();
	}
	
	public void releaseStaticLock() {
		getLock(this).release();
	}
	
	synchronized private static PowerManager.WakeLock getLock(Context context) {
	    if (lockStatic==null) {
	      PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
	      
	      lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
	      lockStatic.setReferenceCounted(true);
	    }
	    
	    return(lockStatic);
	}
    
	private RunningAppProcessInfo checkApplicationStatus(String pkg) {
		ActivityManager activityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
	    List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
	    for(int i = 0; i < procInfos.size(); i++){
	        if(procInfos.get(i).processName.matches(pkg)) {
	            logToFile(TAG, pkg + " is running at position " + i);
	            return procInfos.get(i);
	        }
	    }
        logToFile(TAG, pkg + " is not running");
        return null;
	}
	
	private void startApplication(String pkg, String cls) {
		try {
			logToFile(TAG, "starting application " + cls);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(new ComponentName(pkg, cls));
			this.getBaseContext().startActivity(intent);
		} catch (Exception e) {
			logToFile(TAG, "could not start " + cls);
		}
	}
	
	private void killProcess(RunningAppProcessInfo procInfo) {
		if (procInfo != null) {
			int pid = procInfo.pid;
			logToFile(TAG, "pid found " + pid + ", killing process");
			android.os.Process.killProcess(pid);
		}
	}
	
	private boolean logToFile(String tag, String str) {
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

package edu.illinois.greengps.stability;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Adapted from https://github.com/commonsguy/cw-advandroid/tree
 *
 */
public class StartupIntentReceiver extends BroadcastReceiver {

	private static final String TAG = "StabilityBootReceiver";
	// schedule the restart for some time around midnight
	public static long trigger_time;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {	
			
			Log.d(TAG,"received BOOT_COMPLETED signal"); 
			AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i=new Intent(context, OnAlarmReceiver.class);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0,
			                                              i, 0);
			
			trigger_time = System.currentTimeMillis()
					+ (24-Calendar.getInstance().get(Calendar.HOUR_OF_DAY))*3600*1000;

			mgr.setRepeating(AlarmManager.RTC_WAKEUP,
//					SystemClock.elapsedRealtime()+60000,
//					System.currentTimeMillis()+60000,
					trigger_time, AlarmManager.INTERVAL_DAY, pi);
			
//			Log.d(TAG, "Current time: "+System.currentTimeMillis());
//			Log.d(TAG, "Hour of day: "+Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
//			Log.d(TAG, "Millis to wait: "+(24-Calendar.getInstance().get(Calendar.HOUR_OF_DAY))*3600*1000);
//			Log.d(TAG, "Scheduled trigger time: " + trigger_time);
		}
	}
}

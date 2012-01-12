package edu.illinois.greengps.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Adapted from https://github.com/commonsguy/cw-advandroid/tree
 *
 */
public class StartupIntentReceiver extends BroadcastReceiver {

	private static final String TAG = "AppMonitorReceiver";
	// schedule the restart for some time around midnight
	public static long trigger_time;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {	
			
			Log.d(TAG,"received BOOT_COMPLETED signal"); 
			context.startService(new Intent(context, AppMonitorService.class));
		}
	}

}


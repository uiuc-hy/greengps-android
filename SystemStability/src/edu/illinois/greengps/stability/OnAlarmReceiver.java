package edu.illinois.greengps.stability;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Adapted from https://github.com/commonsguy/cw-advandroid
 *
 */
public class OnAlarmReceiver extends BroadcastReceiver {
	
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    WakefulIntentService.acquireStaticLock(context);
	    
	    context.startService(new Intent(context, AppService.class));
	  }
}

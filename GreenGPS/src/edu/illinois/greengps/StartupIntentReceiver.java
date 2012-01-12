package edu.illinois.greengps;

import edu.illinois.greengps.activity.FusionSuite;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupIntentReceiver extends BroadcastReceiver {
	
	public final static String TAG = "FusionSuite Boot Service";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {	
			Log.d(TAG,"Starting FusionSuite application in background");

			// Start the FusionSuite activity in a new thread, just in case it
			// decides to exceed the 10 second timeout 
			FSThread fst = new FSThread(context);
			fst.start();
		}
	}
	
	private class FSThread extends Thread {
		public Context context;
		
		public FSThread(Context context) {
			this.context = context;
		}
		
		public void run() {
			Intent fs = new Intent(context, FusionSuite.class);
			fs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(fs);
		}
	}
}

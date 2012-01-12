package edu.illinois.greengps.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class EnergyMonitor extends Thread {
	
	private FusionSuiteSingleton singleton;
	private BatteryMonitor bMonitor;
	
	private static final String TAG = "EnergyMonitor";
	
	private boolean stop;
	
	public EnergyMonitor(FusionSuiteSingleton singleton) {
		this.singleton = singleton;
		this.stop = false;
		bMonitor = new BatteryMonitor(singleton.getFusionSuiteActivity());
	}
	
	public void setStop() {
		stop = true;
		this.interrupt();
	}

	public void run() {
		while(!stop) {
			bMonitor.checkBattery();
			
			try {
				Thread.sleep(FusionSuiteSingleton.energy_monitor_interval);
			} catch (InterruptedException e) {
			}
		}
		
		singleton.display(TAG, "exit thread");
	}

	private class BatteryMonitor extends BroadcastReceiver{
		
		private int powerSource = -1;
		private FusionSuite suite;
		
		public BatteryMonitor(FusionSuite suite) {
			this.suite = singleton.getFusionSuiteActivity();
		}
		
		public void checkBattery() {
			unregisterReceiver();
			IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			suite.registerReceiver(this, batteryLevelFilter);
		}
		
		private void unregisterReceiver() {
			try {
				singleton.getFusionSuiteActivity().getApplicationContext().unregisterReceiver(this);
			} catch (Exception e) {
			}
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				context.unregisterReceiver(this);
			} catch (Exception e) {
				singleton.logMsg(TAG, "Error unregistering receiver");
			}
			  		
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			singleton.logMsg(TAG, System.currentTimeMillis() +": Battery Level Remaining: " + level + "%");
			  		
			int source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			
			if (source == 0 && level <= 15) {
				// take drastic action if no power source and battery <= 15%
				singleton.onBatteryMode();
				singleton.enterEmergencyEnergySave();
				powerSource = source;
			}
			else if (source != powerSource) {
				powerSource = source;
				if (source == 0 ) {
					singleton.display(TAG, "Power source: on battery");
					singleton.onBatteryMode();
				}
				else if (source == 1 ) {
					singleton.display(TAG, "Power source: on AC");
					singleton.onPowerSourceMode();
				}
				else if (source == 2) {
					singleton.display(TAG, "Power source: on USB");
					singleton.onPowerSourceMode();
				}
				else {
					singleton.display(TAG, "Power source: " + source);

				}
			}
	 	}
	}
	
}


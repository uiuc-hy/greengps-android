package edu.illinois.greengps.sensing;

import edu.illinois.greengps.activity.FusionSuiteSingleton;

public class Sampler extends Thread {
	private GPSSampler gps;
	private OBDSampler obd;
	private boolean stop = false;
	
	private static String TAG = "Sampler";
	private static FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
	
	public Sampler() {
		singleton.display(TAG, "initializing sampler");
		startAll();
	}
	
	public void run() {
		while (!stop) {
			if (!obd.isRunning()) {
				singleton.display(TAG, "engine off, restarting samplers in " 
						+ FusionSuiteSingleton.idle_sample_interval + " seconds");
				stopAll();
				singleton.engineOffMode();
				sleep(FusionSuiteSingleton.idle_sample_interval);
				if (!stop) { ///< re-check in case of an interrupt
					startAll();
				}
			}
			else {
				android.util.Log.i(TAG, "sleeping for short interval");
				// give the obd sampler time to collect data/accumulate errors
				sleep(FusionSuiteSingleton.sampler_control_interval);
			}
		}
		
		stopAll();
		singleton.display(TAG, "exit thread");
	}
	
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			singleton.display(TAG, "interrupted from sleep");
		}
	}
	
	public void setStop() {
		stop = true;
		this.interrupt();
	}
	
	public void stopAll() {
		singleton.display(TAG, "Stopping all samplers");
		
		if (gps != null) {
			gps.disableListener();
			gps = null;
		}
		if (obd != null) {
			obd.setStop();
			obd = null;
		}
	}
	
	public void startAll() {
		singleton.turnOnBluetooth();
		if (!singleton.isBluetoothEnabled())
			sleep(15000); ///< bluetooth takes awhile to turn on
		
		singleton.display(TAG, "starting all samplers");
		gps = new GPSSampler();
		obd = new OBDSampler();
		obd.start();
	}
}


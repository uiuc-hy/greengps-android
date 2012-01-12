package edu.illinois.greengps.device;

import android.content.Context;
import android.net.wifi.WifiManager;
import edu.illinois.greengps.activity.FusionSuite;

public class WiFiDevice implements Device{
	
	private WifiManager wm;
	private static final String TAG = "WiFiDevice";
	
	public WiFiDevice(FusionSuite suite) {
		wm = (WifiManager) suite.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void enable() {
		android.util.Log.i(TAG, "turning wifi on");
		wm.setWifiEnabled(true);
	}
	
	public void disable() {
		android.util.Log.i(TAG, "turning wifi off");
		wm.setWifiEnabled(false);
	}
	
	public boolean isEnabled() {
		return wm.isWifiEnabled();
	}
}

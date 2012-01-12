package edu.illinois.greengps.device;

import android.bluetooth.BluetoothAdapter;

public class BTDevice implements Device{
	
	private static final String TAG = "BTDevice";

	public BTDevice() {
		
	}
	
	public void enable() {
		android.util.Log.i(TAG, "turning bluetooth on");
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		mBluetoothAdapter.enable();
	}
	
	public void disable() {
		android.util.Log.i(TAG, "turning bluetooth off");
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		mBluetoothAdapter.disable();
	}
	
	public boolean isEnabled() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		return mBluetoothAdapter.isEnabled();
	}
}

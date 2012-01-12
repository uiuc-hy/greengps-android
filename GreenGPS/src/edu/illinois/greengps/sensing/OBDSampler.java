package edu.illinois.greengps.sensing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.db.TableConstants;
import edu.illinois.greengps.obdroid.ELMInterface;
import edu.illinois.greengps.obdroid.ELMParam;
import edu.illinois.greengps.sensing.Sample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class OBDSampler extends Thread {
	
	protected static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final String TAG = "OBDSampler";
	
	private ELMInterface _obd;
	private BluetoothDevice _dev;
	private BluetoothSocket _sock;
	private boolean _stop;
	private long _tripID;
	private FusionSuiteSingleton _singleton;
	private InputStream _in; 
	private OutputStream _out;
	
	public OBDSampler() {
		_stop = false;
		_tripID = -2; // -2 indicates uninitialized
		_singleton = FusionSuiteSingleton.getInstance();
	}
	
	/**
	 * Add a sample indicating that the engine change state (on/off).
	 * - a tripID>=0 indicates that this is a valid engine status change
	 * - the tag stores the state (on/off)
	 * - the data map stores the tripID
	 * 
	 * @param status - any string, but only 'on' or 'off' is recognized by server
	 */
	private void logEngineStatusChange(String status) {

		if (_tripID < 0) {
			_singleton.logMsg(TAG, "did not record " + status);
			return; // nothing to log, likely an 'off' without and 'on' first
		}
		else {
			_singleton.display(TAG, "engine is " + status);
		}
			
		Map <TableConstants, String> map = new HashMap<TableConstants, String>();
		map.put(TableConstants.TRIPID, "" + _tripID);
		Sample s = 
			new Sample(System.currentTimeMillis(), status, map, TableConstants.ENGINESTATUSCHANGE);
		_singleton.addSample(s);
	}

	/**
	 * Add a sample by extracting non NaN values from the collection
	 * 
	 * @param params
	 */
	private void addSample(Collection<ELMParam> params) {
		
		HashMap<TableConstants, String> values = new HashMap<TableConstants, String>();
		for (ELMParam e: params) {
			if (e.hasCmd) {
				if (!Float.isNaN(e.lastVal)) {
					values.put(e.name, String.valueOf(e.lastVal));
					e.resetError();
				}
				else {
					e.addError();
				}
			}
		}
		
		values.put(TableConstants.TRIPID, String.valueOf(_tripID));
		Sample sample = 
			new Sample(System.currentTimeMillis(), _singleton.getTag(), values, TableConstants.OBDSAMPLEID);
		_singleton.addSample(sample);
	}
	
	/**
	 * Locate the first paired device available and store it in the global variable _dev
	 * 
	 * @return true on success, false if bluetooth is off or cannot find paired devices
	 */
	private boolean findPairedDevice() {
		_singleton.logMsg(TAG, "finding paired device");
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter != null && adapter.isEnabled()) {
			Set<BluetoothDevice> devices = adapter.getBondedDevices();
			
			if (!devices.isEmpty()) {
				_dev = devices.iterator().next();
				_singleton.display(TAG, "Found paired: " + _dev.getName());
				return true;
			}
			
			_singleton.display(TAG, "No paired devices");
			return false;
		}

		_singleton.display(TAG, "Cannot initialize Bluetooth");
		return false;
	}
	
	/**
	 * Connect to paired device and initialize the elm interface for communicating with 
	 * the OBD II adapter
	 * 
	 * @return true on success, false on error
	 */
	private boolean connect() {
		try {
			_singleton.display(TAG, "Connecting");
			_sock = _dev.createRfcommSocketToServiceRecord(MY_UUID);
			_sock.connect();
		
			_in = _sock.getInputStream();
			_out = _sock.getOutputStream();
			_obd = new ELMInterface(_in, _out);
			_singleton.display(TAG, "Connected to " + _dev.getName() + " (" + _dev.getAddress() + ")");
			return true;
		} catch (IOException e) {
			_singleton.display(TAG, "Error connecting to OBD-II interface");
			_singleton.logException(TAG, e);
			return false;
		} catch (Exception e) {
			_singleton.display(TAG, "Error settings up data streams");
			_singleton.logException(TAG, e);
			return false;
		}
	}
	
	private boolean disconnect() {
		try {
			_sock.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Reset OBD-II settings and connect the engine on a fresh start
	 * 
	 * @return true on success, false if the initialization fails
	 */
	private boolean resetObd() {
		try {
			_singleton.display(TAG, "Resetting");
			_obd.reset();
			_singleton.display(TAG, "Connected to ECU");
			return true;
		} catch (Exception e) {
			_singleton.display(TAG, "Error resetting OBD-II interface");
			disconnect();
			return false;
		}
	}
	
	/**
	 * Initialize the bluetooth and OBD-II connections. Since any stage of 
	 * the initialization have been observed to fail on a whim, each stage is 
	 * allowed a certain number of attempts, defined by obd_max_errors
	 * 
	 * @return true on success, false if any stage fails
	 */
	private boolean initialize() {
		int tries = 0;
		while(!findPairedDevice() && !_stop) {
			if (tries++ > FusionSuiteSingleton.obd_max_errors)
				return false;
			nap(FusionSuiteSingleton.sample_interval);
		}
		
		tries = 0;
		while((!connect() || !resetObd()) && !_stop) {
			if (tries++ > FusionSuiteSingleton.obd_max_errors)
				return false; 
			nap(FusionSuiteSingleton.sample_interval);
		}
/* connecting and resetting must go together
		tries = 0;
		while(!resetObd() && !_stop) {
			if (tries++ > FusionSuiteSingleton.obd_max_errors)
				return false;
			nap(FusionSuiteSingleton.sample_interval);
		}
*/

		if (!_stop) { // display and store the elmid and vid for logging purposes
			if (_obd.elmid != null && _obd.vid != null)
				_singleton.display(TAG, "Elmid: " + _obd.elmid + ", " + "Vid: " + _obd.vid);
			return true;
		}
		return false;
	}
	
	/**
	 * Milk the engine for data, this thread will run until obd_max_errors is 
	 * breached, at which point it will assume the engine is off and exit thread
	 * 
	 * - only IOExceptions count towards obd_max_errors, for now if the engine 
	 *   is on, no other exceptions matters
	 * - this thread is responsible for turning bluetooth off, since it will be
	 *   the first to know when the sockets are ready to close
	 */
	public void run() {
		if (!initialize()) {
			_stop = true;
			_singleton.turnOffBluetooth();
			_singleton.display(TAG, "failed to initialize, exit thread");
			return;
		}
		
		boolean on = false;
		int errors = 0;
		while (!_stop) {
			
			try {
				Collection<ELMParam> params = _obd.getParams();
				if (!on) { // record an engine 'on'
					_tripID = System.currentTimeMillis();
					logEngineStatusChange("on");
					on = true;
					_singleton.engineOnMode();
				}
				addSample(params);
				
			} catch (IOException e) {
				_singleton.logMsg(TAG, e.toString());
				if (errors++ > FusionSuiteSingleton.obd_max_errors) {
					_singleton.display(TAG, "too many IOExceptions");
					break;
				}
				nap(FusionSuiteSingleton.sample_interval);
			} catch (Exception e) {
				// TODO: determine if we should look into any specific error type
				_singleton.logMsg(TAG, e.toString());
				continue;
			}
		}
		
		if (on) // record an engine 'off'
			logEngineStatusChange("off");
		safeStop();
		
		_singleton.display(TAG, "exit thread");
	}
	
	/**
	 * Put the OBD-II adapter into low energy mode and stop all bluetooth communication
	 */
	private void safeStop() {
		_singleton.display(TAG, "performing safe stop");
		_stop = true;

		/* the powersave command has not worked for any car yet
		try {
			_obd.enterPowerSave();
		} catch (Exception e1) {
			_singleton.display(TAG, "error entering power save");
		}
		*/
		
		try {
			_sock.close();
			_singleton.turnOffBluetooth();
		} catch (IOException e) {
		}
	}
	
	private void nap(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			_singleton.logMsg(TAG, "Interrupted from sleep of " + time + "ms");
		}
	}
	
	/**
	 * Check if the thread is running, false indicates some part of the connection failed
	 * 
	 * @return 
	 */
	public boolean isRunning() {
		return !_stop;
	}
	
	public void setStop() {
		_stop = true;
	}
}


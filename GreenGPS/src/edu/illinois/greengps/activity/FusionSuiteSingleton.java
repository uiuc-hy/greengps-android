package edu.illinois.greengps.activity;

import edu.illinois.greengps.db.DatabaseHelper;
import edu.illinois.greengps.db.TableConstants;
import edu.illinois.greengps.sharing.FileSharingModule;
import edu.illinois.greengps.sharing.TCPPushSharingModule;
import edu.illinois.greengps.sensing.Sample;
import edu.illinois.greengps.sensing.Sampler;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import edu.illinois.greengps.device.*;

public class FusionSuiteSingleton 
{
  private static FusionSuiteSingleton instance = new FusionSuiteSingleton();
  
  public static final String start_time = new Date().toString();

  public static final String TAG_ERROR = "ERROR";
  public static final String TAG_WARNING = "WARNING";
  public static final String TAG_INFO = "INFO";

  public static final int sample_interval = 2000;
  public static final int tcp_push_interval = 5000; 
  public static final int tcp_error_timeout = 300000; // 5 minutes
  public static final int sampler_control_interval = 6000;
  public static final int energy_monitor_interval = 6000;
  public static final int idle_sample_interval = 180000; // 3 minutes
  public static final int obd_max_errors = 20; // max # of obd exceptions before engine off
  public static final int tcp_max_errors = 25; // max # of tcp exceptions in a row before sleep
  public static final int max_log_size = 50; // max # of messages to show in log panel
  public static final int data_block_size = 50; // number of samples per packet
  public static final boolean debug = true; // extra logging
  
  public boolean persistent_log = false;
  public boolean first_start = true;
  public FileOutputStream log_file_output_stream = null;
  public boolean running = false;

  private Sampler sampler = null;
  private FusionSuite suite;
  private int sample_count = 0;

  private DatabaseHelper dbHelper;
  private EnergyMonitor eMonitor;
  private FileSharingModule fileShare;
  private TCPPushSharingModule tcpShare;
  
  private BTDevice bt;
  private WiFiDevice wifi;

  private Handler mHandler;
  
  private static final String TAG = "FusionSuiteSingleton";


  private FusionSuiteSingleton() {
//    sharing_modules = new ArrayList<SharingModule>();
  }
  
  public static FusionSuiteSingleton getInstance() {
    return instance;
  }

  public void setFusionSuiteActivity(FusionSuite suite, Handler handler) {
    this.suite = suite;
    this.mHandler = handler;
  }
  
  public void setFusionSuiteDBHelper(DatabaseHelper dbHelper) {
	this.dbHelper = dbHelper;
  }

  public FusionSuite getFusionSuiteActivity() {
    return suite;
  }
  
  public void logMsg(String tag, String msg) {
    if (!persistent_log)
      return;
    PrintWriter p = new PrintWriter(log_file_output_stream);
    p.println("[" + tag + "] " + msg);
    p.flush();
  }

  public void logException(String tag, Exception e) {
    if (!persistent_log)
      return;
    PrintWriter p = new PrintWriter(log_file_output_stream);
    p.print("[" + tag + "] "); 
    e.printStackTrace(p);
    p.flush();
  }

  public void display(final String tag, final String msg) {
    logMsg(tag, "Display-> " + msg);
    if (suite != null)
    	mHandler.obtainMessage(0, msg.length(), -1, tag + " >> " + msg).sendToTarget();
  }

  public String getTag() {
    if (suite == null)
      return "3";
    return suite.getTag();
  } 
  
  private void insertSampleInDB(Sample sample) {
	  dbHelper.insert(sample.id, sample.toString());
  }

  /**
   * Add a sample to the database and log file, and display to screen
   * @param sample
   */
  public void addSample(Sample sample) {
    sample_count ++;
    String disp = "Sample #" +sample_count+ " measured (" + sample.id + ") ";
    
    display(TAG, disp);
    insertSampleInDB(sample);
    
    if (sample.type.equals(TableConstants.OBDSAMPLEID))
    	display(TAG_INFO, sample.toReadableString());
    
//    for (int i = 0; i < sharing_modules.size(); i++)
//     sharing_modules.get(i).addSample(sample);
    fileShare.addSample(sample);
  }
  
  private void initializeDevices() {
	if (bt == null)
		bt = new BTDevice();
	if (wifi == null)
		wifi = new WiFiDevice(suite);
  }

  /**
   * Called on button press, start energy monitor, sharing, and sampling
   */
  public void startServices() {
	android.util.Log.i(TAG, "starting services");

	initializeDevices();

	if (fileShare == null || !fileShare.isAlive()) {
		fileShare = new FileSharingModule();
		fileShare.start();
	}
	
//	restartTCPShare();

	if (eMonitor == null) {
		eMonitor = new EnergyMonitor(this);
		eMonitor.start();  
	}
  }
  
  /**
   * Called on button press, this will stop energy monitoring, sampling and sharing
   */
  public synchronized void stopServices() {
//	releaseWakeLock();
	  
	if (eMonitor != null ) {
		eMonitor.setStop();
		eMonitor = null;
	}
    
    if (sampler != null) {
    	sampler.setStop();
    	sampler = null;
    }
    
    tcpShare.cancel();
  }
  
  public void turnOnWifi() {
	  if (!wifi.isEnabled()) {
		  wifi.enable();
	  }
  }
  
  public void turnOffWifi() {
	  if (wifi.isEnabled()) {
		  wifi.disable();
	  }
  }
  
  public boolean isWifiEnabled() {
	  return wifi.isEnabled();
  }
  
  public void turnOnBluetooth() {
	  if (!bt.isEnabled()) {
		  bt.enable();
	  }
  }
  
  public void turnOffBluetooth() {
	  if (bt.isEnabled()) {
		  bt.disable();
	  }
  }
  
  public boolean isBluetoothEnabled() {
	  return bt.isEnabled();
  }
  
  public void clearSamples() {
	  display(TAG, "Clearing samples table");
	  dbHelper.clearSamples();
  }

  public void engineOffMode() {
	  display(TAG, "Engine off mode");
	  tcpShare.enterEnergySavingMode();
  }
  
  public void restartTCPShare() {
	  turnOnWifi();
	  if (tcpShare == null || !tcpShare.isAlive()) {
		  tcpShare = new TCPPushSharingModule(suite.getApplicationContext());
		  tcpShare.start();
	  }
	  else {
		  tcpShare.exitEnergySavingMode();
	  }
  }
  
  public void enterEmergencyEnergySave() {
	  // TODO: 
	  acquireWakeLock();
	  tcpShare.cancel();
	  turnOffBluetooth();
	  turnOffWifi();
	  releaseWakeLock();
  }
  
  /**
   * Wake up sharing modules
   */
  public void engineOnMode() {
	  display(TAG, "Engine on mode");
	  restartTCPShare();
  }
  
  /**
   * The device has switched from running on battery to running on a power source
   * - assume the car is on
   * - start sampling to check on the engine state
   * - start sharing to upload data
   */
  public void onPowerSourceMode() {
	  if (sampler == null) {
		  logMsg(TAG, "starting new sampler");
		  sampler = new Sampler();
		  sampler.start();
	  }
	  restartTCPShare();
  }

  /**
   * The device has switched from running on a power source to running on battery
   * - assume the the car is off
   * - for now, ignore the possibility of someone unplugging the phone
   * - turn off sampling and only allow wifi to transmit data
   */
  public void onBatteryMode() {
//	  acquireWakeLock();
	  if (sampler != null) {
		  logMsg(TAG, "stopping sampler");
		  sampler.setStop(); 
		  sampler = null;
	  }
	  
	  if (tcpShare != null)
		  tcpShare.enterEnergySavingMode();
	  else
		  turnOffWifi();
	  turnOffBluetooth();
//	  releaseWakeLock();
  }

  private WakeLock mWakeLock;
  
  public void acquireWakeLock() {
	  if (mWakeLock == null) {
		  PowerManager pm = (PowerManager) suite.getSystemService(Context.POWER_SERVICE);
		  mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
	  }
	  if (!mWakeLock.isHeld()) {
		  logMsg(TAG, "Acquiring CPU wake lock");
		  mWakeLock.acquire();
	  }
  }
  
  public void releaseWakeLock() {
	  if (mWakeLock != null && mWakeLock.isHeld()) {
		  logMsg(TAG, "Releasing CPU wake lock");
		  mWakeLock.release();
	  }
  }

  
}

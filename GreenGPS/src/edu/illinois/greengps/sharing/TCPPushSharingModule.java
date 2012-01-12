package edu.illinois.greengps.sharing;

import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.db.DatabaseHelper;
import edu.illinois.greengps.db.TableConstants;
import edu.illinois.greengps.sensing.Sample;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import android.content.Context;

public class TCPPushSharingModule extends SharingModule
{
	public static final String tcpHost = "ndn.cs.illinois.edu";

  protected ArrayList <Sample> samples;
  protected final Semaphore samples_lock = new Semaphore(1);
  private DatabaseHelper dbHelper;
  private final String TAG = "TCPPushSharingModule";
  
  private enum Mode { NORMAL, ENERGY_SAVING};
  private Mode myMode;
  private FusionSuiteSingleton singleton;
  private Socket socket;
  private PrintWriter out;
  private InputStreamReader in;
  
  private long energy_save_start = 0;
  private long startId = -1;

  public TCPPushSharingModule(Context ctx) {
    samples = new ArrayList<Sample>();
    dbHelper = new DatabaseHelper(ctx);
    singleton = FusionSuiteSingleton.getInstance();
    myMode = Mode.NORMAL;
    dbHelper.setupState(TAG, 0);
    startId = dbHelper.getStateStatus(TAG);
  }
  
  /**
   * Attempts to setup both an input and output stream to the server, assume
   * the setup is correctly initialized in getting an ACK from the server in
   * 1 second or less
   * 
   * @return
   */
  private boolean setupStreams() {
	  if (socket == null) {
		  try {
			  socket = new Socket(tcpHost, 4938);
			  socket.setSoTimeout(1000);
			  out = new PrintWriter(socket.getOutputStream());
			  in = new InputStreamReader(socket.getInputStream());
			  
			  int ack = in.read(); // get an ACK, meaning server is ready
			  singleton.logMsg(TAG, "ACK from server: " + (char) ack);
			  return true;
		  } catch (Exception e) {
			  singleton.logMsg(TAG, "error initializing streams");
			  socket = null;
			  return false;
		  }
	  }
	  return true;
  }
  
  public void run() {
	  energy_save_start = System.currentTimeMillis();
	  
	  while (!stop) {
		  int total = 0;
		  ArrayList <Entry<Long, String>> data = 
			  dbHelper.getData(startId, FusionSuiteSingleton.data_block_size);
		  total = data.size();
		  android.util.Log.i(TAG, "number of data points to upload " + total);		
		  
		  if (myMode.equals(Mode.ENERGY_SAVING) && total==0) {
			  singleton.display(TAG, "BREAK detected empty database at " + System.currentTimeMillis());
			  break;
		  }
			  
		  try { // try to upload, and reset counter if successful
			  sendData(data);
			  energy_save_start = System.currentTimeMillis();
		  } catch (Exception e) {
			  total = 0;
			  if (myMode.equals(Mode.ENERGY_SAVING) 
					  && energy_save_start + FusionSuiteSingleton.tcp_error_timeout < System.currentTimeMillis()) {
				  singleton.display(TAG, "BREAK exceeded error timeout at " + System.currentTimeMillis());
				  break;
			  }
		  }
		  
		  if (total < FusionSuiteSingleton.data_block_size) 
			  nap(FusionSuiteSingleton.tcp_push_interval);
	  }

	  try {
		  in.close();
	  } catch (Exception e) {}
	  try {
		  out.close();
	  } catch (Exception e) {}
	  try {
		  socket.close();
	  } catch (Exception e) {
	  }
	  
	  singleton.turnOffWifi();

	  // TODO: if the server shuts down unexpectedly, the phone may send one or two 
	  // extra batches of data, so lower the cut off bound, just in case
	  this.cleanDatabase(startId);
	  singleton.display(TAG, "exit thread");
  }
  
  /**
   * Send all the samples within the data set at once in one string, using separator "|"
   * @param data
   * @throws UnknownHostException
   * @throws IOException
   */
  private void sendData(ArrayList <Entry<Long,String>> data) throws Exception {
	  if (data==null || data.size() == 0) {
		  return;
	  }
	  
	  if (!setupStreams()) {
		  throw new IOException("Error setting up streams");
	  }
	  
	  String message = null;
	  Entry<Long,String> last = null;
	  
	  for (Entry<Long,String> en: data) {
		  if (message==null) {
			  message = TableConstants.NODEID.ordinalToString() + ":" + phone_id + " " + en.getValue();
		  } else {
			  message += "|" + TableConstants.NODEID.ordinalToString() + ":" + phone_id + " " + en.getValue();
		  }
		  last = en;
	  }
	  
	  out.println(message);
	  
	  if (out.checkError() || socket.isInputShutdown()) {
		  if (socket != null && !socket.isClosed()) {
			  socket.close();
			  socket = null;
		  }
		  out.close();
		  in.close();
		  throw new IOException("Error printing to output stream");
	  }

	  startId = last.getKey();
	  dbHelper.updateStateStatus(TAG, startId);
	  singleton.display(TAG, "sent " + data.size() + " samples");
  }
  
  /**
   * Remove all data with an id <= lastSent
   * @param lastSent
   */
  private void cleanDatabase(long lastSent) {
	  singleton.display(TAG, "deleting all sent samples where id <= " + lastSent);
	  if (lastSent >= 0)
		  dbHelper.deleteRange(lastSent);
  }
  
  private synchronized boolean isEnergySaving() {
	  return myMode.equals(Mode.ENERGY_SAVING);
  }
  
  private void nap(int time) {
	try {
		Thread.sleep(time);
	} catch (InterruptedException e) {
		singleton.display(TAG, "interrupted from sleep interval: " + time);
	}
  }

  public synchronized void enterEnergySavingMode() {
	  if (!isEnergySaving()) {
		  myMode = Mode.ENERGY_SAVING;
		  energy_save_start = System.currentTimeMillis();
		  singleton.display(TAG, "Enter energy saving mode at " + energy_save_start);
		  this.interrupt();
	  }
  }
  
  public synchronized void exitEnergySavingMode() {
	  android.util.Log.i(TAG, "Exit energy saving mode");
	  singleton.turnOnWifi();
	  myMode = Mode.NORMAL;
	  this.interrupt();
  }
  
  public boolean addSample(Sample sample) { 
	// samples are added to the database irrespective of the sharing module
    return true;
  }
}


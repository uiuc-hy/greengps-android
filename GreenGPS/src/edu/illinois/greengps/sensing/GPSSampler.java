package edu.illinois.greengps.sensing;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import edu.illinois.greengps.activity.FusionSuite;
import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.db.TableConstants;
import java.util.HashMap;
import java.util.Map;

public class GPSSampler implements LocationListener {
	
  protected LocationManager locationManager;
  private long last_GPS_time = 0;
  private final static String TAG = "GPSSampler";

  /**
   * Initialize a register a receiver for the GPS service, a Looper is required
   * to initialize GPS
   */
  public GPSSampler() {
    FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
    FusionSuite suite = singleton.getFusionSuiteActivity();

    if (Looper.myLooper() == null) {
    	Looper.prepare();  ///< request location updates requires this
    }
    
    try { // Start location manager
    	singleton.display(TAG, "Registering with GPS provider");
    	locationManager = 
    		(LocationManager)suite.getSystemService(Context.LOCATION_SERVICE);
      
    	locationManager.requestLocationUpdates(
    		LocationManager.GPS_PROVIDER, FusionSuiteSingleton.sample_interval, 0, this);
    } catch (Exception e) {
    	singleton.display(TAG, "Error initializing GPS Sampler!");
    	singleton.logException(FusionSuiteSingleton.TAG_ERROR, e);
    }
  }


  /**
   * Receiver for a location update, create a new sample for the update
   */
  @Override
  public void onLocationChanged(Location location) {

	  if (location.getTime() < last_GPS_time + FusionSuiteSingleton.sample_interval)
		  return;
	  last_GPS_time = location.getTime();
	  
	  // grab whatever GPS data is available
	  Map<TableConstants,String> dataMap = new HashMap<TableConstants, String>();
	  dataMap.put(TableConstants.GPSTIME, (new Long(location.getTime())).toString());
	  dataMap.put(TableConstants.GPSLATITUDE, "" + location.getLatitude());
	  dataMap.put(TableConstants.GPSLONGITUDE, "" + location.getLongitude());
	  if (location.hasSpeed())
		  dataMap.put(TableConstants.GPSSPEED, "" + location.getSpeed());
	  if (location.hasBearing())
		  dataMap.put(TableConstants.GPSBEARING, "" + location.getBearing());
	  if (location.hasAltitude())
		  dataMap.put(TableConstants.GPSALTITUDE, "" + location.getAltitude());

	  FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
	  Sample sample = 
		  new Sample(System.currentTimeMillis(), singleton.getTag(), dataMap, TableConstants.GPSSAMPLEID);
	  singleton.addSample(sample);
  }
  
  @Override
  public void onProviderDisabled(String provider) {
	  FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
	  singleton.display(TAG, "gps is disabled");
  }
  
  @Override
  public void onProviderEnabled(String provider) {
  }
  
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }
  
  /**
   * Disable the gps listener to reduce power use
   */
  public void disableListener() {
	  if (locationManager != null) {
		FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
		singleton.display(TAG, "Deregistering with GPS provider");
		locationManager.removeUpdates(this);
	  }
  }
}

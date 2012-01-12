package edu.illinois.greengps.sharing;

import android.content.Context;
import android.telephony.TelephonyManager;

import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.sensing.Sample;

public abstract class SharingModule extends Thread
{
  protected boolean stop = false;
  protected String phone_id;

  public SharingModule() {
    FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
    TelephonyManager telephony = (TelephonyManager) 
      singleton.getFusionSuiteActivity().getSystemService(
        Context.TELEPHONY_SERVICE); 
    phone_id = telephony.getDeviceId();
  }

  public void cancel() {
    stop = true;
    this.interrupt();
  }
  

  /**
   * Adds a sample to be shared. Returns true on success.
   */
  public abstract boolean addSample(Sample sample);
  
  public abstract void enterEnergySavingMode();
  public abstract void exitEnergySavingMode();
}


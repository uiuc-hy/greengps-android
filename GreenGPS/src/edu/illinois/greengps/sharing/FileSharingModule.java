package edu.illinois.greengps.sharing;

import android.os.Environment;

import edu.illinois.greengps.activity.FusionSuiteSingleton;
import edu.illinois.greengps.sensing.Sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
                                    
public class FileSharingModule extends SharingModule 
{
  public void run() {
  }

  /**
   * Adds a sample to be shared. Returns true on success.
   */
  public boolean addSample(Sample sample) {
    String state = Environment.getExternalStorageState();                   
    if (Environment.MEDIA_MOUNTED.equals(state)) {                          
      try {                                                                 
        File path = FusionSuiteSingleton.getInstance()
            .getFusionSuiteActivity().getExternalFilesDir(null);
        File file = new File(path, "Samples");    

        PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
        out.println("NodeID:" + phone_id + " " + sample.toReadableString());
        out.flush();
        out.close();
        return true;
      } catch (Exception e) {                                             
      }                                                                     
    }
    return false;
  }

  @Override
  public void enterEnergySavingMode() {
  }

  @Override
  public void exitEnergySavingMode() {
  }
}


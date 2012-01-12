package edu.illinois.greengps.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.illinois.greengps.R;

public class GreenGPS extends Activity {
 
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.greengps);
    
    TextView t=(TextView) findViewById(android.R.id.title);
    t.setGravity(Gravity.CENTER);

    RadioButton rb = (RadioButton) findViewById(R.id.radio_fast);
    rb.setOnCheckedChangeListener(rbListener);
    rb = (RadioButton) findViewById(R.id.radio_short);
    rb.setOnCheckedChangeListener(rbListener);
    rb = (RadioButton) findViewById(R.id.radio_fuel);
    rb.setOnCheckedChangeListener(rbListener);
  }

  private OnCheckedChangeListener rbListener = new OnCheckedChangeListener() {
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
      if (isChecked) {
        button.setBackgroundColor(0x444444);
        button.setTextSize(24);
        button.setHighlightColor(0x444444);
      } else {
        button.setBackgroundColor(0x444444);
        button.setTextSize(16);
        button.setHighlightColor(0x444444);
      }
    }
  };
}

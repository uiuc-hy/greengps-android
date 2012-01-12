package edu.illinois.greengps.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.greengps.R;
import edu.illinois.greengps.db.DatabaseHelper;

public class FusionSuite extends Activity {

	private ArrayAdapter<String> mLogAdapter;
	private ListView mLogView;
 
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    // Set up the window layout
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.main);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

    // Set up the custom title
    TextView mTitle = (TextView) findViewById(R.id.title_left_text);
    mTitle.setText(R.string.app_name);
    mTitle = (TextView) findViewById(R.id.title_right_text);
    mTitle.setText(FusionSuiteSingleton.start_time);
    
    // Setup singleton and related displays
    FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
    singleton.setFusionSuiteActivity(this, mHandler);
    singleton.setFusionSuiteDBHelper(new DatabaseHelper(getApplicationContext()));

    setupLogFile(singleton);
    setupLogPanel();
    setupButtons(singleton);
  }
  
  private void setupLogFile(FusionSuiteSingleton singleton) {
	if (!singleton.persistent_log) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File path = getExternalFilesDir(null);
			File file = new File(path, "FusionSuite"+System.currentTimeMillis()+".log");
			try {
				singleton.log_file_output_stream = new FileOutputStream(file);
	            singleton.persistent_log = true;
			} catch (IOException e) {
			}
		}
	}
  }
  
  private void setupLogPanel() {
	// Initialize the array adapter for the conversation thread
	mLogAdapter = new MyArrayAdapter(this, R.layout.message);
	mLogView = (ListView) findViewById(R.id.log);
	mLogView.setAdapter(mLogAdapter);
  }

  private void setupButtons(FusionSuiteSingleton singleton) {
	Button b = (Button) findViewById(R.id.startstop);
	b.setOnClickListener(startStopListener);
	    
	if (singleton.running) {
		b.setText("Stop");
	} else { 
		b.setText("Start");
	}
	    
	Button c = (Button) findViewById(R.id.clear);
	c.setOnClickListener(clearListener);
	    
	if (singleton.first_start) { // allow viewing the gui for debuggin
		singleton.first_start = false;
		b.performClick(); // start up the singleton activity and exit view
		finish();
	}
  }

  private OnClickListener startStopListener = new OnClickListener() {
    public void onClick(View v) {
      FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
      if (singleton.running) {
        singleton.running = false;
        ((Button)v).setText("Start");
        singleton.stopServices();
      } else {
        singleton.running = true;
        ((Button)v).setText("Stop");
        singleton.startServices();
      }
    }
  };
  
  private OnClickListener clearListener = new OnClickListener() {
	  public void onClick(View v) {
		  new AlertDialog.Builder(FusionSuite.this)
		  .setMessage("Do you want to clear all samples from the database?")
		  .setIcon(android.R.drawable.ic_dialog_alert)
		  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		  public void onClick(DialogInterface dialog, int whichButton) {
			  FusionSuiteSingleton singleton = FusionSuiteSingleton.getInstance();
			  singleton.clearSamples();
		  }})
		  .setNegativeButton(android.R.string.no, null).show();
	  }
  };

  public String getTag() {
    //return "DRVN"; 
    return "3";
  }

  public void display(String msg) {
	  mLogAdapter.add(msg);
	  mLogAdapter.notifyDataSetChanged();
  }
  
  private final Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
    	  String log_info = (String) msg.obj;
    	  android.util.Log.i("FusionSuiteLog", log_info);
    	  display(log_info);
      }
  };

  static final int NAVIGATE = 1;

  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, NAVIGATE, 0, "Navigate");
    return true;
  }
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case NAVIGATE:
        Intent navigateIntent = new Intent(this, GreenGPS.class);
        try {
        startActivity(navigateIntent);
        } catch (Exception e) {
          display(e.toString());
        }
      return true;
    }
    return false;
  }
  
  private class MyArrayAdapter extends ArrayAdapter<String> {
	  private ArrayList <String> messages;

	  public MyArrayAdapter(Activity context, int textViewResourceId) {
		  super(context, textViewResourceId);
		  this.messages = new ArrayList <String>();
	  }
		
	  public void add(String message) {
		  messages.add(message);
		  if (messages.size() > FusionSuiteSingleton.max_log_size) {
			  messages.remove(0);
			  notifyDataSetChanged();
		  }
	  }
		
	  public String getItem(int position) {
		  return messages.get(position);
	  }
	  
	  public long getItemId(int position) {
			return position;
		}
	  
	  public int getCount() {
		  return messages.size();
	  }
		
	  public void clear() {
		  messages.clear();
		  notifyDataSetChanged();
	  }
	  
	  public View getView(int position, View convertView, ViewGroup parent) {
		  View v = convertView;
          
          if (v == null) {
        	  LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	  v = vi.inflate(R.layout.message, null);
          }
          
          String msg = messages.get(position);
          
          if (msg!=null) {
        	  TextView tv = (TextView) v.findViewById(R.id.message);
        	  tv.setText(msg);
          }
          
          return v;
	  }

  }

}

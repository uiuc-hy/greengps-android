package edu.illinois.greengps.stability;

import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SystemStability extends Activity implements OnClickListener {

	private Button button;
	AlarmManager mgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
        mgr=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
		// Always begin with stop service. Because unfortunately, I don't know a method
		// to determine whether the alarm receiver has been registered or not.
        button.setText("Stop Service");
        
        Button button_r = (Button) findViewById(R.id.button_r);
        button_r.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.d("AppService", "Trying to reboot NOW");
				try {
					Runtime.getRuntime().exec("su -c reboot");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    
        });
    }

	@Override
	public void onClick(View v) {
		if (button.getText().equals("Stop Service")) {	
			
			Log.d("AppService", "Canceling Receiver");
			Intent i=new Intent(getApplicationContext(), OnAlarmReceiver.class);
			PendingIntent pi=PendingIntent.getBroadcast(getApplicationContext(), 0,
			                                              i, 0);
			mgr.cancel(pi);
			button.setText("Start Service");
		}
		else {
			Intent i=new Intent(getApplicationContext(), OnAlarmReceiver.class);
			PendingIntent pi=PendingIntent.getBroadcast(getApplicationContext(), 0,
			                                              i, 0);
			
			long trigger_time = System.currentTimeMillis()
					+ (24-Calendar.getInstance().get(Calendar.HOUR_OF_DAY))*3600*1000;
			
			mgr.setRepeating(AlarmManager.RTC_WAKEUP,
//					SystemClock.elapsedRealtime()+60000,
//					System.currentTimeMillis()+60000,
					trigger_time, 
					AlarmManager.INTERVAL_DAY, pi);
			
			Log.d("AppService", "Current time: "+System.currentTimeMillis());
			Log.d("AppService", "Hour of day: "+Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
			Log.d("AppService", "Millis to wait: "+(24-Calendar.getInstance().get(Calendar.HOUR_OF_DAY))*3600*1000);
			Log.d("AppService", "Scheduled trigger time: " + trigger_time);
			button.setText("Stop Service");
		}
	}
}

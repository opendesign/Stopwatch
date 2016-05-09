package com.psrivastava.stopwatch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StopwatchActivity extends Activity {

	private static final String TAG = "StopwatchActivity";

	private Chronometer mChronometer;
	private LinearLayout buttonContainer;
	private ImageButton mStartButton, mPauseButton, mStopButton;
	
	private int mLapCount = 0;	
	private Boolean mChronoPaused = false;
	
	private IChronometerService mService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buttonContainer = (LinearLayout) findViewById(R.id.llButtonContainer);

		mChronometer = (Chronometer) findViewById(R.id.chronometer);

		mStartButton = (ImageButton) findViewById(R.id.bStart);
		mStartButton.setOnClickListener(startListener);

		mPauseButton = (ImageButton) findViewById(R.id.bPause);
		mPauseButton.setOnClickListener(pauseListener);

		mStopButton = (ImageButton) findViewById(R.id.bStop);
		mStopButton.setOnClickListener(stopListener);

		bindService(new Intent(this, ChronometerService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IChronometerService.Stub.asInterface(service);
			Log.v(TAG, "Service Connected");
			try {
				if (mService.isRunning()) {
					buttonContainer.setVisibility(View.VISIBLE);
					mStartButton.setImageResource(R.drawable.split);
					mChronoPaused = false;
				}
			} catch (RemoteException e) {
				Log.e(TAG, "Connection to service failed.", e);
			}
		}
	};
	
	View.OnClickListener startListener = new OnClickListener() {
		public void onClick(View v) {
			try {
				if (mChronoPaused) {
					Log.v(TAG, "start-chrono was paused");
					mChronometer.resume();
				} 
				else if (!mService.isRunning()) {
					Log.v(TAG, "start-chrono was stopped");
					mChronometer.start();
				} 
				else if (!mChronoPaused) {
					Log.v(TAG, "split button pressed");
					LinearLayout history = (LinearLayout) findViewById(R.id.llLaps);
					TextView lap = new TextView(getApplicationContext());
					mLapCount++;
					lap.setText(mLapCount
							+ "."
							+ timeFormat(mService.getTime()));
					history.addView(lap, 0);
				}
		
				buttonContainer.setVisibility(View.VISIBLE);
				mStartButton.setImageResource(R.drawable.split);
				mChronoPaused = false;
			} catch (RemoteException e) {
				Log.e(TAG, "Connection to service failed.", e);
			}
		}

		private String timeFormat(long l) {
			int minutes;
			float seconds;
			int milliseconds;
			String mins;
			String secs;

			float time = (float) l / 1000;

			minutes = (int) (time / 60);
			seconds = (time % 60);
			milliseconds = (int) (((int) l % 1000) / 100);

			if (minutes < 10) {
				mins = "0" + minutes;
			} else {
				mins = "" + minutes;
			}

			if (seconds < 10) {
				secs = "0" + (int) seconds;
			} else {
				secs = "" + (int) seconds;
			}

			return "\t\t\t" + mins + ":" + secs + "." + milliseconds;
		}
	};

	View.OnClickListener pauseListener = new OnClickListener() {
		public void onClick(View v) {
			if (!mChronoPaused) {
				Log.v(TAG, "pause");
				mChronometer.pause();
				mChronoPaused = true;
				mStartButton.setImageResource(R.drawable.start);
			}
		}
	};

	View.OnClickListener stopListener = new OnClickListener() {
		public void onClick(View v) {
			Log.v(TAG, "stop");
			buttonContainer.setVisibility(View.INVISIBLE);
			mChronometer.stop();
			LinearLayout history = (LinearLayout) findViewById(R.id.llLaps);
			history.removeAllViews();
			mStartButton.setImageResource(R.drawable.start);
			mChronoPaused = false;
			mLapCount = 0;
		}
	};
}
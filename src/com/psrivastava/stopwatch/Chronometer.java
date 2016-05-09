package com.psrivastava.stopwatch;

import java.text.DecimalFormat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/* Gets time from service and displays it.
 * Original source from Antonis Balasas
 * https://github.com/antoniom/Millisecond-Chronometer
 */
public class Chronometer extends TextView {
	private static final String TAG = "Chronometer";
	
	private boolean mRunning;

	private static final int TICK_WHAT = 2;
	
	IChronometerService mService;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			stopDisplay();
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IChronometerService.Stub.asInterface(service);
			Log.v(TAG, "Service Connected");
			updateText();
			startDisplay();
		}
	};

	public Chronometer(Context context) {
		this(context, null, 0);
	}

	public Chronometer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Chronometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		context.getApplicationContext().bindService(new Intent(context, ChronometerService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	public void start() {
		try {
			mService.start();
		} catch (RemoteException e) {
			Log.e(TAG, "Connection to service failed.", e);
		}
		startDisplay();
	}
	
	public void pause() {
		try {
			mService.pause();
		} catch (RemoteException e) {
			Log.e(TAG, "Connection to service failed.", e);
		}
		stopDisplay();
		updateText();
	}
	
	public void resume() {
		try {
			mService.resume();
		} catch (RemoteException e) {
			Log.e(TAG, "Connection to service failed.", e);
		}
		startDisplay();
	}

	public void stop() {
		try {
			mService.stop();
		} catch (RemoteException e) {
			Log.e(TAG, "Connection to service failed.", e);
		}
		stopDisplay();
		updateText();
	}

	@Override
	protected void onDetachedFromWindow() {
		stopDisplay();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if (visibility == VISIBLE) {
			startDisplay();
		}
		else {
			stopDisplay();
		}		
	}

	/**
	 * Display elapsed time.
	 */
	private synchronized void updateText() {
		
		try {
			long timeElapsed;
				timeElapsed = mService.getTime();
	
			DecimalFormat df = new DecimalFormat("00");
	
			int hours = (int) (timeElapsed / (3600 * 1000));
			int remaining = (int) (timeElapsed % (3600 * 1000));
	
			int minutes = (int) (remaining / (60 * 1000));
			remaining = (int) (remaining % (60 * 1000));
	
			int seconds = (int) (remaining / 1000);
			remaining = (int) (remaining % (1000));
	
			int milliseconds = (int) (((int) timeElapsed % 1000) / 100);
	
			String text = "";
	
			if (hours > 0) {
				text += df.format(hours) + ":";
			}
	
			text += df.format(minutes) + ":";
			text += df.format(seconds) + ".";
			text += Integer.toString(milliseconds);
	
			setText(text);
		} catch (RemoteException e) {
			Log.e(TAG, "Connection to service failed.", e);
		}
	}
	
	private void startDisplay() {
		try {
			if (!mRunning && mService != null && mService.isRunning()) {
				mHandler.dispatchMessage(Message.obtain(mHandler, TICK_WHAT));	
				mRunning = true;
			}
		} catch (RemoteException e) {
			Log.e(TAG, "Connection to service failed.", e);
		}
	}
	
	private void stopDisplay() {
		mHandler.removeMessages(TICK_WHAT);			
		mRunning = false;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message m) {
			sendMessageDelayed(Message.obtain(this, TICK_WHAT), 100);
			updateText();
		}
	};
}
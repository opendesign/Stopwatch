package com.psrivastava.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

/**
 * Takes the time, makes sure this works even when the main activity is closed.
 */
public class ChronometerService extends Service {

	private static final String TAG = "ChronometerService";
	
	private long mBaseTime = SystemClock.elapsedRealtime();
	private long mPausedTime = 0;
	private boolean mRunning = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private IBinder mBinder = new IChronometerService.Stub() {
		
		/**
		 * Starts the timer.
		 */
		public void start() throws RemoteException {
			mBaseTime = SystemClock.elapsedRealtime();
			mPausedTime = 0;
			mRunning = true;
		}
		
		/**
		 * Pauses the timer, to start again use {@link #resume()}
		 */
		public void pause() throws RemoteException {
			mPausedTime = SystemClock.elapsedRealtime();
			mRunning = false;
		}
		
		/**
		 * Continues running after pausing the timer with {@link #pause()}
		 */
		public void resume() throws RemoteException { 
			mBaseTime = SystemClock.elapsedRealtime() - (mPausedTime - mBaseTime);
			mPausedTime = 0;
			mRunning = true;
		}

		/**
		 * Resets timer to zero.
		 */
		public void stop() throws RemoteException {
			mBaseTime = 0;
			mPausedTime = 0;
			mRunning = false;
		}

		/**
		 * Has neither {@link #pause()} nor {@link #stop()} been called since 
		 * last calling {@link #start()}?
		 * 
		 * @return True if the timer is running
		 */
		public boolean isRunning() throws RemoteException {
			return mRunning;
		}
		
		/**
		 * Gets the time that should be displayed on screen.
		 * 
		 * @return Time for display in milliseconds
		 */
		public long getTime() throws RemoteException {
			return (mRunning) 
					? SystemClock.elapsedRealtime() - mBaseTime 
					: (mPausedTime != 0) 
							? mPausedTime - mBaseTime 
							: 0;
		}
	};
}
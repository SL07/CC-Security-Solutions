package com.example.audiodetection;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/* constants */
	private static final int POLL_INTERVAL = 300;

	/** running state **/
	private boolean mRunning = false;

	/** config state **/
	private int mThreshold;

	private PowerManager.WakeLock mWakeLock;

	private Handler mHandler = new Handler();

	/* References to view elements */
	private TextView mStatusView;
	private SoundLevelView mDisplay;

	/* data source */
	private SoundMeter mSensor;

	private RecordSound mRecorder;



	/****************** Define runnable thread again and again detect noise *********/

	private Runnable mSleepTask = new Runnable() {
		public void run() {
			//Log.i("Noise", "runnable mSleepTask");

			start();
		}
	};

	// Create runnable thread to Monitor Voice
	private Runnable mPollTask = new Runnable() {
		public void run() {

			double amp = mSensor.getAmplitude();
			//Log.i("Noise", "runnable mPollTask");
			updateDisplay("Monitoring Voice...", amp);

			if ((amp > mThreshold)) {
				updateDisplay("Recording...", amp);
				callForHelp();
				//Log.i("Noise", "==== onCreate ===");

			}

			// Runnable(mPollTask) will again execute after POLL_INTERVAL
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}
	};

	private Runnable mRecordTask = new Runnable(){
		public void run(){

			Log.v("MainActivity", "Enter mRecordTask");
			long start_time, current_time, time_elapsed = 0;

			Log.v("MainActivity", "mRecordTask.start()");
			mRecorder.start();

			start_time = System.currentTimeMillis();

			Log.v("MainActivity", "10s Start");
			while(time_elapsed <= 10000){
				current_time = System.currentTimeMillis();
				time_elapsed = current_time - start_time;
			}

			Log.v("MainActivity", "Finish recording");
			mRecorder.stop();

			Toast.makeText(getApplicationContext(), "Finish Reccording", Toast.LENGTH_SHORT).show();

			Log.v("MainActivity", "Return polling");
			start();
		}
	};

	/*********************************************************/

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Defined SoundLevelView in main.xml file
		setContentView(R.layout.activity_main);
		mStatusView = (TextView) findViewById(R.id.status);

		// Used to record voice
		mSensor = new SoundMeter();
		mDisplay = (SoundLevelView) findViewById(R.id.volume);

		mRecorder = new RecordSound();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
	}


	@Override
	public void onResume() {
		super.onResume();
		//Log.i("Noise", "==== onResume ===");

		initializeApplicationConstants();
		mDisplay.setLevel(0, mThreshold);

		if (!mRunning) {
			mRunning = true;
			start();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		// Log.i("Noise", "==== onStop ===");

		//Stop noise monitoring
		stop();

	}

	private void start() {
		//Log.i("Noise", "==== start ===");

		mSensor.start();
		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}

		//Noise monitoring start
		// Runnable(mPollTask) will execute after POLL_INTERVAL
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void stop() {
		Log.i("Noise", "==== Stop Noise Monitoring===");
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSensor.stop();
		mDisplay.setLevel(0,0);
		updateDisplay("stopped...", 0.0);
		mRunning = false;

	}


	private void initializeApplicationConstants() {
		// Set Noise Threshold
		mThreshold = 8;

	}

	private void updateDisplay(String status, double signalEMA) {
		mStatusView.setText(status);
		mDisplay.setLevel((int)signalEMA, mThreshold);
	}


	private void callForHelp() {

		//stop();

		// Show alert when noise thersold crossed

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}

		Log.v("MainActivity", "Stop mSensor");
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSensor.stop();

		Log.v("MainActivity", "call mRecordTask");
		mHandler.post(mRecordTask);
	}

};
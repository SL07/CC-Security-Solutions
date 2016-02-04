package com.example.audiodetection;

import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class RecordSound {
	
	private MediaRecorder myAudioRecorder;
	private String outputFile = null;
	
	public void start() {
		Log.v("RecordSound", "Entering start()");
		
		outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/myrecording.3gp";

		myAudioRecorder = new MediaRecorder();
		myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		myAudioRecorder.setOutputFile(outputFile);
		
		try {
			Log.v("RecordSound", "prepare()");
			myAudioRecorder.prepare();
			Log.v("RecordSound", "start()");
			myAudioRecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("RecordSound", "IllegalStateException", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("RecordSound", "IOException", e);
		}
	}
	
	public void stop() {
		myAudioRecorder.stop();
		myAudioRecorder.release();
		myAudioRecorder  = null;
	}

}

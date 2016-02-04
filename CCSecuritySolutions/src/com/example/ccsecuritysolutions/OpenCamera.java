package com.example.ccsecuritysolutions;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/*import com.jwetherell.motion_detection.MotionDetectionActivity.DetectionThread;
import com.jwetherell.motion_detection.MotionDetectionActivity.SavePhotoTask;
import com.jwetherell.motion_detection.data.Preferences;
import com.jwetherell.motion_detection.detection.IMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;*/



public class OpenCamera extends Activity {
	private static final int MIN_FRAME_RATE = 1500; 
   private Camera mCamera;
   private Preview mPreview;
   private ImageView pic;
   private Uri fileUri = null;
   private File pictureFile = null;
   private static int photo_name = 1;
   private static int total_photo_count = 1;
   private static int photo_delete_count = 1;
   private static int max_photo = 2;
   
   private static boolean motion_detected = false;
   private static boolean message_sent = true;
   public static final int MEDIA_TYPE_IMAGE = 1;
   public long startTime, endTime, duration = 0;
   private static File photo_buffer[] = new File[1395];
   private static byte[] photo_bytes[] = new byte[11][];
   private static byte[] yuv_photo_bytes[] = new byte[11][];
   private File currentFile = null;
   MyTimerTask my_task;
   Timer my_timer;
   int i,j = 0;
   int pic_to_send = 1;
   int saved_photos = 0;
   boolean start_comparing = false;
   private static File mediaStorageDir = new File("sdcard/DCIM/CCSecuritySolutionsBuffer");
   private static File mediaStorageDir2 = new File("sdcard/DCIM/CCSecuritySolutionsCapture");
   Size mSize = null;
   
   
	/* constants */
	private static final int POLL_INTERVAL = 300;

	/** config state **/
	private int mThreshold = 11;
	private Handler mHandler = new Handler();
	public boolean audioFlag = false; 
	/* data source */
	private SoundMeter mSensor;
	
	private static IMotionDetection detector = null;

	// Create runnable thread to Monitor Voice
	private Runnable mPollTask = new Runnable() {
		public void run() {

			double amp = mSensor.getAmplitude();

			if ((amp > mThreshold)) {
				callForHelp();
			}
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}
	};
   
   
   public static Camera isCameraAvailiable(){
      Camera object = null;
      try {
         object = Camera.open(); 
      }
      catch (Exception e){
      }
      return object; 
   }
   
   public void initialiseCamera(){
	   Parameters parameters = mCamera.getParameters();
	      List<Size> sizes = parameters.getSupportedPictureSizes();
	      
	      // Iterate through all available resolutions and choose one.
	      // The chosen resolution will be stored in mSize.
	      mSize = sizes.get(Integer.valueOf((sizes.size()-1)/2));;
	      int height; 
	      int width; 
	      for (Size size : sizes) {
	    	  height = size.height;
	          width = size.width;
	    	  Log.i("Open Camera OnCreate", "Available resolution: " + size.width + " " + size.height);
	    	  if (width == 640 && height == 480) {
	    		  mSize = size;
	    		  break;
	    	  }
	      }

	      parameters.setPictureSize(mSize.width, mSize.height);
	      mCamera.setParameters(parameters);
	      
	      height = mSize.height;
	      width = mSize.width;
	      Toast.makeText(getApplicationContext(), Integer.toString(height) + "," + Integer.toString(width), Toast.LENGTH_SHORT).show();	
	      //parameters.setPreviewSize(size2.width, size2.height);
	      
	      //start the camera preview
	      mPreview = new Preview(this, mCamera);
	      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	      preview.addView(mPreview);
	   
   }
   
/*   @Override
   public void onBackPressed() {
   }*/

   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.camera);
      
      mCamera = isCameraAvailiable();
      initialiseCamera();
      
      mSensor = new SoundMeter();
      
      detector = new RgbMotionDetection();
      
      my_timer = new Timer();
      my_task = new MyTimerTask();
      my_timer.schedule(my_task, 3000, MIN_FRAME_RATE);
      
      Button disarm_device = (Button) findViewById(R.id.disarm_device);
      final TextView passwordTextView = (TextView) findViewById(R.id.editText1);

      disarm_device.setOnClickListener(new OnClickListener() {
    	  public void onClick(View view0) {
    		  String password  = passwordTextView.getText().toString();
    		  final Context currentContext = getBaseContext();

    		  if(PreferenceObject.checkPassword(currentContext,password) == true){
    			  
    			  Intent intent = new Intent(currentContext, Home.class);
    			  startActivity(intent);
    		  }
    		  else{
    			  Toast.makeText(currentContext, "Incorrect Password, Please Try Again", Toast.LENGTH_SHORT).show();
    		  }
    	  }
      });
   }
   
   public class MyTimerTask extends TimerTask {
	   
	   public void run() {
		   startTime = System.currentTimeMillis();
		   mCamera.takePicture(null, null, capturedIt);
		   mCamera.setPreviewCallback(previewCallback);

		   new CompareAsyncTask().execute();
		   new SendDE2AsyncTask().execute();
	   }
   }
   
   class CapturePhotosAsyncTask extends AsyncTask<Void, Void,Void> {
	   @Override
	   protected Void doInBackground(Void... params) {
		   mCamera.takePicture(null, null, capturedIt);
		   mCamera.setPreviewCallback(previewCallback);
		   return null;
	   }
   }
   class SendDE2AsyncTask extends AsyncTask<Void, Void,Void> {
	   @Override
	   protected Void doInBackground(Void... params) {
		   File directory = new File(mediaStorageDir.getPath());
		   File[] contents = directory.listFiles();
		   
		   
		   //String photoDelete = (mediaStorageDir.getPath() + File.separator + "IMG_" + Integer.toString(pic_to_send) + ".jpg");
		   //File delFile = new File(photoDelete);
		   if (contents != null && contents.length >= 3 && message_sent && j < contents.length){
			   currentFile = contents[j];
			   Log.i("sendDe2", "" + currentFile.toString());
			   // send photo first before deleting
			   //connectMiddleMan();
			   Socket s = null;
				String ip = "206.12.53.225";
				Integer port = 50002;

				try {
					s = new Socket(ip, port);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				MyApplication myApp = (MyApplication) OpenCamera.this.getApplication();
				myApp.sock = s;
				Log.i("MiddleMan", "connected");
			   //setImageBit(photoDelete);
				message_sent = false; 
			   while(sendphoto());
			   //contents[0].delete();
			   j++;
			   message_sent = true;

			   photo_delete_count++;
			   //Toast.makeText(OpenCamera.this, photoDelete +" deleted.", Toast.LENGTH_LONG).show();
			   //Log.d("cameratest", photoDelete + " deleted");

			   final Context currentContext = getBaseContext();
			   //makes a new email with the users credentials and sends the user a msg with a photo
			   final GmailSender sender = new GmailSender(PreferenceObject.getEmail(currentContext), PreferenceObject.getEmailPassword(currentContext));
			   try {   
				   //attach a photo
				   sender.addAttachment(currentFile.toString());
				   //make the body of the program and send it to the user
				   sender.sendMail("Alert from CCSecurity Solutions", "We have detected an intruder in your home!",  PreferenceObject.getEmail(currentContext),  PreferenceObject.getEmail(currentContext));   
			   } catch (Exception e) {   
				   Log.e("SendMail", e.getMessage(), e);   
			   }
			   
		   }
		   else{
			   pic_to_send++;
		   }
		   


		   return null;
	   }
	   /*protected void onPostExecute(Socket s) {
			MyApplication myApp = (MyApplication) OpenCamera.this.getApplication();
			myApp.sock = s;
			Log.i("MiddleMan", "connected");
		}*/
		
	   /*private void connectMiddleMan(){
		   Socket s = null;
			String ip = "206.12.53.225";
			Integer port = 50002;

			try {
				s = new Socket(ip, port);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	   }*/
	   
	   
	   private boolean sendphoto(){
			MyApplication app = (MyApplication) getApplication();
			Log.i("current","file " + currentFile.toString());
			byte[] imageByte =  setImageBit(currentFile);
			
			// Create an array of bytes.  First byte will be the
			// message length, and the next ones will be the message
			
			byte buf[] = new byte[imageByte.length + 4];
			int length = imageByte.length;

			
			buf[3] = (byte) length;
			buf[2] = (byte) (length >>> 8);
			buf[1] = (byte) (length >>> 16);
			buf[0] = (byte) (length >>> 24);
			
			//System.out.printf("%x ", imageByte);
			System.arraycopy(imageByte, 0, buf, 4, imageByte.length);			

			// Now send through the output stream of the socket
			
			OutputStream out;
			try {
				out = app.sock.getOutputStream();
				try {
					out.write(buf, 0, imageByte.length + 4);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("sendphoto", "sending");
			TCPReceiveMsg();
			return false;

	   }


	   public void TCPReceiveMsg(){

		   MyApplication app = (MyApplication) getApplication();
		   if (app.sock != null && app.sock.isConnected() && !app.sock.isClosed()) {

			   try {
				   InputStream in = app.sock.getInputStream();

				   // See if any bytes are available from the Middleman

				   int bytes_avail = 0;
				   

				   while (bytes_avail <= 0) {
					   bytes_avail = in.available();
				   } 
				   Log.i("RecieveDe2", "msg recieved");
				   // If so, read them in and create a string

				   byte buf[] = new byte[bytes_avail];
				   in.read(buf);

				   final String s = new String(buf, 0,bytes_avail, "US-ASCII");
				   Log.i("confirm: ","" + s);
				   
				   if(s.equals("Picture received")){
					   Log.i("confirm: ","" + s);
					   
				   }
			   } catch (IOException e) {
				   e.printStackTrace();
			   }

		   }
	   }
	   
		private byte[] setImageBit(File file){
	        //String path = android.os.Environment.DIRECTORY_DCIM + File.separator + "Camera";
			//String path = "/sdcard/DCIM/CCSecuritySolutionsBuffer";
	        //File file = new File(path,fileName);
			//File file = new File(filename);
	        //if(file.exists()){
		        String item = file.toString();
		        Bitmap image = BitmapFactory.decodeFile(item);  		        
		        Bitmap bitmap = Bitmap.createScaledBitmap (image, 640, 480, false);		        
		        ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
		       
		        byte[] byteArray = stream.toByteArray();

		        return byteArray;
	       
		}
   }

   class CompareAsyncTask extends AsyncTask<Void, Void,Void> {

	   @Override
	   protected Void doInBackground(Void... params) {
		   // Only save to the Buffer Folder if detected motion

		   
		   // call motion detection function
		   if(i == 0 ){
			   if(motionDetection(yuv_photo_bytes[10], mSize.width, mSize.height)){
				   motion_detected = true;
			   }else{
				   motion_detected = false;
			   }
		   }else{
			   if(motionDetection(yuv_photo_bytes[i-1], mSize.width, mSize.height)){
				   motion_detected = true;
			   }else{
				   motion_detected = false;
			   }
		   }
		   //if(motion_detected == true){
		  if(motion_detected == true || audioFlag == true){
			   Log.d("asynch task", "motion true");
			   try {
				   FileOutputStream fos;
				   if(i == 0){
					   fos = new FileOutputStream(photo_buffer[10]);
				   		fos.write(photo_bytes[i-1]);
				   }else{   
					   fos = new FileOutputStream(photo_buffer[i-1]);
					   fos.write(photo_bytes[i-1]);
				   }
				   fos.close();
				   //Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();

				   //mCamera.startPreview();
				   
				   if(audioFlag == true){
					   if(saved_photos == 3){
						   audioFlag = false;
					   		saved_photos = 0;
					   }
					   saved_photos++;
				   }

			   } catch (FileNotFoundException e) {

			   } catch (IOException e) {

			   } catch (NullPointerException e) {

			   }
		   }
		   else{
			   Log.d("asynch task", "motion false");
		   }
		   return null;
	   }
   }

   
   public boolean motionDetection(byte[] data, int width, int height){

	   // Previous frame
	   //int[] pre = null;
	   //if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();

	   int[] img = null;
	   if(data != null){
		   img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
	   }
	   // if motion was detected
	   if (img != null && detector.detect(img, width, height)) {
		   return true;
	   }else{
		   return false;
	   }

   }


   public void snapIt(View view){
	  //Preview mPreviewState;
	  //mPreviewState = K_STATE_PREVIEW;
	// Add a listener to the Capture button
	   //startTime = System.currentTimeMillis();
	  // mCamera.takePicture(null, null, capturedIt);
	   //new CompareAsyncTask().execute();    
   }

	private static boolean getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		
	    //File mediaStorageDir = new File("sdcard/DCIM/CCSecuritySolutions2");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("CCSecuritySolutionsBuffer", "failed to create directory");
	            return false;
	        }
	    }
	    
	    if (! mediaStorageDir2.exists()){
	        if (! mediaStorageDir2.mkdirs()){
	            Log.d("CCSecuritySolutionsCapture", "failed to create directory");
	            return false;
	        }
	    }
	    

	    return true;

	}
   

    private PreviewCallback previewCallback = new PreviewCallback() {
    	int cnt = 0;
    	/**
    	 * {@inheritDoc}
    	 */
    	@Override
    	public void onPreviewFrame(byte[] data, Camera cam) {

    		if (data == null) return;
    		Camera.Size size = cam.getParameters().getPreviewSize();
    		if (size == null) return;

    		// if reached end of array, overwrite it starting from beginning  
    		if(i >= 11){
    			//i = 1; 
    		}else if(1 <= i && i < 11){
    			yuv_photo_bytes[i-1] = data;
    			//Toast.makeText(getApplicationContext(), "taken, " + Integer.toString(photo_name), Toast.LENGTH_SHORT).show();
    			//i++;
    			Log.i("MotionPreview", "photo: " + Integer.toString(photo_name));
    		}
    		//mCamera.startPreview();


    	}
    };

    private PictureCallback capturedIt = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			endTime = System.currentTimeMillis();
			duration = (endTime - startTime);
			//mCamera.startPreview();	
			//Toast.makeText(getApplicationContext(), Integer.toString((int) duration), Toast.LENGTH_SHORT).show();	
			String timeStamp;
			File mediaFile, mediaFile2;
			// pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE); 
			if(!getOutputMediaFile()){
				Toast.makeText(getApplicationContext(), "Photo Not Taken", Toast.LENGTH_SHORT).show();
				mCamera.startPreview();
				return;
			}else {
				//
				// Create a media file name
				// Buffer folder
				mediaFile = new File(mediaStorageDir.getPath() + File.separator + 
						"IMG_" + Integer.toString(photo_name) + ".jpg");
				
				// Captured folder
				// mediaFile2 = new File(mediaStorageDir2.getPath() + File.separator + 
				//"IMG_" + Integer.toString(photo_name) + ".jpg");
				
				if(photo_name == 21){
					//Toast.makeText(getApplicationContext(), "done, " + Integer.toString(photo_name), Toast.LENGTH_SHORT).show();
					//my_timer.cancel();
				}
				// if reached end of array, overwrite it starting from beginning  
				if(i >= 10){
					i = 0; 
				}else if(0 <= i && i < 10){
					/*if(i >= 5){
						motion_detected = true;
					}else if(0 <= i && i < 5){
						motion_detected = false;
					}*/
					photo_buffer[i] = mediaFile;
					photo_bytes[i] = data;
					//Toast.makeText(getApplicationContext(), "taken, " + Integer.toString(photo_name), Toast.LENGTH_SHORT).show();
					photo_name++;
					i++;
					Log.i("OnPicTaken", "photo: " + Integer.toString(photo_name));
				}
				
			}
			mCamera.startPreview();

		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
   
   @Override
   protected void onStop() {
	   	super.onStop();
	    if (mCamera != null) {
	    	
	        // Call stopPreview() to stop updating the preview surface.
	        mCamera.stopPreview();
	    
	        // Important: Call release() to release the camera for use by other
	        // applications. Applications should release the camera immediately
	        // during onPause() and re-open() it during onResume()).
	        mCamera.release();
	    
	        mCamera = null;
	        my_timer.cancel();
	        
			mHandler.removeCallbacks(mPollTask);
		
			mSensor.stop();
	        
	    }
   }
   
/*   @Override
   protected void onPause() {
	   	super.onPause();
	    if (mCamera != null) {
	        // Call stopPreview() to stop updating the preview surface.
	        mCamera.stopPreview();
	    
	        // Important: Call release() to release the camera for use by other
	        // applications. Applications should release the camera immediately
	        // during onPause() and re-open() it during onResume()).
	        mCamera.release();
	    
	        mCamera = null;
	        //my_timer.cancel();
	    }
   }*/
   
   @Override
   protected void onResume() {
	   	super.onResume();
	        // Call stopPreview() to stop updating the preview surface.
	        if(mCamera == null){
//	        	mThreshold = 8;
	        	mCamera = isCameraAvailiable();
	            initialiseCamera();
	        	mCamera.startPreview();
	        }

   }
   
   @Override
   protected void onStart() {
	   	super.onStart();
	   	mSensor.start();
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
   }
   
   private void callForHelp() {
	   	Log.i("AudioDetection:", "call for help ");
		audioFlag = true;		
	}
   

}
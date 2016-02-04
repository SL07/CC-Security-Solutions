package com.example.ccsecuritysolutions;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

   private SurfaceHolder mHolder;
   private Camera mCamera;
   private SurfaceView dummy;
   public Preview(Context context,Camera camera) {
      super(context);
      dummy= new SurfaceView(context);
      mCamera = camera;
      mHolder = getHolder();
      mHolder.addCallback(this);
      mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
   }



@Override
   public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
       if (mHolder.getSurface() == null){
           // preview surface does not exist
           return;
         }
       // stop preview before making changes
       try {
           mCamera.stopPreview();
       } catch (Exception e){
         // ignore: tried to stop a non-existent preview
       }

       // set preview size and make any resize, rotate or
       // reformatting changes here

       // start preview with new settings
       try {
    	   //mCamera.setPreviewDisplay(dummy.getHolder());
           mCamera.setPreviewDisplay(mHolder);
           mCamera.startPreview();

       } catch (Exception e){
           
       }
   }

   @Override
   public void surfaceCreated(SurfaceHolder holder) {
      try {
         mCamera.setPreviewDisplay(holder);
         mCamera.startPreview(); 
      } catch (IOException e) {
      }
   }

   @Override
   public void surfaceDestroyed(SurfaceHolder arg0) {
	    // Surface will be destroyed when we return, so stop the preview.
	    if (mCamera != null) {
	        // Call stopPreview() to stop updating the preview surface.
	        mCamera.stopPreview();
	    }
   }

}
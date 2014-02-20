package com.gsdp.opsecurity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.FrameLayout;


public class CameraActivity extends Activity {
	
	private Camera mCamera;    
	private CameraPreview mPreview;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static String siteName;
	File pictureFile;
	TelephonyManager telephonyMgr;
	PictureCallback mPicture;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        // Create an instance of Camera        
      	mCamera = getCameraInstance();
      	Log.d("Henry", "getCameraInstance completed");
      	
     // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        Log.d("Henry", "Preview completed");
        
        telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener listener;

        listener = new PhoneStateListener() {
        	@Override
        	public void onCallStateChanged(int state, String incomingNumber) {
            	if (state == TelephonyManager.CALL_STATE_RINGING) {

            		Log.d("Henry", "Got to takePicture");
            		mCamera.takePicture(null, null, mPicture);
            		Log.d("Henry", "finished takePicture");
        		
            	} // end if ringing
            } // end on call state change
        };
        
        telephonyMgr.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
        Log.d("Henry", "Listener started");
    
		// creates a call back interface which supplies image data from a photo capture.
		mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) { // gets a byte array of the image data from the camera
			Log.d("Henry", "onPictureTaken Started ");
			// returns a file in a directory to write the image data to
			pictureFile = getCameraMediaFile(MEDIA_TYPE_IMAGE);
			Log.d("Henry", "getCameraMediaFile finished");
			if (pictureFile == null){   
				Log.d("Henry", "Error creating media file check storage permissions: ");// if there was an error getting a file to write data to
				return;
			}
		 
			try {
				// creates an output stream to write the image data using the "pictureFile" file
				FileOutputStream fos = new FileOutputStream(pictureFile);            
				fos.write(data);
				fos.close();
				Log.d("Henry", "Picture saved");
			} catch (FileNotFoundException e) {
				Log.d( "Henry", "File not found: problem");// File has not been found
			} catch (IOException e) {
				Log.d("Henry", "Error accessing file: problem");// Error accessing the file
			}
			Log.d("Henry", "onPictureTaken finished");
		} // <-- end of onPictureTaken
		};
		
        
    } // <-- end of onCreate
    

    
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
    	Camera c = null;    
    	try {
			c = Camera.open(); // attempt to get a Camera instance
    	}
    	catch (Exception e){
    		// if camera has not been released
    	}
    	return c;
    }
    
    
    private static File getCameraMediaFile (int type) {
    	// To be safe, you should check that the SDCard is mounted    
    	// using Environment.getExternalStorageState() before doing this.
    	Log.d("Henry", "getCameraMediaFile started ");
    	
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
    			Environment.DIRECTORY_PICTURES), "GDSPPrototype");
    	// This location works best if you want the created images to be shared    
    	// between applications and persist after your app has been uninstalled.
    	
    	// Create the storage directory if it does not exist   
    	if (! mediaStorageDir.exists()){
    		if (! mediaStorageDir.mkdirs()){
    			Log.d("Henry", "failed to create directory");            
    			return null;
    		}
    	}
    	
        
    	// Create a media file name
    	String imageNameStamp = "latestPicture";    
    	File mediaFile;
    	if (type == MEDIA_TYPE_IMAGE){  
    		
    		mediaFile = new File(mediaStorageDir.getPath() + File.separator +
    		"IMG_"+ imageNameStamp + ".jpg");
    		
    		
    		if(mediaFile.exists()) {
    			mediaFile.delete();
    			Log.d("Henry", "Old picture deleted");
    		}
    		
    		
    	} else {
    		return null;
    	}
    	
    	return mediaFile;
    }
    
    
    
} // end of MainActivity
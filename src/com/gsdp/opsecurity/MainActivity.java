package com.gsdp.opsecurity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends ActionBarActivity {

	private Camera mCamera;    
	private CameraPreview mPreview;
	public static final int MEDIA_TYPE_IMAGE = 1;
	File pictureFile;
	TelephonyManager telephonyMgr;
	PictureCallback mPicture;
	private Button start, stop;
	private FrameLayout prev;
	PhoneStateListener listener;
	static String fullPicturePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/GDSPPrototype/IMG_latestPicture.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start = (Button)findViewById(R.id.start);
		stop = (Button)findViewById(R.id.stop);

		start.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				start.setEnabled(false);
				stop.setEnabled(true);
				startSecurity();
			}
		});

		stop.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				start.setEnabled(true);
				stop.setEnabled(false);
				stopSecurity();
			}
		});

		// Create an instance of Camera        
		mCamera = getCameraInstance();
		Log.d("Henry", "getCameraInstance completed");

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		Log.d("Henry", "Preview completed");

		telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


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
				encodedSend();
				Log.d("Henry", "encodedSend finished");
			} // <-- end of onPictureTaken
		};

	}// End of on create

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//Handle pressed on the action bar items
		switch (item.getItemId()){
		case R.id.action_settings:
			Intent i=new Intent(this, UserSettings.class);
			startActivityForResult(i, 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	} // end onOptionItemSelected


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
	}  // end getCameraInstance


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
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ imageNameStamp + ".jpg");
			if(mediaFile.exists()) {
				mediaFile.delete();
				Log.d("Henry", "Old picture deleted");
			}
		} else {
			return null;
		}
		
		return mediaFile;
	} // end getCameraMediaFile 

	public void encodedSend() {

		// create byte array of image

		Log.d("encodedSend", "Picture path" + fullPicturePath);

		Bitmap bm = BitmapFactory.decodeFile(fullPicturePath);
		Log.v("encodedSend", "Bitmap encoded");

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		Log.v("encodedSend", "AFTER BYTEARRAYOUTPUT");


		bm.compress(Bitmap.CompressFormat.JPEG, 42, baos);  //needs to be variable  
		bm.recycle();
		Log.v("encodedSend", "AFTER COMPRESSFILE");


		byte[] byteArrayImage = baos.toByteArray(); 
		Log.v("encodedSend", "AFTER TOBYTEARRAY");

		// encode to Base64 string
		String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
		Log.v("encodedSend", "AFTER ENCODETOSTRING");

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://bamurdoch.com/GSDP2/echostr.php");
		Log.v("encodedSend", "AFTER HTTPCLIENT");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uImg", encodedImage));
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String siteName = prefs.getString("pref_siteName", "");
			Log.d("Henry", siteName);
			nameValuePairs.add(new BasicNameValuePair("sitename", siteName));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			ResponseHandler<String> responseHandler=new BasicResponseHandler();
			String responseBody = httpclient.execute(httppost, responseHandler);

			//Just display the response back
			Log.v("encodedSend", "Response: "+responseBody);

		} catch (ClientProtocolException e) {
			Log.d("Henry","ClientProtocolException");
		} catch (IOException e) {
			Log.d("Henry","IOException");
		}

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}


	public void startSecurity(){
		prev = (FrameLayout)findViewById(R.id.camera_preview);
		prev.setVisibility(View.VISIBLE);

		telephonyMgr.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
		Log.d("Henry", "Listener started");
	}

	public void stopSecurity(){
		prev = (FrameLayout)findViewById(R.id.camera_preview);
		prev.setVisibility(View.INVISIBLE);

		telephonyMgr.listen(listener,PhoneStateListener.LISTEN_NONE);

	}
}

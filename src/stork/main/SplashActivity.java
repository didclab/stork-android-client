package stork.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends FragmentActivity implements TaskFragment.TaskCallbacks {
	private static final String TAG = "SplashActivity";
	private TaskFragment mTaskFragment;
	private static ProgressDialog mLoading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// we set the color model on the activity to make sure that the gradients are smooth
		// this is optional
		getWindow().setFormat(PixelFormat.RGBA_8888);

		// we want the activity to occupy full screen including the Android system notification bar
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);

		try{
			FragmentManager fm = getSupportFragmentManager();
			mTaskFragment = (TaskFragment) fm.findFragmentByTag("taskfrag");
			
			if(mTaskFragment == null){	
				mTaskFragment = new TaskFragment();
				 fm.beginTransaction().add(mTaskFragment, "taskfrag").commit();
		            // load the data from the web
		         mTaskFragment.executeBackgroundTask();   
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		}	
		
	}//end of onCreate
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "+++++++onDestroy called+++++Outside_If");
		if(mLoading != null){
			mLoading.dismiss();
			Log.v(TAG, "+++++++onDestroy called+++++Inside_If");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "+++++++onPause called+++++Outside If");
		if(mLoading != null){
			Log.v(TAG, "+++++++onPause called+++++Inside If");
			mLoading.dismiss();
		}
		Log.v(TAG, "+++++++onPause called+++++");
	}
	
	public void instantiateDialog(){
		Log.v(TAG, "+++++++instantiateDialog called");
		mLoading = new ProgressDialog(SplashActivity.this);
	}
	
	@Override
	public void onPreExecute() {
		Log.v(TAG, "+++++++onPreexecute called");
		mLoading.setMessage("Initializing...");
		mLoading.show();
	}

	@Override
	public void onProgressUpdate(int percent) {
		Log.v(TAG, "+++++++onProgressUpdate called with value = "+percent);
		switch(percent){
		case 0:
			mLoading.setMessage("Checking internet connection...");
			break;
		case 1:
			mLoading.setMessage("Network unavailable");
			finish();
		}
	}

	@Override
	public void onCancelled() {
		Log.v(TAG, "+++++++onCancelled called");
		mLoading.dismiss();
		mTaskFragment.onDestroy();
	}

	@Override
	public void onPostExecute() {
		Log.v(TAG, "+++++++onPostExecute called");
		mLoading.dismiss();
		Intent intent = new Intent(SplashActivity.this, StartupClass.class);
		startActivity(intent);
		finish();
	}
}
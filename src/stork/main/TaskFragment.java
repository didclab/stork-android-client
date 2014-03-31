package stork.main;

import stork.Server;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class TaskFragment extends Fragment {
	
	public static interface TaskCallbacks {
	    void onPreExecute();
	    void onProgressUpdate(int percent);
	    void onCancelled();
	    void onPostExecute();
	    void instantiateDialog();
	  }
	
	private static final String TAG = "TaskFragment.class";
	private TaskCallbacks mCallbacks;
	private SplashTask mTask;
	private FragmentActivity mHostingActivity;

	  /**
	   * Hold a reference to the parent Activity so we can report the
	   * task's current progress and results. The Android framework 
	   * will pass us a reference to the newly created Activity after 
	   * each configuration change.
	   */
	  @Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    mHostingActivity = (FragmentActivity) activity;
	    mCallbacks = (TaskCallbacks) activity;
	    Log.v(TAG, "+++++++onAttach called!");
	  }

	  /**
	   * This method will only be called once when the retained
	   * Fragment is first created.
	   */
	  
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Retain this fragment across configuration changes.
	    setRetainInstance(true);
	    Log.v(TAG, "+++++++onCreate called!");
	  }
	  
	  @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	    mCallbacks.instantiateDialog();
	    executeBackgroundTask();
	    if(mCallbacks != null){
			mCallbacks.onPreExecute();
		}
	    Log.v(TAG, "+++++++onActivity created called!");
	}
	  
	  public void executeBackgroundTask() {
		  try{
			  if(mTask == null){
			    mTask = new SplashTask();
			    mTask.execute();
			  } 
			  
		    }catch(Exception e){
		    	Log.e(getTag(), e.getMessage(), e);
		    }
	  }

	/**
	   * Set the callback to null so we don't accidentally leak the 
	   * Activity instance.
	   */
	  @Override
	  public void onDetach() {
	    super.onDetach();
	    mCallbacks = null;
	  }
	
	
	  
	private class SplashTask extends AsyncTask<Void, Integer, Void>{

		// this is the progress dialog that we will use to show the user that there is 
		// some activity in the background
		//mLoading = new ProgressDialog(SplashActivity.this);

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
					Log.v("AsyncTask", "+++++++doInBackground called++++++");
					Thread.sleep(2000L);
					if(!isNetworkAvailable()){
						publishProgress(1); 
					} else {
						publishProgress(0);
					}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.v("AsyncTask", "+++++++onProgressUpdate called++++++Outside_IF");
			if (mCallbacks != null) {
				Log.v("AsyncTask", "+++++++onProgressUpdate called++++++Inside_IF");
		        mCallbacks.onProgressUpdate(values[0]);
		      }
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.v("AsyncTask", "+++++++onPostExecute called++++++Outside_IF");
			if (mCallbacks != null) {
				Log.v("AsyncTask", "+++++++onPostExecute called++++++Inside_IF");
		        mCallbacks.onPostExecute();
		      }
		}    	
		
		 @Override
		 protected void onCancelled() 	{
			 Log.v("AsyncTask", "+++++++onCancelled called++++++Outside_IF");
		      if (mCallbacks != null) {
		    	  Log.v("AsyncTask", "+++++++onCancelled called++++++Inside_IF");
		        mCallbacks.onCancelled();
		      }
		 }
		 
		private boolean isNetworkAvailable() {
			boolean flag = false;
			ConnectivityManager connectivity = (ConnectivityManager) mHostingActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null)
					for (int i = 0; i < info.length; i++)
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							flag = true;
						}
				NetworkInfo result = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (result != null && result.isConnectedOrConnecting())
					flag = true;

				if (flag)
					return Server.isWalledGardenConnection();
			}
			return false;
		}
	}//end of SplashTask
		
}//end of TaskFragment


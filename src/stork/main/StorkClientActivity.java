package stork.main;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import stork.ConnectForm;
import stork.JobProgressActivity;
import stork.PrefetchThread;
import stork.Server;
import stork.TreeView;
import stork.TreeViewRoot;
import stork.ad.Ad;
import stork.listeners.ConfirmDAPClickListener;
import stork.server.SendDAPFileTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StorkClientActivity extends Activity {
	public static BlockingQueue<TreeView> queue = new LinkedBlockingQueue<TreeView>();
	public static volatile TreeViewRoot[] lc = { null, null };
	boolean dapConfirmResponse = false;
	public static String cert_path = null;
	public static PrefetchThread[] pac;
	public static int countOfThreads = 5;
	File mPath = new File(Environment.getExternalStorageDirectory() + "/"
			+ "Stork");

	// Set when we start the connect form activity.
	public static TreeViewRoot currentContext = null;

	public static StorkClientActivity context = null;

	// Used to load layout views.
	public static LayoutInflater inflater() {
		return (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public final static String FIRST_TIME_USE = "FirstTimeUse";
	public static final String PREFEREENCES = "StorkSharedPrefences";
	public static final String CERTIFICATE_LOCATION = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "Stork"
			+ File.separator + "Certificates";

	/** Called when the activity is first created. */
	@SuppressWarnings("resource")
	public void onCreate(Bundle saveState) {
		try {
			super.onCreate(saveState);
			setContentView(R.layout.bothlists);
			context = this;

			// Make sure network is available.
			if (!isNetworkAvailable()) {
				showToast("Network is unavailable", true);
				return;
			}
			// Spawn a thread to fetch the creds.
			new FetchCredentials().execute(null, null);

			// Grab the views for both lists.
			lc[0] = new TreeViewRoot("left",
					(ViewGroup) findViewById(R.id.left));
			lc[1] = new TreeViewRoot("right",
					(ViewGroup) findViewById(R.id.right));
			if(saveState != null) {
				String temp = saveState.getString("lc[0]");
				Log.v("URI1", temp + "");
				String temp1 = saveState.getString("lc[1]");
				Log.v("URI2", temp1 + "");
				if (temp != null) {
					lc[0].init(new URI(temp));
				}
	
				if (temp1 != null) {
					lc[1].init(new URI(temp1));
				}
			}
			// Initialize prefetching threads.
			pac = new PrefetchThread[countOfThreads];
			for (int i = 0; i < countOfThreads; i++)
				(pac[i] = new PrefetchThread()).start();

			// Set up listeners for both of the views.
			for (final TreeViewRoot l : lc) {
				registerForContextMenu(l.view);

				Button searchButton = (Button) l.view
						.findViewById(R.id.serverSelection);
				searchButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						currentContext = l;
						Intent i = new Intent(context, ConnectForm.class);
						startActivity(i);
					}
				});
			}

			SharedPreferences prefs = getSharedPreferences(PREFEREENCES,
					Context.MODE_PRIVATE);

			// it returns false if settings is not set before or this is the
			// first time we are running
			boolean silent = prefs.getBoolean(FIRST_TIME_USE, false);

			if (!silent) {
				// change to true
				SharedPreferences.Editor editor = prefs.edit();// creates the
																// editor using
																// which we can
																// modify the
																// shared
																// preference
																// data
				editor.putBoolean(FIRST_TIME_USE, true); // sets the preference
															// data to the
															// boolean value
															// passed as the
															// second parameter
				editor.commit(); // makes the changes visible to all

				/* do all operations on first run */

				// creating folder for certificates if needed in sdcard
				File sdDir = new File(CERTIFICATE_LOCATION);
				if (sdDir.mkdirs())
					Log.v(getClass().getSimpleName(), "Certificate Dir Created");
				else
					Log.e(getClass().getSimpleName(),
							"Certificate Dir Not Created!");// getSimplename jus
															// returns the name
															// of the class

			}
			// }
		}// end of try
		catch (Exception e) {
			e.printStackTrace();
			Log.v("Error in StorkClientActivity", e.getMessage());
		}

	}

	/**
	 * Menu options. TODO remove for ICS compatibility
	 */
	// called whenever the activity is loaded onto the device
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.clientmenu, menu);
		return true;
	}

	/**
	 * Menu Listener.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.progress:
			Intent getProgressIntent = new Intent(getApplicationContext(),
					JobProgressActivity.class);
			startActivityForResult(getProgressIntent, 0);
			return true;
		case R.id.Dserver1:
			Log.v("server 1", "Dserver1");
			lc[0].reset();
			return true;
		case R.id.Dserver2:
			Log.v("server 2", "Dserver2");
			lc[1].reset();
			return true;
		case R.id.DserverBoth:
			Intent i = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(
							getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(i);
			finish();
			return true;
		case R.id.disconnectAll:
			Log.v("disconnect", "All");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Select's the file / dirs to be and from transfered. If all passes checks,
	 * sends a DAP request to server.
	 * 
	 * @param serverFrom
	 * @param serverTo
	 * @return
	 */
	public boolean makeTransfer(TreeViewRoot fromRoot, TreeViewRoot toRoot) { // ListView
																				// serverfrom
																				// and
																				// serverto
		if (fromRoot == null && toRoot == null) {
			showToast("Make a selection");
			return false;
		}
		
		List<TreeView> from = fromRoot.selectedChild;
		if(from.size() < 1){
			showToast("Select atleast one source directory", true);
			return false;
		}
		if (toRoot.selectedChild.size() > 1) {
			showToast("Select only one destination", true);
			return false;
		}
		TreeView to = null;
		Log.v("To size = ", toRoot.selectedChild.size() + "");
		if (toRoot.selectedChild.size() == 1) {
			to = toRoot.selectedChild.get(0);
		}
		if (toRoot.selectedChild.size() > 1) {
			showToast("select only one destination");
		}
	
		// if the user misses to select a directory on one of the sides then
		// transfer to the path set on the login page.
		if (from != null && to == null) {
			to = toRoot;// how to acces
		}

		int counter = 0;
		while (counter < from.size()) {
			// perform validation
			if (from.get(counter++).dir && !to.dir) {
				showToast("Cannot transfer directory to a file");
				return false;
			}
		}
		//{"src":{"uri":["ftp://didclab-ws8/home/globus/stuff"]},
		//"dest":{"uri":["ftp://didclab-ws8/home/globus/stuff"]},"options":{"optimizer":null,"overwrite":true,"verify":false,"encrypt":false,"compress":false}}
		View mSpinner = View.inflate(this, R.layout.spinner, null);
		Spinner s = (Spinner) mSpinner.findViewById(R.id.spin);
		ArrayList<RowData> data = getRowData();
		s.setAdapter(new CustomSpinnerAdapter(getApplicationContext(), data));
		
		SendDAPFileTask sendDap = new SendDAPFileTask(from, to, data);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		ConfirmDAPClickListener dapListener = new ConfirmDAPClickListener(this,
				sendDap, null);
		
		builder.setMessage(sendDap.toString()).setCancelable(false)
				.setView(mSpinner)
				.setTitle("Are you sure about this transfer?")
				.setPositiveButton("Yes", dapListener)
				.setNegativeButton("No", dapListener);
		AlertDialog alert = builder.create();
		
		// set width n height
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(alert.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.FILL_PARENT;
		alert.show();
		alert.getWindow().setAttributes(lp);

		return true;	
	}

	private ArrayList<RowData> getRowData() {
		ArrayList<RowData> listToReturn = new ArrayList<RowData>();
		
		String[] options = getResources().getStringArray(R.array.option_array);
		
		for(String option: options){
			RowData data = new RowData();
			data.setName(option);
			listToReturn.add(data);
		}
		return listToReturn;
	}

	public static void showToast(String msg) {
		showToast(msg, false);
	}

	public static void showToast(final String msg, boolean is_long) {
		final int l = (is_long) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(l);
		TextView v = (TextView) context.findViewById(R.id.toasttext);
		v.setText(msg);
		toast.setView(context.getLayoutInflater().inflate(R.layout.customtoast, null));
		toast.show();
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
		}
		return false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	protected void onSaveInstanceState(Bundle saveState) {
		Log.v("Onsaved Instance", "state called");
		Log.v("URI1", lc[0].uri + "");
		Log.v("URI2", lc[1].uri + "");
		System.out.println(lc[1].hashCode());
		if(lc[0].uri != null) saveState.putString("lc[0]", lc[0].uri + "");
		if(lc[1].uri != null) saveState.putString("lc[1]", lc[1].uri + "");
		super.onSaveInstanceState(saveState);
	}
	
	private class FetchCredentials extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Ad ad = Server.sendRequest("/api/stork/info?type=cred",
					null, "POST");	
			System.out.println(ad.toString());
			Server.getCredentials().clear();
			Server.getCredentials().add("");
			for (String s : ad.keySet()) {
				Server.getCredentials().add(s);
			}
			return null;
		}
		
	}
}
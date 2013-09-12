package stork.main;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import stork.Configuration;
import stork.ConnectForm;
import stork.JobProgressActivity;
import stork.PrefetchThread;
import stork.Server;
import stork.TreeView;
import stork.TreeViewRoot;
import stork.ad.Ad;
import stork.ad.AdObject;
import stork.listeners.ConfirmDAPClickListener;
import stork.server.SendDAPFileTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

/**
 * Main Activity for Stork Thin Client
 * 
 * @author Basically Everyone
 */
public class StorkClientActivity extends Activity {
	public static BlockingQueue<TreeView> queue = new LinkedBlockingQueue<TreeView>();
	public static TreeViewRoot[] lc = { null, null };
	boolean dapConfirmResponse = false;
	public static String cert_path = null;
	public static PrefetchThread[] pac;
	public static int countOfThreads = 5;
	File mPath = new File(Environment.getExternalStorageDirectory() + "/"+"Stork");
	
	// Set when we start the connect form activity.
	public static TreeViewRoot currentContext = null;
	
	public static StorkClientActivity context = null;
	
	// Used to load layout views.
	public static LayoutInflater inflater() {
		return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public final static String FIRST_TIME_USE = "FirstTimeUse";
	public static final String PREFEREENCES = "StorkSharedPrefences";
	public static final String CERTIFICATE_LOCATION = Environment.getExternalStorageDirectory()+File.separator+"Stork"+File.separator+"Certificates";

	/** Called when the activity is first created. */
	@SuppressWarnings("resource")
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.bothlists);

		context = this;
		
		// Make sure network is available.
		if (!isNetworkAvailable()) {
			showToast("Network is unavailable", true);
			return;
		}
		// Spawn a thread to fetch the creds.
		new Thread() {
			public void run() {
				Ad ad = Server.sendRequest("/api/stork_info?type=cred");
				Set<Entry<Object, AdObject>> a = ad.entrySet();
				for(Entry<Object, AdObject> s : a){
					Server.credentialKeys.add(s.getKey().toString());
				}
			}
		}.start();
		
		// Grab the views for both lists.
		lc[0] = new TreeViewRoot("left", (ViewGroup) findViewById(R.id.left));
		lc[1] = new TreeViewRoot("right", (ViewGroup) findViewById(R.id.right));

		// Initialize prefetching threads.
		pac = new PrefetchThread[countOfThreads];
		for(int i=0;i<countOfThreads;i++)
			(pac[i] = new PrefetchThread()).start();
		
		// Set up listeners for both of the views.
		for (final TreeViewRoot l : lc) {
			registerForContextMenu(l.view);

			Button searchButton = (Button) l.view.findViewById(R.id.serverSelection);
			searchButton.setOnClickListener(new View.OnClickListener() { 
				public void onClick(View v) {
					currentContext = l;
					Intent i = new Intent(context, ConnectForm.class);
					startActivity(i);
				}
			});
		}

		SharedPreferences prefs = getSharedPreferences(PREFEREENCES, Context.MODE_PRIVATE);

		// it returns false if settings is not set before or this is the first time we are running
		boolean silent = prefs.getBoolean(FIRST_TIME_USE, false);

		if (!silent) {
			//change to true
			SharedPreferences.Editor editor = prefs.edit();//creates the editor using which we can modify the shared preference data  
			editor.putBoolean(FIRST_TIME_USE, true); //sets the preference data to the boolean value passed as the second parameter
			editor.commit(); // makes the changes visible to all

			/*do all operations on first run*/

			//creating folder for certificates if needed in sdcard
			File sdDir = new File(CERTIFICATE_LOCATION);
			if(sdDir.mkdirs()) 	Log.v(getClass().getSimpleName(), "Certificate Dir Created");
			else 				Log.e(getClass().getSimpleName(), "Certificate Dir Not Created!");//getSimplename jus returns the name of the class

		}
	}

	/**
	 * Menu options. TODO remove for ICS compatibility
	 */
	//being an onCreate called whenever the application is loaded onto the device
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.clientmenu, menu);
		return true;
	}

	/**
	 * Menu Listener. 
	 */
	//called whenever the user selects some options from the main screen
	//Menu button is present at the bottom of the phone, visible when the application is running

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.transfer21:
			return makeTransfer(lc[1], lc[0]);
		case R.id.transfer12:
			return makeTransfer(lc[0], lc[1]);
		case R.id.progress:
			Intent getProgressIntent = new Intent(getApplicationContext(), JobProgressActivity.class);
			startActivityForResult(getProgressIntent, 0);
			return true;
		case R.id.disconnectAll:
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(i);
			return true;
		case R.id.config:
			Intent intent = new Intent(this, Configuration.class);
			startActivity(intent);
			return true;
		case R.id.select:
			openFiles();//file browsing on the sd card
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	private void openFiles() {
		FileDialog fileDialog = new FileDialog(StorkClientActivity.this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Log.d(getClass().getName(), "selected file " + file.toString());
            }
        });
        fileDialog.showDialog();
	}

	/**
	 * Select's the file / dirs to be and from transfered. If all passes checks,
	 * sends a DAP request to server.
	 * 
	 * @param serverFrom
	 * @param serverTo
	 * @return
	 */
	private boolean makeTransfer(TreeViewRoot fromRoot, TreeViewRoot toRoot) { //ListView serverfrom and serverto
		//if the user doesn't enter any credentials of servers to which this app should connect
		TreeView from = fromRoot.selectedChild;
		TreeView to = toRoot.selectedChild;
		//Checking that the user makes a selection
		if (from == null || to == null) {
			showToast("Make a selection");
			return false;
		}
		//perform validation 
		if(from.dir && !to.dir){
			showToast("Cannot transfer directory to a file");
			return false;
		}
		
		SendDAPFileTask sendDap = new SendDAPFileTask(from, to);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		ConfirmDAPClickListener dapListener = new ConfirmDAPClickListener(this, sendDap, null);
		builder .setMessage(sendDap.toString()/* + "\n"*/)
		.setCancelable(false)
		.setTitle("Are you sure about this transfer?")
		.setPositiveButton("Yes",dapListener)
		.setNegativeButton("No", dapListener);
		AlertDialog alert = builder.create();

		//set width n height
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(alert.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.FILL_PARENT;
		alert.show();
		alert.getWindow().setAttributes(lp);
		
		return true;
	}

	final static Handler toaster = new Handler();
	public static void showToast(String msg) {
		showToast(msg, false);
	} public static void showToast(final String msg, boolean is_long) {
		final int l = (is_long) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		toaster.post(new Runnable() {
			public void run() {
				Toast.makeText(context, msg, l).show();
			}
		});
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager manager =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
}
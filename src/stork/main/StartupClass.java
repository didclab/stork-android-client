package stork.main;

import stork.Server;
import stork.ad.Ad;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class StartupClass extends Activity {
	public static StartupClass context = null;
	    private SharedPreferences loginPreferences;
	    private SharedPreferences.Editor loginPrefsEditor;
	    private Boolean saveLogin;
	   
	protected void onCreate(Bundle savedInstanceState) {
		try{
		context = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);

		if (!isNetworkAvailable()) {
			Log.v("Network is unavailable", "Startup Class");
			alertbox("Network problem", "Network unavailable");
			return;
		} else {
			final EditText username = (EditText) findViewById(R.id.getUserName);
			final EditText password = (EditText) findViewById(R.id.getPassword);
			final CheckBox rememberMe = (CheckBox) findViewById(R.id.rememberMe);
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
	        loginPrefsEditor = loginPreferences.edit();
	        
	        saveLogin = loginPreferences.getBoolean("saveLogin", false);
	        if (saveLogin) {
	        	String user = loginPreferences.getString("username", "");
	            username.setText(user);
	            password.setText("somerandom");
	            String cookie = loginPreferences.getString("cookie", "");
	            if(!cookie.equals("")){
	            	Server.cookie = Ad.parse(cookie);
	            }
	            rememberMe.setChecked(true);
	        }
			final Button submitButton = (Button) findViewById(R.id.StartupSubmit);
			submitButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					StartupClass context = StartupClass.this;
					String user = username.getText().toString();
					String pass = password.getText().toString();
					try {
						//first time when the user logs in.
						if (validate(user, pass) && !saveLogin) {
							Ad ad = new Ad("user_id", user);
							ad.put("password", pass);
							Thread t;
							t = asyncFetchCookie(ad);
							t.run();
							Intent intent = new Intent(context,
									StorkClientActivity.class);
							startActivity(intent);
						}
						else{
							//the login information is already stored
							if(saveLogin){
								Intent intent = new Intent(context,
										StorkClientActivity.class);
								startActivity(intent);
							}
						}
						if (rememberMe.isChecked()) {
			                loginPrefsEditor.putBoolean("saveLogin", true);
			                loginPrefsEditor.putString("username", user);
			                loginPrefsEditor.putString("cookie", Server.cookie.toString());
			                loginPrefsEditor.commit();
			            } else {
			                loginPrefsEditor.clear();
			                loginPrefsEditor.commit();
			            }
					} catch (Exception e) {
						Toast.makeText(StartupClass.this, e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			Button registerButton = (Button) findViewById(R.id.StartupRegister);
			registerButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(context,
							Register.class);
					startActivity(intent);	
				}
			});
		}// end of else
		
	}//end of try
	catch(Exception e){
		Log.v("Error", e.getMessage());
	}
	
	}
	private Thread asyncFetchCookie(final Ad ad) {
		return new Thread() {
			public void run() {
				try {
					Server.cookie = Server.sendRequest("/api/stork/user", ad,
							"POST").remove("pass_salt");
					Log.v("Cookie", Server.cookie.toString());
				} catch (final Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		};
	}
	private boolean validate(String user, String pass) {
		if (user.length() > 0 && pass.length() >= 6) {
			return true;
		} else {
			if (user.length() == 0) {
				showToast("Invalid user Name");
				return false;
			}
			if (pass.length() < 6) {
				showToast("Password should be more than or equal to 6 letters");
				return false;
			} else {
				showToast("UserName or Password combination is invalid");
				return false;
			}
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
            NetworkInfo result = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            	if (result != null && result.isConnectedOrConnecting());
            		return true;
        }
        return false;
  }
	private static void showToast(String s) {
		Toast.makeText(context, s, Toast.LENGTH_LONG).show();
	}
	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}
	@Override
	protected void onResume(){
		super.onResume();
		if (!isNetworkAvailable()) {
			Log.v("inside", "Network unavailable");
			alertbox("Network problem", "Network unavailable");
		}
		
	}

	@Override
	protected void onRestart(){
		Log.v("Restart", "Called");
		super.onRestart();
	}
	
	@Override
	protected void onStop(){
		Log.v("Stop", "Called");
		super.onStop();
	}
	
	@Override
	protected void onPause(){
		Log.v("Pause", "Called");
		super.onPause();
	}

//	protected void onDestroy(){
//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		PowerManager.WakeLock wakeLock = pm.newWakeLock(
//		        pm.SCREEN_DIM_WAKE_LOCK, "My wakelook");
//		wakeLock.acquire(1000);
//		wakeLock.release();
//	}
}

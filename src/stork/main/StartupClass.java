package stork.main;

import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
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
import android.text.Editable;
import android.text.TextWatcher;
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
	private boolean textedited = false;

	protected void onCreate(Bundle savedInstanceState) {
		try {
			context = this;
			super.onCreate(savedInstanceState);

			if (!isNetworkAvailable()) {
				Log.v("Network is unavailable", "Startup Class");
				alertbox("Network problem", "Network unavailable");
				return;
			} else {
				setContentView(R.layout.startup);
				final EditText email = (EditText) findViewById(R.id.getEmail);
				final EditText password = (EditText) findViewById(R.id.getPassword);
				final CheckBox rememberMe = (CheckBox) findViewById(R.id.rememberMe);
				loginPreferences = getSharedPreferences("loginPrefs",
						MODE_PRIVATE);
				loginPrefsEditor = loginPreferences.edit();
				saveLogin = loginPreferences.getBoolean("saveLogin", false);

				if (saveLogin) {
					getCookie(email, password, rememberMe);
				}
				email.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						textedited = true;
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						textedited = true;
					}
				});

				final Button submitButton = (Button) findViewById(R.id.StartupSubmit);
				submitButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						v.performHapticFeedback(VIRTUAL_KEY);
						StartupClass context = StartupClass.this;
						String uEmail = email.getText().toString();
						String pass = password.getText().toString();
						try {
							if (saveLogin) {
								if (uEmail.equals(Server.cookie.get("email"))
										&& textedited == false) {
									// login with the last stored information
									Ad ad = new Ad(Server.cookie).put("action",
											"login");
									Thread t = asyncFetchCookie(ad);
									t.run();
									Intent intent = new Intent(context,
											StorkClientActivity.class);
									startActivity(intent);
								} else {// consider the typed info
									if (validate(uEmail, pass)) {
										typedLogin(context, uEmail, pass);
									}
								}
							} else {
								// consider the typed info
								if (validate(uEmail, pass)) {
									typedLogin(context, uEmail, pass);
								}
							}
							if (rememberMe.isChecked()) {
								setCookie();
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
						try {
							Intent intent = new Intent(context, Register.class);
							startActivity(intent);
						} catch (Exception e) {
							showToast(e.getMessage());
						}
					}
				});
			}// end of else

		}// end of try
		catch (Exception e) {
			Log.v("Error", e.getMessage());
		}

	}

	private void setCookie() {
		loginPrefsEditor.putBoolean("saveLogin", true);
		loginPrefsEditor.putString("cookie", Server.cookie.toString());
		loginPrefsEditor.commit();
	}

	private void getCookie(EditText email, EditText password,
			CheckBox rememberMe) {
		password.setText("password");// fake password
		String cookie = loginPreferences.getString("cookie", "");
		if (!cookie.isEmpty())
			Server.cookie = Ad.parse(cookie).filter("email", "hash");// jus get the email and hash
		email.setText(Server.cookie.get("email"));
		rememberMe.setChecked(true);
	}

	private Thread asyncFetchCookie(final Ad ad) {
		return new Thread() {
			public void run() {
				try {
					Server.cookie = Server.sendRequest("/api/stork/user", ad,
							"POST");
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
				showToast("Invalid email");
				return false;
			}
			if (pass.length() < 6) {
				showToast("Password should be more than or equal to 6 letters");
				return false;
			} else {
				showToast("Email or Password combination is invalid");
				return false;
			}
		}
	}

	private boolean isNetworkAvailable() {
		boolean flag = false;
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

	private static void showToast(String s) {
		Toast.makeText(context, s, Toast.LENGTH_LONG).show();
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						}).show();
	}

	@Override
	protected void onResume() {
		Log.v("StartupClass", "onResume Called");
		super.onResume();
		if (!isNetworkAvailable()) {
			Log.v("inside", "Network unavailable");
			alertbox("Network problem", "Network unavailable");
		}
	}

	@Override
	protected void onRestart() {
		Log.v("StartupClass", "Restart Called");
		if (!isNetworkAvailable()) {
			Log.v("Network is unavailable", "Startup Class");
			alertbox("Network problem", "Network unavailable");
		}
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Log.v("StartupClass", "onStop");
		super.onStop();
	}

	@Override
	protected void onPause() {
		Log.v("StartupClass", "onPause Called");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.v("StartupClass Destroyed", "");
		super.onDestroy();
	}

	private void typedLogin(StartupClass context, String uEmail, String pass) {
		Ad ad = new Ad("action", "login");
		ad.put("email", uEmail).put("password", pass);
		Thread t;
		t = asyncFetchCookie(ad);
		t.run();
		Intent intent = new Intent(context, StorkClientActivity.class);
		startActivity(intent);
	}
}

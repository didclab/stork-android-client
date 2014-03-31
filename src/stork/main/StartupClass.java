package stork.main;

import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import stork.Server;
import stork.ad.Ad;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
		context = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		final EditText email = (EditText) findViewById(R.id.getEmail);
		final EditText password = (EditText) findViewById(R.id.getPassword);
		final CheckBox rememberMe = (CheckBox) findViewById(R.id.rememberMe);
		loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
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
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
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
						if (uEmail.equals(Server.getCookie().get("email"))
								&& textedited == false) {
							// login with the last stored information
							Ad ad = new Ad(Server.getCookie())
									.put("action", "login");
							new asyncFetchCookie().execute(ad);
							
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

	}

	private void setCookie() {
		loginPrefsEditor.putBoolean("saveLogin", true);
		loginPrefsEditor.putString("cookie", Server.getCookie().toString());
		loginPrefsEditor.commit();
	}

	private void getCookie(EditText email, EditText password,
			CheckBox rememberMe) {
		password.setText("password");// fake password
		String cookie = loginPreferences.getString("cookie", "");
		Ad ad = null;
		if (!cookie.isEmpty())
			 ad = Ad.parse(cookie).filter("email", "hash");// jus get
																		// the
																		// email
																		// and
																		// hash
		Server.setCookie(ad);
		email.setText(Server.getCookie().get("email"));
		rememberMe.setChecked(true);
	}
	
	private class asyncFetchCookie extends AsyncTask<Ad, Void, Void> {

		@Override
		protected Void doInBackground(Ad... ads) {
			try {
				 Ad ad = Server.sendRequest("/api/stork/user", ads[0],
						"POST");
				 Server.setCookie(ad);
			} catch (final Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			return null;
		}
		
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


	private void typedLogin(StartupClass context, String uEmail, String pass) {
		Ad ad = new Ad("action", "login");
		ad.put("email", uEmail).put("password", pass);
		new asyncFetchCookie().execute(ad);
		Intent intent = new Intent(context, StorkClientActivity.class);
		startActivity(intent);
	}
}

package stork.main;

import stork.Server;
import stork.ad.Ad;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends Activity {
	static Register context;
	
	protected void onCreate(Bundle savedInstanceState) {
		try{
		Log.v("inside", "Register");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		context = Register.this;
		final EditText email = (EditText) findViewById(R.id.email);
		final EditText password = (EditText) findViewById(R.id.Rpass);
		Log.v("password", password.toString());
		Button registerButton = (Button) findViewById(R.id.register);
		registerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					String email1 = email.getText().toString();
					String pass = password.getText().toString();
					Log.v("password", pass);
					if (validate(pass, email1)) {
						Ad ad = new Ad("email", email1);
						ad.put("password", pass);
						ad.put("action", "register");
						Thread t = asyncFetchCookie(ad);
						t.run();
						if(Server.cookie!=null)
							finish();
					}
				} catch (Exception e) {
					showToast(e.getMessage());
				}	
			}
		});
		}//end of try
		catch(Exception e){
			Log.v("Register", e.toString());
		}
	}
	private boolean validate(String pass, String email1) {
		if ( pass.length() >= 6 && isValidEmail(email1)) {
			return true;
		} else {
			if (isValidEmail(email1) == false) {
				showToast("Invalid email id");
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
	private static void showToast(String s) {
		Toast.makeText(context, s, Toast.LENGTH_LONG).show();
	}
	private Thread asyncFetchCookie(final Ad ad) {
		return new Thread() {
			public void run() {
				try {
					Server.cookie = Server.sendRequest("/api/stork/user", ad,
							"POST");
					Log.v("Cookie", Server.cookie.toString());
				} catch (final Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		};
	}
	
	public final static boolean isValidEmail(String target) {
		 if (target.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+") && target.length() > 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }   
}//end of class

	


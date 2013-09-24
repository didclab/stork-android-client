package stork;

import stork.ad.Ad;
import stork.main.R;
import stork.main.R.layout;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		final EditText text = (EditText) findViewById(R.id.textView1); 
		final String hostname =  text.getText().toString();
		text.setHint("Proxy Server Name");

		// Set click listener for the submit button.
		findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				EditText host =  (EditText) findViewById(R.id.UserName);
				String userName = host.getText().toString();
				EditText pass = (EditText) findViewById(R.id.Password);
				String password = pass.getText().toString();

				try {
					Ad ad = new Ad("myproxy_host", hostname).
							   put("myproxy_user", userName).
							   put("myproxy_pass", password).
							   put("type", "gss-cert").
							   put("action", "create");
					
					Ad res = Server.sendRequest(null, "/api/stork/cred", ad, "POST");
					String cred = res.get("uuid");
					Server.credentialKeys.add(cred);
					Toast.makeText(Login.this, "Got token: "+cred, Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(Login.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}

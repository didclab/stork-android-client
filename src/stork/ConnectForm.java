package stork;

import java.net.URI;

import stork.main.R;
import stork.main.StorkClientActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectForm extends Activity {
	
	private String getFormItem(int id) {
		String s = ((TextView) findViewById(id)).getText().toString().trim();
		return s.isEmpty() ? null : s;
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accessserver);
		final Spinner mySpinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, Server.credentialKeys);
		mySpinner.setAdapter(adapter);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Set click listener for the login button.
		Button okButton = (Button) findViewById(R.id.loginbutton);
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					onClick2(v);
					v.performHapticFeedback(VIRTUAL_KEY);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(ConnectForm.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			} private void onClick2(View v) throws Exception {
				final String protocol = getFormItem(R.id.protocol);
				final String server   = getFormItem(R.id.serverName);
				final String username = getFormItem(R.id.username);
				final String password = getFormItem(R.id.password);
				final String path     = getFormItem(R.id.serverPath);
				
				String ui = username == null ? null :
				            password == null ? username :
				            username+":"+password;
				TreeViewRoot lc = StorkClientActivity.currentContext;
				
				// Try to init the list context.
				lc.init(new URI(protocol, ui, server, -1, path, null, null));
				lc.cred = mySpinner.getSelectedItem().toString();
				finish();
			}
		});
		
		// Set the click listener for the protocol selector.
		Button pkey = (Button) findViewById(R.id.AddCredential);
		pkey.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ConnectForm context = ConnectForm.this;
				Intent intent = new Intent(context, Login.class);
				intent.putExtra("hi", "LCL");
				startActivity(intent);
				//finish();
			}
		});
//http://128.205.39.40:9000/api/stork_ls?uri=ftp://didclab-ws9:21/&depth=0
		// Set the click listener for the credential picker.
	}//end of OnCreate
	
}

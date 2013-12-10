package stork;

import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import stork.ad.Ad;
import stork.main.R;
import stork.main.StorkClientActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectForm extends Activity {
	
	private String getFormItem(int id) {
		String s = ((TextView) findViewById(id)).getText().toString().trim();
		return s.isEmpty() ? null : s;
	}
	private static final List<String> listServerNames= new ArrayList<String>();
	
	protected void onCreate(Bundle savedInstanceState) {
		try{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accessserver);

		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, listServerNames);//serverName
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.serverName);
		textView.setAdapter(adapter1);	
		
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

				if(!listServerNames.contains(server))
					listServerNames.add(server);
				
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
				finish();
			}
		});
	}//end of try
	catch(Exception e){
		e.printStackTrace();
		Log.v("Error on ConnectForm", e.toString());
	}
}//end of OnCreate
}





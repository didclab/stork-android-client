package stork.main;

import java.util.Set;
import java.util.Map.Entry;

import stork.Server;
import stork.ad.Ad;
import stork.ad.AdObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartupClass extends Activity{
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		final EditText username = (EditText) findViewById(R.id.getUserName);
		final EditText password = (EditText) findViewById(R.id.getPassword);
		Button tryButton = (Button) findViewById(R.id.StartupSubmit);
		
		tryButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {	
			StartupClass context = StartupClass.this;
			try{
				Ad ad = new Ad("user_id", username.getText().toString());
				ad.put("password", password.getText().toString());
				//ad.put("action", "register");
				Server.cookie = Server.sendRequest("/api/stork_user", ad, "POST").remove("pass_salt");
				Log.v("Cookie", Server.cookie.toString());
				Intent intent = new Intent(context, StorkClientActivity.class);
				startActivity(intent);
			}
			catch(Exception e){
				Toast.makeText(StartupClass.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			}
		});
	}

}

package stork;

import stork.main.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Configuration extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		try{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		}
		catch(Exception e){
			Log.v("Error", e.toString());
		}
	}
}
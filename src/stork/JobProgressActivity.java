package stork;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import stork.ad.Ad;
import stork.main.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import stork.adapter.ProgressListAdapter;
import stork.framework.ProgressView;

/**
 * Activity to see all the different job progress activities and further options
 * accordingly
 * 
 * @author Rishi
 * 
 */
public class JobProgressActivity extends Activity  {
	ArrayAdapter<ProgressView> adapter;
	ListView lv;
	Timer timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.progressrow);
			adapter = new ProgressListAdapter(this, new ArrayList<ProgressView>(), R.layout.rowprogresslayout);
			adapter.setNotifyOnChange(true);
			getData(adapter);

			lv = (ListView) findViewById(R.id.progresslistview);
			lv.setAdapter(adapter);


			Toast.makeText(getApplicationContext(), "Touch Job ID for Details, Cancel or Remove Job", Toast.LENGTH_LONG).show();

			timer = new Timer();

			timer.scheduleAtFixedRate(new TimerTask(){

				public void run()
				{
					try
					{
						runOnUiThread(new  Runnable() {

							public void run() {
								System.out.println("running...");
								getData(adapter);
								adapter.notifyDataSetChanged();
								lv.invalidateViews();
							}
						});
					}
					catch(Exception e)
					{
						Log.v(getLocalClassName(), e.toString());
					}
				}

			}, 1000,1000);
		}
		catch(Exception e)
		{
			Log.e(getClass().getSimpleName(), e.toString());
		}
	}


	@Override
	/**
	 * Menu options. TODO remove for ICS compatibility
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		try
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.progressmenu, menu);
		}
		catch(Exception e)
		{
			Log.v(getLocalClassName(), e.toString());
		}
		return true;
	}

	@Override
	/**
	 * responds to menu button pressed
	 * @param menu item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.goBack: {
			try
			{
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
				return true;
			}
			catch(Exception e)
			{
				Log.v(getLocalClassName(), e.toString());
			}
		}
		case R.id.goStartAgain: {
			try
			{
				Intent intent = getIntent();
				finish();
				startActivity(intent);
				return true;
			}
			catch(Exception e)
			{
				Log.v(getLocalClassName(), e.toString());
			}
		}
		default:
			return false;
		}
	}

	/**
	 * adds to the adapter actual data from Stork
	 * 
	 * @param adapter
	 */
	public void getData(ArrayAdapter<ProgressView> adapter) {
		// actual data
		//DatabaseWrapper dbHelper = new DatabaseWrapper(getApplicationContext());
		//String[][] temp = dbHelper.onSelectAll();
		//dbHelper.close();

		// Get the listing from Stork.\
		adapter.clear(); //added by Sonali Batra
		try
		{
			Map<Integer, Ad> queue = Server.getQueue("all");

			if (queue == null)
				return;

			Log.v(getClass().getName(), queue.toString());

			// For everything in the map, put ProgressView in list. 
			for (Ad ad : queue.values()) {
				Ad prog = ad.getAd("progress.bytes");
				int d = -1;

				String status = ad.get("status");
				long id = ad.getInt("job_id");
				String src = ad.get("src.uri");
				String dest = ad.get("dest.uri");

				// Ignore invalid ads.
				if (id <= 0) continue;
				if (status == null) continue;
				if (src == null) continue;
				if (dest == null) continue;

				// Parse progress.
				if (prog != null && prog.getInt("total") > 0)
					d = 100*prog.getInt("done") / prog.getInt("total");
				if (d > 100) d = 100;

				// Insert status into list.
				if (d >= 0 && d <= 100)
					adapter.insert(new ProgressView(id, src, d, dest), 0);
				else if (status.equals("complete"))
					adapter.insert(new ProgressView(id, src, 100, dest), 0);
				else
					adapter.insert(new ProgressView(id, src, status, dest), 0);	
			}

			/*if (progress.matches("[0-9]*\\.[0-9]*"))
			adapter.insert(new ProgressView(id, s[1], (int) (Float.valueOf(progress.trim()).floatValue()), s[2]),0);
		else {
			if(progress.equals("request_removed"))		progress = "Removed";
			adapter.insert(new ProgressView(id, s[1], progress.trim(), s[2]),0);
		}*/
			adapter.notifyDataSetChanged();
		}
		catch(Exception e)
		{
			Log.v(getLocalClassName(), e.toString());
		}
	}

	@Override
	public void onPause(){
		try
		{
			super.onPause();
			finish();
		}
		catch(Exception e)
		{
			Log.v(getLocalClassName(), e.toString());
		}
	}

	//	public class RemindTask extends TimerTask
	//	{
	//		public void run()
	//		{
	//			try
	//			{
	//				new timerThread().execute("");
	//			}
	//			catch(Exception e)
	//			{
	//				Log.v(getLocalClassName(), e.toString());
	//			}
	//		}
	//	}

	//	public class timerThread extends AsyncTask<String,Void,String>
	//	{
	//		@Override
	//		protected String doInBackground(String... params) {
	//			// TODO Auto-generated method stub
	//			return null;
	//		//	return "";
	//		}
	//		
	//		@Override
	//        protected void onPostExecute(String result) {
	//			try
	//			{
	//				
	//			}
	//			catch(Exception e)
	//			{
	//				Log.v(getLocalClassName(), e.toString());
	//			}
	//        }
	//
	//        @Override
	//        protected void onPreExecute() {
	//        }
	//
	//        @Override
	//        protected void onProgressUpdate(Void... values) {
	//        }
	//		
	//	}

}
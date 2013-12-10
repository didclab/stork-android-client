package stork;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import stork.ad.Ad;
import stork.adapter.ProgressListAdapter;
import stork.framework.ProgressView;
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
			adapter.sort(new Comparator<ProgressView>() {
			    public int compare(ProgressView arg0, ProgressView arg1) {
			    	long one = arg0.job_id;
			    	long two = arg1.job_id;
			    	long returnVal = (two - one);
			    	Long l = Long.valueOf(returnVal);
					Integer ret = l != null ? l.intValue() : null;
			        return ret;
			    }
			});
			adapter.notifyDataSetChanged();
			
			Toast.makeText(getApplicationContext(), "Touch Job ID for Details, Cancel or Remove Job", Toast.LENGTH_LONG).show();
			
			//fetching the jobs submitted after every 10 seconds...
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
								adapter.sort(new Comparator<ProgressView>() {
								    public int compare(ProgressView arg0, ProgressView arg1) {
								    	long one = arg0.job_id;
								    	long two = arg1.job_id;
								    	long returnVal = (two - one);
								    	Long l = Long.valueOf(returnVal);
										Integer ret = l != null ? l.intValue() : null;
								        return ret;
								    }
								});
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
			}, 6000,6000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
		
		// Get the listing from Stork.\
		adapter.clear(); 
		try
		{
			Map<Integer, Ad> queue = Server.getQueue();
			
			if (queue == null)
				return;
			// For everything in the map, put ProgressView in list. 
			for (Ad ad : queue.values()) {
				ProgressView pv = ad.unmarshalAs(ProgressView.class);
				adapter.insert(pv,0);
			}
			adapter.notifyDataSetChanged();
		}
		catch(Exception e)
		{
			Log.v(getLocalClassName(), e.toString());
		}
	}
	

	@Override
	public void onPause(){
			super.onPause();
	}
	@Override
	public void onRestart(){
			super.onRestart();
	}
}
package stork.listeners;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import stork.TreeView;
import stork.Watch;
import stork.adapter.InteractiveListArrayAdapter;

/**
 * listener that initiates a list array adapter to read files once all the
 * authentication protocols are done and dusted to get / display all the files
 * and dirs it contains.
 * 
 * @author rishi baldawa
 * 
 */
public class DirListClickListener extends AsyncTask<Void, Void, Void> implements OnClickListener {
	InteractiveListArrayAdapter aa;
	int pos;
	List<TreeView> list;
	View view;
	ProgressBar progressbar;
	Activity con = null;
	ListView lv;
	/**
	 * 
	 * @param aa Interactive List Array Adapter
	 * @param pos1 position in the list
	 * @param l the original list itself
	 * @param pb for loading icon
	 */
	public DirListClickListener(InteractiveListArrayAdapter aa,int pos1,List<TreeView> l, ProgressBar pb,  Activity context ) {
		this.list=l;
		this.pos = pos1;
		this.aa = aa;
		this.view = null;
		this.progressbar = pb;
		this.con = context;
	}
	public DirListClickListener() {
		this(null, 0, null, null, null);
	}
	public void onClick(View v) {
		Log.v(getClass().getSimpleName(), "onClick");
		view = v;
		view.setClickable(false);
		progressbar.setVisibility(View.VISIBLE);
		Void[] temporary = null;
		this.execute(temporary);
	}
	@Override
	protected Void doInBackground(Void... params) {
		view.setClickable(false);
		progressbar.setVisibility(View.VISIBLE);
		TreeView tree = list.get(pos);
		Log.v(getClass().getSimpleName(), "DoInBackground");
		
		if (tree.open) {
			Log.v("dirList", "SubDirVisible");
			tree.open = false;
		} else {
			if (tree.getChildCount() > 0){
				Log.v("dirList", "ShowChildinList");
				tree.open = true;
			}
			else {
				Log.v("dirList", "fetchMyKids");
				tree.fetchChildren();
			}
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void c){
		//this.aa.notifyDataSetChanged();
		progressbar.setVisibility(View.INVISIBLE);
		if(view != null)
			view.setClickable(true);
	}
	public boolean onLongClick(View arg1) {
		Log.v(getClass().getSimpleName(), "LongClick enabled");
		view = arg1;
		view.setClickable(false);
		progressbar.setVisibility(View.VISIBLE);
		Void[] temporary = null;
		this.execute(temporary);
		return true;
	}
}


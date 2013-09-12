package stork;

import java.net.URI;

import stork.ad.Ad;
import stork.main.R;
import stork.main.R.id;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * Represents the entire context of one of the lists on the screen.
 */

public class TreeViewRoot extends TreeView {
	public final String side;
	public final View view;
	public TreeView selectedChild;
	public URI uri;
	public String cred;
	public ListView lv;
	private BaseAdapter adapter = adapter();
	
	public TreeViewRoot(String n, View v) {
		super(null, n, true);
		side = n;
		view = v;
		v.setVisibility(View.VISIBLE);
		this.lv = (ListView) view.findViewById(R.id.listview);
	}
	public void redraw() {
		view.postInvalidate();
	}
	
	public String toString() {
		return uri.toString();
	}
	
	public URI getURI() {
		return uri;
	}
	
	public String getCred() {
		return (cred == null || cred.isEmpty()) ? null : cred;
	}
	
	// Initialize the context by creating a root treeview for a URI.
	public TreeView init(URI uri) {
		this.uri = URI.create(uri+"/").normalize();
		
		ListView v = (ListView) view.findViewById(R.id.listview);
		v.setAdapter(adapter);

		// Update the list. This may throw.
		fetchChildren();
		adapter.notifyDataSetChanged();
		
		// Update the UI.
		TextView header = (TextView) view.findViewById(R.id.server_header);
		header.setText(uri.getHost());

		ImageButton resetButton = (ImageButton) view.findViewById(R.id.server_header_x);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reset();
			}
		});

		ImageButton refreshButton = (ImageButton) view.findViewById(R.id.server_refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				asyncFetchChildren();
			}
		});
		
		view.findViewById(R.id.serverSelection).setVisibility(View.GONE);
		return this;
	}

	public BaseAdapter adapter() {
		return new BaseAdapter(){
			public int getCount() {
				return height()-1;
			}
			public TreeView getItem(int position) {
				return getChild(position);
			}

			public long getItemId(int position) {
				return position;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
				TreeView tv = getItem(position);
				if (tv == null)
					return null;
				return getItem(position).convertView((LinearLayout)convertView);
			}
		};
	}
	

	public void refreshData() {
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	protected int depth() {
		return -1;
	}
	
	public boolean isOpen() {
		return true;
	}
	
	public TreeView getChild(int i) {
		return super.getChild(i+1);
	}
	
	// Reset the list and UI.
	public void reset() {
		selectedChild = null;
		uri = null;
		view.findViewById(R.id.serverSelection).setVisibility(View.VISIBLE);
	}
	
	// select the transferring child; update the current selection.
	public void onChecked(TreeView tv){
		if(selectedChild != null && selectedChild != tv){
			selectedChild.unselect();
		}
		selectedChild = tv;
	}
}
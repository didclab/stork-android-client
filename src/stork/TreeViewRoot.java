package stork;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import stork.main.R;
import stork.main.StorkClientActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Represents the entire context of one of the lists on the screen.
 */

public class TreeViewRoot extends TreeView {
	public final String side;
	public final View view;
	public List<TreeView> selectedChild  = new ArrayList<TreeView>();
	public volatile URI uri;
	public String cred;
	public ListView lv;
	public BaseAdapter adapter = adapter();
	
	public TreeViewRoot(String n, View v) {
		super(null, n, true);
		side = n;
		view = v;
		v.setVisibility(View.VISIBLE);
		this.lv = (ListView) view.findViewById(R.id.listview);
	}
	public void redraw() {
		view.postInvalidate();
		refreshData();
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
	public TreeView init(URI u) {
		uri = u;
	
		ListView v = (ListView) view.findViewById(R.id.listview);
		v.setAdapter(adapter);
		
		// Update the list. This may throw.
		fetchChildren();
		
		adapter.notifyDataSetChanged();
		
		// Update the UI.
		ImageButton transfer12 = (ImageButton) view.findViewById(R.id.transfer12);
		transfer12.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StorkClientActivity.context.makeTransfer(StorkClientActivity.lc[0], StorkClientActivity.lc[1]);
			}
		});

		ImageButton transfer21 = (ImageButton) view.findViewById(R.id.transfer21);
		transfer21.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StorkClientActivity.context.makeTransfer(StorkClientActivity.lc[1], StorkClientActivity.lc[0]);
			}
		});
		TextView left_header = (TextView) view.findViewById(R.id.server_header_left);
		TextView right_header = (TextView) view.findViewById(R.id.server_header_right);
		TextView dummy = (TextView) view.findViewById(R.id.dummy);//for spacing out the "->" on GUI
		
		
		if(side.equals("left")) {
			left_header.setText(uri.getHost());
			left_header.setVisibility(View.VISIBLE);
			transfer21.setVisibility(View.GONE);
			transfer12.setVisibility(View.VISIBLE);
			dummy.setVisibility(View.GONE);
			right_header.setVisibility(View.GONE);
			left_header.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					StorkClientActivity.showToast(uri.getHost(),false);
				}
			});
			
		}
		else{
			right_header.setText(uri.getHost());
			right_header.setVisibility(View.VISIBLE);
			transfer12.setVisibility(View.GONE);
			transfer21.setVisibility(View.VISIBLE);
			left_header.setVisibility(View.GONE);
			dummy.setVisibility(View.INVISIBLE);
			right_header.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					StorkClientActivity.showToast(uri.getHost(),false);
				}
			});
		}

		view.findViewById(R.id.serverSelection).setVisibility(View.GONE);
		
		return this;
	}
	public ListView getListView() {
		return lv;
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
				if(position == 0){
					Log.v("positiion = 0", "");
				}
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
	
	public int height() {
		return super.height();
	}
	
	public TreeView getChild(int i) {
		return super.getChild(i+1);
	}
	
	public void post(Runnable runnable) {
		lv.post(runnable);
	}
	
	// Reset the list and UI.
	public void reset() {
		this.selectedChild = null;
		this.uri = null;
		this.view.findViewById(R.id.serverSelection).setVisibility(View.VISIBLE);
		this.children.clear();
		this.redraw();
		this.fetched = false;
		this.fetching = false;
		this.error = false;
	}
	
	public TreeViewRoot root() {
		return this;
	}	
}
package stork;

import java.net.URI;
import java.util.*;

import stork.main.StorkClientActivity;
import static stork.main.StorkClientActivity.inflater;
import android.util.Log;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import stork.ad.Ad;
import stork.main.R;
import stork.cache.Cache;

/**
 * Directory Tree Nodes used in List View. Executing this thing in
 * the background causes it to toggle state and potentially fetch
 * listing data.
 */
public class TreeView {
	public TreeView parent;
	public String name;
	public List<TreeView> children;
	public boolean dir = false;
	public boolean open = false;
	
	public boolean fetching = false;
	public boolean fetched = false;
	public boolean error = false;

	public TreeView(TreeView parent, String name, boolean dir) {
		this.parent = parent;
		this.name = name;
		this.dir = dir;
		children = new LinkedList<TreeView>();
	}
	
	public int height(){
		if(!isOpen())
			return 1;
		int t = 1;
		for(TreeView c:  children)
			t += c.height();
		return t;
	}
	
	protected View convertView(LinearLayout v) {
		if(v == null)
			v = (LinearLayout) inflater().inflate(R.layout.treenode, null);

		int h = isOpen() ? 10 : 0;
		v.setPadding(depth()*20, 0, 0, h);

		ImageView iv = (ImageView) v.findViewById(R.id.icon);
		iv.setImageResource(dir ? R.drawable.folder : R.drawable.text);
		
		TextView textView = (TextView) v.findViewById(R.id.label);
		textView.setText(toString());
		
		CheckBox c = (CheckBox) v.findViewById(R.id.check);
		c.setOnCheckedChangeListener(null);
		c.setChecked(isSelected());
		c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isSelected() && !isChecked)
					root().selectedChild = null;
				else if (!isSelected() && isChecked)
					root().selectedChild = TreeView.this;
				redraw();
			}
		});
		
		v.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Log.v("View clicked", v.toString());
					
					toggle();
					v.performHapticFeedback(VIRTUAL_KEY);
				} catch (Exception e) {
					StorkClientActivity.showToast(e.getMessage());
				}
			}
		});
		
		v.invalidate();
		
		return v;
	}
	
	public boolean isSelected() {
		return root().selectedChild == this;
	}
	
	public TreeView getChild(int i){
		if (i == 0) return this;
		for (TreeView tv: children){
			int h = tv.height();
			if (i <= h)
				return tv.getChild(i-1);
			i -= h;
		} return null;
	}
	
	public int getFlatPosition(){
		int i = parent.getFlatPosition()+1;
		for(TreeView tv: parent.children){
			if(tv == this){
				break;
			}
			i += tv.height();
		}
		return i;
	}
	
	protected int depth() {
		return parent.depth()+1;
	}

	public String getPath() {
		return getURI().getPath();
	}
	
	public URI getURI() {
		String n = name+(dir ? "/" : "");
		return parent.getURI().resolve("./"+n).normalize();
	}
	
	public String getCred() {
		return root().getCred();
	}
	public void open() {
		setOpen(true);
	} public void close() {
		setOpen(false);
	} public void toggle() {
		if(this.name.equals("..")){
			//call init by changing the uri
				Log.v("value of root", this.root().toString());
				TreeViewRoot dummyRoot = this.root();
				dummyRoot.init(dummyRoot.getURI().resolve(".."));
				refreshData();
		}
		setOpen(!open);
	} public void setOpen(boolean v) {
		open = v;
		open = isOpen();
		if (open && !fetched)
			fetchChildren();
		refreshData();
	} public boolean isOpen() {
		return dir && open;
	}
	public void refreshData() {
		root().refreshData();
	}
	
	public String toString() {
		return name;
	}

	public synchronized void asyncFetchChildren() {
		if (fetching)
			return;
		fetching = true;
		new Thread() {
			public void run() {
				fetchChildren();
			}
		}.start();
	}
	
	public Ad fetchChildren() {
		if (dir) try {
			final Ad listing = fetchListingData();
			fetched = true;
			post(new Runnable() {
				public void run() {
					createTreeViews(listing);
				}
			});
			return listing;
		} catch (Exception e) {
			error = true;
			StorkClientActivity.showToast(e.getMessage());
			return new Ad();
		} return new Ad();
	}

	public void post(Runnable runnable) {
		root().post(runnable);
	}

	private Ad fetchListingData() {
		URI uri = getURI();

		if (!dir) return new Ad();
		
		// Check if we can get listings from the cache.
		Ad listing = Cache.getFromCache(uri);

		if (listing != null) {
			Log.v("TreeView", "Retrieved from cache:"+uri);
		} else {
//			for(prefetch_and_cache p : StorkClientActivity.pac)
//				p.interrupt();
			listing = Server.getListings(this);
		//	Log.v("Listing from Directory", listing.toString());

			Log.v(getClass().getSimpleName(), "Retrieved from dls");

			Cache.addToCache(uri, listing);
		}
		return listing;
	}
	
	// Draw all of the treeviews below this treeview.
	private void createTreeViews(Ad ad) {
		children.clear();
		if (ad.has("files")) for (Ad a : ad.getAds("files")) {
			boolean is_dir = a.get("dir", "").equals("true");
			if(this == root() && children.size() == 0) children.add(new TreeView(this, "..", true));
			children.add(new TreeView(this, a.get("name"), is_dir));
		}
		redraw();
	}
	
	public TreeViewRoot root() {
		return parent.root();
	}
	
	public void redraw() {
		root().redraw();
	}
}
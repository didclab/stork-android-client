package stork;

import java.net.URI;
import java.util.*;

import stork.main.StorkClientActivity;
import static stork.main.StorkClientActivity.inflater;
import android.util.Log;
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
public class TreeView extends LinearLayout {
	public TreeView parent;
	public String name;
	public boolean dir = false;
	public boolean open;
	public LinearLayout view = null;
	public boolean fetched = false;
	
	protected TreeView(){
		super(StorkClientActivity.context);
		setOrientation(VERTICAL);
		dir = true;
	}
	
	public TreeView(TreeView parent, String name, boolean dir) {
		this();
		this.parent = parent;
		this.name = name;
		this.dir = dir;
		view = (LinearLayout) inflater().inflate(R.layout.treenode, null);
		view.addView(this);
		view.setTag(this);
		
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					toggle();
				} catch (Exception e) {
					StorkClientActivity.showToast(e.getMessage());
				}
			}
		});
		
		setPadding(20, 0, 0, 0);
		setOpen(false);
		
		ImageView iv = (ImageView) view.findViewById(R.id.icon);
		iv.setImageResource(dir ? R.drawable.folder : R.drawable.text);
		
		TextView textView = (TextView) view.findViewById(R.id.label);
		textView.setText(toString());
		
		CheckBox c = (CheckBox) view.findViewById(R.id.check);
		c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onChecked(isChecked ? TreeView.this : null);
			}
		});
	} 

	public String getPath() {
		return getURI().getPath();
	}
	
	public URI getURI() {
		return parent.getURI().resolve("./"+this).normalize();
	}
	
	public String getCred() {
		return parent.getCred();
	}
	
	public void open() {
		setOpen(true);
	} public void close() {
		setOpen(false);
	} public void toggle() {
		setOpen(!open);
	} public void setOpen(boolean v) {
		open = v;
		open = isOpen();
		setVisibility(open ? VISIBLE : GONE);
		redraw();
	} public boolean isOpen() {
		// We should always consider root views to be open.
		return dir && (open || name == null);
	}

	public String toString() {
		return name+(dir ? "/" : "");
	}
	
	public int getChildCount() {
		if (dir && isOpen()) try {
			if (dir && !fetched) fetchChildren();
			return super.getChildCount();
		} catch (Exception e) {
			// Fall through.
		} return 0;
	}
	
	public View getChildAt(int i) {
		if (dir && !fetched) fetchChildren();
		return super.getChildAt(i);
	}
	
	public TreeView getChildTreeView(int i) {
		return (TreeView) getChildAt(i).getTag();
	}
	
	public void redraw() {
		parent.postInvalidate();
	}

	public Ad fetchChildren() {
		try {
			Ad listing = fetchListingData();
			createTreeViews(listing);
			return listing;
		} catch (Exception e) {
			StorkClientActivity.showToast(e.getMessage());
			return new Ad();
		}
	}

	public Ad fetchListingData() {
		URI uri = getURI();

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
		System.out.println("About to create all TreeViews");

		if (ad.has("files")) for (Ad a : ad.getAds("files")) {
			TreeView t = new TreeView(this, a.get("name"), a.has("dir"));
			addView(t.view);
			StorkClientActivity.queue.add(t);
		}

		fetched = true;
		redraw();
	}
	
	//bubbles up the selected child to TreeViewRoot
	//Call this when a new child is selected.
	public void onChecked(TreeView tv){
		parent.onChecked(tv);
	}
	//update the UI
	public void unselect(){
		CheckBox c = (CheckBox) view.findViewById(R.id.check);
		c.setSelected(false);
	}
	
}
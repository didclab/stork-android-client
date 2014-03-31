package stork;

import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static stork.main.StorkClientActivity.inflater;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import stork.ad.Ad;
import stork.cache.Cache;
import stork.main.R;
import stork.main.StorkClientActivity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * DirectoryItem Tree Nodes used in List View. Executing this thing in the
 * background causes it to toggle state and potentially fetch listing data.
 */
public class TreeView {

	public TreeView parent;
	public String name;
	public List<TreeView> children;
	public boolean dir = false;
	public boolean open = false;

	public boolean isSelected = false;

	public boolean fetching = false;
	public boolean fetched = false;
	public boolean error = false;

	public TreeView(TreeView parent, String name, boolean dir) {
		this.parent = parent; //there is no parent of root.
		this.name = name;
		this.dir = dir;
		children = new LinkedList<TreeView>();
	}

	public int height() {
		if (!isOpen())
			return 1;
		int t = 1;
		for (TreeView c : children)
			t += c.height();
		return t;
	}

	private class ViewHolder {
		ImageView imgV;
		CheckBox checkBox;
		TextView txtV;
	}

	// this method is called for a particular child.
	// the position is determined from the TreeViewRoot.
	protected View convertView(LinearLayout v) {
		ViewHolder holder;
		if (v == null) {
			v = (LinearLayout) inflater().inflate(R.layout.treenode, null);
			holder = new ViewHolder();
			holder.imgV = (ImageView) v.findViewById(R.id.icon);
			holder.txtV = (TextView) v.findViewById(R.id.label);
			holder.checkBox = (CheckBox) v.findViewById(R.id.CheckBox);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		int h = isOpen() ? 10 : 0;
		v.setPadding(depth() * 20, 0, 0, h);

		holder.imgV.setImageResource(dir ? R.drawable.folder
				: R.drawable.file_img);
		holder.txtV.setText(toString());

		holder.checkBox.setChecked(isSelected);
		
		Click click = new Click(holder.checkBox, this);//implements onClick
		v.setOnClickListener(click);
		v.invalidate();
		return v;
	}

	private class Click implements View.OnClickListener {
		CheckBox c;
		TreeView TV;

		Click(CheckBox c, TreeView tv) {
			this.c = c;
			TV = tv;
		}

		@Override
		public void onClick(View v) {
			try {
				// if the treeview was selected and checkbox was already
				// checked then uncheck and unselect
				TreeViewRoot root = root();
				if (isSelected()) {
					Log.v("View Clicked", "c is unchecked");
					Log.v("Deletion success?", root.selectedChild.remove(TV)+"");
					isSelected = false;
					c.setChecked(false);
				} else {
					Log.v("View Clicked", "c is checked");
										
					root.selectedChild.add(TV);
					isSelected = true;
					c.setChecked(true);
				}
				toggle();
				refreshData();
				v.performHapticFeedback(VIRTUAL_KEY);
			} catch (Exception e) {
				StorkClientActivity.showToast(e.getMessage());
			}
		}

	}

	public boolean isSelected() {
		TreeViewRoot root = root();
		if (root != null) {
			if (root.selectedChild != null) {
				Iterator<TreeView> it = root().selectedChild.iterator();
				while (it.hasNext()) {
					if (it.next() == TreeView.this && TreeView.this.isSelected) {
						return true;
					}
				}
			} else {
				if(root.selectedChild == null)
					root.selectedChild = new ArrayList<TreeView>();
				
				return false;
			}
		}
		return false;
	}
	
	public TreeView getChild(int i) {
		if (i == 0)
			return this;
		for (TreeView tv : children) {
			int h = tv.height();
			if (i <= h)
				return tv.getChild(i - 1);
			i -= h;
		}
		return null;
	}

	public int getFlatPosition() {
		int i = parent.getFlatPosition() + 1;
		for (TreeView tv : parent.children) {
			if (tv == this) {
				break;
			}
			i += tv.height();
		}
		return i;
	}

	protected int depth() {
		return parent.depth() + 1;
	}

	public String getPath() {
		return getURI().getPath();
	}

	public URI getURI() {
		String n = name + (dir ? "/" : "");
		// Log.v("URL", parent.getURI().resolve("./"+n).toString());
		return parent.getURI().resolve("./" + n).normalize();
	}

	public String getCred() {
		return root().getCred();
	}

	public void open() {
		setOpen(true);
	}

	public void close() {
		setOpen(false);
	}

	public void toggle() {
		if (this.name.equals("..")) {
			TreeViewRoot dummyRoot = this.root();
			URI dummyURI = dummyRoot.getURI();
			try {
				dummyRoot.init(dummyURI.resolve(dummyURI.relativize(new URI(
						".."))));
			} catch (Exception e) {
				Log.v("TreeView", e.getMessage());
			}
			refreshData();
		}
		setOpen(!open);
	}

	public void setOpen(boolean v) {
		open = v;
		open = isOpen();
		if (open && !fetched)
			fetchChildren();
		refreshData();
	}

	public boolean isOpen() {
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
		if (dir)
			try {
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
			}
		return new Ad();
	}

	public void post(Runnable runnable) {
		root().post(runnable);
	}

	private Ad fetchListingData() {
		URI uri = getURI();
		if (!dir)
			return new Ad();

		// Check if we can get listings from the cache.
		Ad listing = Cache.getFromCache(uri);

		if (listing != null) {
			// Data is in the cache.
		} else {
			File root = null;
			if (uri.getHost().equals("localhost")) {
				// Log.v("value of this", this.getURI().getHost());
				root = new File(Environment.getExternalStorageDirectory() + "");
				if (isSelected)
					root = new File(Environment.getExternalStorageDirectory()
							+ "/" + this);

				listing = new Ad();
				for (File f : root.listFiles()) {
					Ad a = new Ad("name", f.getName());
					a.put(f.isDirectory() ? "dir" : "file", true);
					listing.put(a);
				}
				return new Ad("files", listing);
			} else
				listing = Server.getListings(this);

			Log.v(getClass().getSimpleName(), "Retrieved from dls");

			Cache.addToCache(uri, listing);
		}
		return listing;
	}

	// Draw all of the treeviews below this treeview.
	private void createTreeViews(Ad ad) {
		children.clear();
		if (ad.has("files"))
			for (Ad a : ad.getAds("files")) {
				boolean is_dir = a.get("dir", "").equals("true");
				if (this == root()
						&& (!this
								.getURI()
								.toString()
								.equals(this.getURI().getScheme() + "://"
										+ this.getURI().getHost()))
						&& children.size() == 0
						&& (!this.getURI().getHost().equals("localhost"))) {

					children.add(new TreeView(this, "..", true));
				}
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

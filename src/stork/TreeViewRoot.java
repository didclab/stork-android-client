package stork;

import java.net.URI;

import stork.ad.Ad;
import stork.main.R;
import stork.main.R.id;
import android.util.Log;
import android.view.View;
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
	
	public TreeViewRoot(String n, View v) {
		side = n;
		view = v;
		v.setVisibility(View.VISIBLE);
	}
	public void redraw() {
		view.postInvalidate();
	}

	public boolean isOpen(){
		return true;
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
		
		ScrollView v = (ScrollView) view.findViewById(R.id.listview);
		v.removeAllViews();

		// Update the list. This may throw.
		fetchChildren();
		v.addView(this);
		
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
				fetchChildren();
			}
		});
		
		view.findViewById(R.id.search_button).setVisibility(View.GONE);
		return this;
	}
	// Reset the list and UI.
	public void reset() {
		selectedChild = null;
		uri = null;
		view.findViewById(R.id.search_button).setVisibility(View.VISIBLE);
	}
	
	// select the transferring child; update the current selection.
	public void onChecked(TreeView tv){
		if(selectedChild != null && selectedChild != tv){
			selectedChild.unselect();
		}
		selectedChild = tv;
	}
}
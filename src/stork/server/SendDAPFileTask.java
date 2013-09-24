package stork.server;

import java.net.URI;

import stork.ad.Ad;
import android.os.AsyncTask;
import android.util.Log;
import stork.Server;
import stork.TreeView;

public class SendDAPFileTask extends AsyncTask<Void, Void, Ad> {
	
	TreeView  from, to;
	Exception error;
	
	public SendDAPFileTask(TreeView from, TreeView to) {
		this.to = to;
		this.from = from;
	}
	
	public String toString(){
		return "Sending : " + from + "\nTo : " + to;
	}

	protected Ad doInBackground(Void... params) {
		return execute();
	}
	
	public Ad execute() {
		Log.v(getClass().getSimpleName(), "Sending job file...");

		Ad ad = new Ad("src.uri",   from.getURI().toASCIIString())
		          .put("src.cred",  from.getCred())
		          .put("dest.uri",  to.getURI().toASCIIString())
		          .put("dest.cred", to.getCred());
		return Server.sendRequest("/api/stork_submit", ad, "POST");
	}
 }

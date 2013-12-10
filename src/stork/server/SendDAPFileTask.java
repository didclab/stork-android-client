package stork.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import stork.Server;
import stork.TreeView;
import stork.ad.Ad;
import stork.main.RowData;
import android.os.AsyncTask;
import android.util.Log;

public class SendDAPFileTask extends AsyncTask<Void, Void, Ad> {
	
	List<TreeView> from;
	TreeView  to;
	Exception error;
	ArrayList<RowData> options;
	
	public SendDAPFileTask(List<TreeView> from, TreeView to, ArrayList<RowData> r) {
		this.to = to;
		this.from = from;
		this.options = r;
	}
	
	public String toString(){
		return "Sending : " + from + "\nTo : " + to;
	}

	protected Ad doInBackground(Void... params) {
		return execute();
	}
	//{"src":{"uri":["ftp://storkcloud.org/doc1","ftp://storkcloud.org/doc2"]},"dest":{"uri":["ftp://storkcloud.org/"]},
	//"options":{"optimizer":null,"overwrite":true,"verify":false,"encrypt":false,"compress":false}}
	
	public Ad execute() {
		Log.v(getClass().getSimpleName(), "Sending job file...");
		int counter = 0;
		List<String> uri = new LinkedList<String>();
		
		while(counter < from.size()) {
			uri.add(from.get(counter).getURI()+"");
			counter++;
		}
		
		Ad ad = new Ad("src.uri", uri)
			.put("src.cred", from.get(0).getCred())
			.put("dest.uri", to.getURI().toASCIIString())
			.put("dest.cred", to.getCred());
		Server.xferOptimization = options.get(0).isSelected();
		Server.overWrite = options.get(1).isSelected();
		Server.fileIntegrity = options.get(2).isSelected();
		Server.EdataChannel = options.get(3).isSelected();
		Server.CdataChannel = options.get(4).isSelected();
		
		return Server.sendRequest("/api/stork/submit", ad, "POST");
	}
 }

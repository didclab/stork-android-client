package stork.listeners;

import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import stork.DatabaseWrapper;
import stork.ad.Ad;
import stork.main.StorkClientActivity;
import stork.server.SendDAPFileTask;

/**
 * if DAP file is confirmed, its sent to STORK server and a record of it is made
 * in the database, else dialogue is simple closed.
 * 
 * @author rishi baldawa
 * 
 */
public class ConfirmDAPClickListener implements DialogInterface.OnClickListener {

	private SendDAPFileTask dap;
	private DatabaseWrapper dbHelper;
	private Context context;
	private int dap_id;
	private String src, dest;

	/**
	 * 
	 * @param cntxt
	 * @param DAP file
	 * @param database connection
	 */
	public ConfirmDAPClickListener(Context cntxt,SendDAPFileTask file,DatabaseWrapper db){
		//TODO check for nulls
		dap = file;
		dbHelper = db;
		context = cntxt;
	}


	public void onClick(DialogInterface dialog, int which) {
		// -1 == Yes
		// -2 == No
		switch (which) {
		case -1: try {
			Ad response = dap.execute();

			Log.d(getClass().getSimpleName(), "DAP File Sent. Response : " + response);
			String s = "Job successfully submitted. Job id: "+response.get("job_id");
			StorkClientActivity.showToast(s);
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(getClass().getSimpleName(), e.toString());
			StorkClientActivity.showToast(e.getMessage());
		}
		case -2:
			dialog.cancel();
		}
	}
}
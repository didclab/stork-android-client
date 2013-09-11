package stork.listeners;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import stork.DatabaseWrapper;
import stork.Server;
import stork.adapter.ProgressListAdapter;
import stork.framework.ProgressView;

public class ProgressMenuClickListener implements
		DialogInterface.OnClickListener {
	Context context;
	Long jobId;
	ProgressListAdapter pla;
	List<ProgressView> progress;

	public ProgressMenuClickListener(Context context, ProgressListAdapter pla, List<ProgressView> progress,Long jobId) {
		this.context = context;
		this.jobId = jobId;
		this.pla = pla;
		this.progress = progress;
	}

	
	public void onClick(DialogInterface dialog, int which) {

		System.out.println("Choice : " + which);

		switch (which) {
		case 0: {
			String[] details = new String[4];
			boolean found = false;
			for(ProgressView p : progress){
				if(p.getJobID() == jobId){
					found = true;
					details[0] = "Job ID: " + Long.toString(p.getJobID());
					details[1] = "Source: " + p.getServer_one();
					details[2] = "Destination: " + p.getServer_two();
					details[3] = "Progress: " + Integer.toString(p.getProgress());
					break;
				}
			}
			
		
			if(found){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
	        builder.setTitle("Job Details");
			builder.setItems(details, null);
	        AlertDialog alert = builder.create();
	        
	      //set width n height
		    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		    lp.copyFrom(alert.getWindow().getAttributes());
		    lp.width = WindowManager.LayoutParams.FILL_PARENT;
		    lp.height = WindowManager.LayoutParams.FILL_PARENT;
		    alert.show();
		    alert.getWindow().setAttributes(lp);
			}
			break;
			
		}
		case 1: {
			String status = Server.sendJobCancelRequest(jobId);
			Log.e (getClass().getSimpleName(),"Stork Status : " + status);
		}
		case 2: {
			try
			{
			DatabaseWrapper dbHelper = new DatabaseWrapper(context.getApplicationContext());
			boolean success = dbHelper.onDelete(jobId);
			dbHelper.close();
			if(success)			Log.v (getClass().getSimpleName(),"deleted the job");
			else 				Log.e (getClass().getSimpleName(),"Unable to delete the job");
			Toast.makeText(context, "Deleted? : " + success, Toast.LENGTH_SHORT).show();
			if(success) {
				int i=0;
				for(i=0;i<progress.size();i++){
					if(progress.get(i).getJobID() == jobId) break;
				}
				// i has to be less than progress.size() since the job Id exists
				progress.remove(i);
				pla.notifyDataSetChanged();
			}
			}//end of try
			catch(Exception e)
			{
				Log.v(getClass().getSimpleName(), e.toString());
				Toast.makeText(context, e.toString(),Toast.LENGTH_SHORT).show();
			}
			break;
		}
		case 3: {
			break;
		}
		default:
			break;
		}
	}


}

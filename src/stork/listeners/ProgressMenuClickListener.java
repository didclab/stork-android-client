package stork.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import stork.Server;
import stork.adapter.ProgressListAdapter;
import stork.framework.ProgressView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

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
			LinkedList <String> details = new LinkedList<String>();
			boolean found = false;
			for(ProgressView p : progress){
				if(p.job_id == jobId){
					found = true;
					details.add("Job ID: " + p.job_id);
					for(String u : p.src.uri)
						details.add("Source: " +u);	
					details.add("Destination: " + p.dest.uri[0]);
					details.add("Progress: " + p.getProgress());
					break;
				}
			}

			if(found){
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Job Details");
				builder.setItems(details.toArray(new String[details.size()]), null);
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
		case 2 : {
			String message = "";
			for(ProgressView p : progress){
				if(p.job_id == jobId && p.message!=null){
					message= p.message;
					break;
				}
				else {
					message = "No message from server";
				}
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Message");
			builder.setMessage(message);
			AlertDialog alert = builder.create();

			//set width n height
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(alert.getWindow().getAttributes());
			lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			alert.show();
			alert.getWindow().setAttributes(lp);

			break;
		}
		case 3: {//close
			break;
		}
		case 4: {//Remove the job from the list
			try
			{
				ListIterator<ProgressView> it = progress.listIterator();
				while(it.hasNext()) {
					if(it.next().job_id == jobId) {
						it.remove();
						break;
					}
				}
				pla.notifyDataSetChanged();
			}//end of try
			catch(Exception e)
			{
				Log.v(getClass().getSimpleName(), e.toString());
				Toast.makeText(context, e.toString(),Toast.LENGTH_SHORT).show();
			}
			break;
		}
		default:
			break;
		}
	}


}

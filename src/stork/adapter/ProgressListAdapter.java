package stork.adapter;

import java.util.List;


import stork.framework.ProgressView;
import stork.listeners.OnJobProgressClickListener;
import stork.main.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Adapter for viewing progress views in a list
 * 
 * @author Rishi
 *
 */
public class ProgressListAdapter extends ArrayAdapter<ProgressView> {

	private List<ProgressView> progressList;
	private Activity context;

	public ProgressListAdapter(Activity context, List<ProgressView> list, int id) {
		super(context, id, list);
		this.context = context;
		this.progressList = list;
	}

	private static class ViewHolder {
		protected TextView server1;
		protected TextView server2;
		protected TextView jobID;
		protected TextView progressMessage;
		protected ProgressBar progressbar;
		protected TextView tv_progress;
	}
	
	private ViewHolder viewHolder;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			// link up viewholder and list row layout
			viewHolder = new ViewHolder();
			LayoutInflater inflator = context.getLayoutInflater();
			convertView = inflator.inflate(R.layout.rowprogresslayout, null);
			viewHolder.progressbar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			viewHolder.tv_progress = (TextView) convertView.findViewById(R.id.tv_progress);
			viewHolder.progressMessage = (TextView) convertView.findViewById(R.id.progressMessage);
			viewHolder.jobID = (TextView) convertView.findViewById(R.id.jobID);
			viewHolder.server1 = (TextView) convertView.findViewById(R.id.progressTextOne);
			viewHolder.server2 = (TextView) convertView.findViewById(R.id.progressTextTwo);
			viewHolder.progressbar.setProgress(0);
			viewHolder.progressbar.setMax(100);
			
			// add listeners
			//OnJobProgressClickListener jpListener = new OnJobProgressClickListener(progressList.get(position).getJobID(),this,progressList);
			
			viewHolder.server1.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// just show the url in a box
			        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
			        builder.setMessage(viewHolder.server1.getText());
			        builder.create().show();
				}
			});
			viewHolder.server2.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// just show the url in a box
			        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
			        builder.setMessage(viewHolder.server2.getText());
			        builder.create().show();
				}
			});
			//set tag
			convertView.setTag(viewHolder);
		} else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		OnJobProgressClickListener jpListener = new OnJobProgressClickListener(getItem(position).job_id,this,progressList);
		viewHolder.progressbar.setOnClickListener(jpListener);
		viewHolder.progressMessage.setOnClickListener(jpListener);
		viewHolder.jobID.setOnClickListener(jpListener);
		
		long jobID = progressList.get(position).job_id;
		viewHolder.jobID.setText(Long.toString(jobID));
		int progress = progressList.get(position).getProgress();
		int color = progressList.get(position).getColor();
		String status = progressList.get(position).status;
		String message = progressList.get(position).message;
		
		Log.v(jobID+" status: ", status+" Color: "+color+" progress: "+progress);
		
		if (progress < 0 || status.equals("failed")) {
			viewHolder.progressbar.setProgress(progress);
			viewHolder.progressbar.setVisibility(View.VISIBLE);
			if(!status.equals("processing"))
				viewHolder.progressbar.getProgressDrawable().setColorFilter(color, Mode.SRC_IN);
			viewHolder.tv_progress.setTextColor(Color.BLACK);
			viewHolder.tv_progress.setTypeface(null,Typeface.BOLD);
			//holder.tv_progress.setText(progress+"");
			viewHolder.tv_progress.setText(status);
		} 
		else{ 
			viewHolder.progressbar.setProgress(progress);
			viewHolder.progressMessage.setVisibility(View.INVISIBLE);
			viewHolder.progressbar.setVisibility(View.VISIBLE);
			viewHolder.tv_progress.setTextColor(Color.BLACK);
			viewHolder.tv_progress.setTypeface(null,Typeface.BOLD);
			
			if(status.equals("removed") || status.equals("complete")){
				viewHolder.progressbar.getProgressDrawable().setColorFilter(color, Mode.SRC_IN);
				viewHolder.tv_progress.setText(status);
			}
			else
				viewHolder.tv_progress.setText(progress+"%");
		}
		String srcDetails = "";
		for(String u : progressList.get(position).src.uri)
			srcDetails += "" +u+" ";	
		viewHolder.server1.setText(srcDetails);
		viewHolder.server2.setText(progressList.get(position).dest.uri[0]);
		notifyDataSetChanged();
		return convertView;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
}

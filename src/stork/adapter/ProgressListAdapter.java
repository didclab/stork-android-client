package stork.adapter;

import java.util.List;

import stork.main.R;
import stork.listeners.OnJobProgressClickListener;
import stork.framework.ProgressView;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
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

	/**
	 * Default constructor
	 * 
	 * @param context
	 * @param list
	 * @param id
	 */
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {

			// link up viewholder and list row layout
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.rowprogresslayout, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.progressbar = (ProgressBar) view.findViewById(R.id.progressBar);
			viewHolder.tv_progress = (TextView) view.findViewById(R.id.tv_progress);
			viewHolder.progressMessage = (TextView) view.findViewById(R.id.progressMessage);
			viewHolder.jobID = (TextView) view.findViewById(R.id.jobID);
			viewHolder.server1 = (TextView) view.findViewById(R.id.progressTextOne);
			viewHolder.server2 = (TextView) view.findViewById(R.id.progressTextTwo);
			viewHolder.progressbar.setMax(100);
			
			// add listeners
			OnJobProgressClickListener jpListener = new OnJobProgressClickListener(progressList.get(position).getJobID(),this,progressList);
			viewHolder.progressbar.setOnClickListener(jpListener);
			viewHolder.progressMessage.setOnClickListener(jpListener);
			viewHolder.jobID.setOnClickListener(jpListener);
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
			view.setTag(viewHolder);
		} else {
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.jobID.setText(Long.toString(progressList.get(position).getJobID()));
		int progress = progressList.get(position).getProgress();
		if (progress < 0) {
			holder.progressbar.setProgress(progressList.get(position).getProgress());
			holder.progressbar.setVisibility(View.INVISIBLE);
			holder.progressMessage.setVisibility(View.VISIBLE);
			holder.progressMessage.setText(progressList.get(position).getMessage());
			holder.tv_progress.setText(Integer.toString(progressList.get(position).getProgress()));
			
	} 
		else{
			holder.progressbar.setProgress(progressList.get(position).getProgress());
			holder.progressMessage.setVisibility(View.INVISIBLE);
			holder.progressbar.setVisibility(View.VISIBLE);
			holder.tv_progress.setTextColor(Color.BLACK);
			holder.tv_progress.setTypeface(null,Typeface.BOLD);
			holder.tv_progress.setText(Integer.toString(progressList.get(position).getProgress())+"%");//for percentage
		}
		holder.server1.setText(progressList.get(position).getServer_one());
		holder.server2.setText(progressList.get(position).getServer_two());
		return view;
	}
	
}

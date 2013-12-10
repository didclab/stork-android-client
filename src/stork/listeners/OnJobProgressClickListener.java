package stork.listeners;

import java.util.List;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import stork.adapter.ProgressListAdapter;
import stork.framework.ProgressView;
//import stork.framework.ProgressView;

public class OnJobProgressClickListener implements View.OnClickListener{
	
	private Long id;
	private ProgressListAdapter pla;
	private List<ProgressView> progress;
	private final CharSequence[] actions = { "Job Details", "Cancel Job","Message", "Close" };
	//the childview clicks are handled here...
	public OnJobProgressClickListener(Long id,ProgressListAdapter pla, List<ProgressView> progress) {
		this.id = id;
		this.pla = pla;
		this.progress = progress;
	}
	public void onClick(View v) {
		Log.v("ID : ", id+"");
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Job ID : " + id);
		builder.setItems(actions ,new ProgressMenuClickListener(v.getContext(),pla,progress, id));
        builder.create().show();
	}
}
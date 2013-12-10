package stork.main;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

/**
 * Custom Spinner adapter
 * 
 *
 */
public class CustomSpinnerAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;
	
	// dataset that maps to individual rows
	private ArrayList<RowData> mDataSet;

	private LayoutInflater mInflater;

	public CustomSpinnerAdapter(Context context, ArrayList<RowData> list) {
		mContext = context;
		mDataSet = list;

		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	// returns total number of rows in the spinner
	@Override
	public int getCount() {		
		return mDataSet.size();
	}


	/**
	 * return the name of the option in a particular row
	 */
	@Override
	public Object getItem(int position) {		
		return mDataSet.get(position).getName();
	}

	
	// not used; we will return position
	@Override
	public long getItemId(int position) {

		return position;
	}

	
	// define the view to be drawn for each row in Spinner, including its data and state
	// since we are drawing check boxes we will set text for each check box and its checked status
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.spinner_row_item, null);
		}

		CheckBox option = (CheckBox) convertView.findViewById(R.id.option);

		option.setText(mDataSet.get(position).getName());
		option.setChecked(mDataSet.get(position).isSelected());
		
		// we need to set tag, so that we can identify the row clicked in on click listener callback
		option.setTag(position);

		option.setOnClickListener(this);

		return convertView;
	}


	/**
	 * Since we are going to re-use whatever we have drawn in getView, no need to override this
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		return super.getDropDownView(position, convertView, parent);
	}

	// since we have check boxes, note that onItemSelected will not work for the Spinner since
	// check boxes will consume the click event
	// So we will attach a click listener on individual check boxes and record the selection state
	@Override
	public void onClick(View v) {
		
		// obtain row position by reading tag (see getView implementation)
		int position = (Integer) v.getTag();	
		
		Log.d("Adapter", "Position : " + position);
		
		// Safe cast; since we know view is checkbox
		CheckBox checkBox = (CheckBox)v;
		
		Log.d("Adapter", " Checkbox#isSelected(position) :" + checkBox.isChecked());

		// record the view state
		mDataSet.get(position).setSelected(checkBox.isChecked());
	}
	
	
	/**
	 * Public method that will list names of all the options selected by the user
	 * @return
	 */
	public ArrayList<String> getSelections(){
		ArrayList<String> selections = new ArrayList<String>();
		
		for(RowData data: mDataSet){
			if(data.isSelected()){
				selections.add(data.getName());
			}
		}
		
		return selections;
	}

}


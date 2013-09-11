package stork.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;
import stork.TreeView;
import stork.main.R;
import stork.listeners.DirListClickListener;

//import stork.framework.Treeview;

/**
 * ListView adapter for file lists which will be viewed as directories
 * 
 * @author Sanat K. Tripathi
 * 
 */
/*
 * How to set the data which is not visible, for e.g. when the root directory is
 * clicked, i wrote that all of its subdirectories should be set however, this
 * ain't happen, because the view visible is just the total number pushed onto
 * the screen. when the code tries to access the data beyond the current view,
 * it gets null value and the code comes out of the block. Now when the user
 * tries to scroll, each view's object is reused, the code goes to the else part
 * of the program i.e. where the convert view is not null.
 * 
 * Two mysteries to resolve 1. how does the views which are not visible get set?
 * 2. Y does that not happen with the immediate scrollable views i.e. the views
 * which are first items when the listview is scrolled
 */

public class InteractiveListArrayAdapter { }/*extends ListAdapter {

	private List<Treeview> list;
	private final Activity context;
	private final ListView listView;
	private LayoutInflater inflator;

	public InteractiveListArrayAdapter(ListView lv, Activity context,
			List<Treeview> list, int id) {
		super(context, id, list);
		this.context = context;
		this.list = list;
		this.listView = lv;
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private static class ViewHolder {
		protected TextView text;
		protected CheckBox checkbox;
		protected ImageView image;
		protected ProgressBar progressbar;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		// Log.v("getView", "called "+position);
		// "if" clause is invoked for the first time
		try {
			if (convertView == null) {
				convertView = inflator.inflate(R.layout.rowbuttonlayout, null);

				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.label);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.check);
				holder.image = (ImageView) convertView
						.findViewById(R.id.row_icon);
				holder.progressbar = (ProgressBar) convertView
						.findViewById(R.id.progressBarLoadingDir);

				// viewHolder.checkbox.setChecked();
				convertView.setTag(holder);
				holder.checkbox.setTag(list.get(position));
				// holder.checkbox.setTag(list.get(position));

			} // if convertView == null

			else {
				holder = (ViewHolder) convertView.getTag();
				((ViewHolder) convertView.getTag()).checkbox.setTag(list
						.get(position));
			}

			final ViewHolder viewHolder = holder;

			holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					try {
						Treeview element = (Treeview) viewHolder.checkbox
								.getTag();
						element.setSelected(buttonView.isChecked());
						Log.v("OnCheck Changed, Position = ",
								Integer.toString(position));
						// /based on the
						Treeview tv = list.get(position);

						if (tv.isDir() && isChecked) {
							// row is even
							List<Treeview> directories = tv.getChild();
							Log.v("Child Count = ", Integer
									.toString(directories.size()));
							for (int i = position, j = 0; i < (position + directories
									.size()); i++, j++) {
								directories.get(j).setSelected(true);
								Log.v("Directory", "selected!");

							}// end of for
						}

						else if (tv.isDir() && !(isChecked)) {
							List<Treeview> directories = tv.getChild();
							for (int i = position, j = 0; i < (position + directories
									.size()); i++, j++) {
								directories.get(j).setSelected(false);
							}// end of for
						}

						else if (!(tv.isDir()) && isChecked) {
							List<Treeview> directories = tv.getChild();
							for (int i = position, j = 0; i < (position + directories
									.size()); i++, j++) {
								directories.get(j).setSelected(true);
								Log.v("Directory", "selected!");
							}// end of for

						} else {
							Log.v("Else", "Reached!");
						}

						// tell the listview adapter(meaning this class
						// object) that the data has changed
						// so it needs to redraw itself.
						notifyDataSetChanged();
					} catch (Exception e) {
						Log.v(getClass().getSimpleName(), e.toString());
					}

				}
			});

			// get the data item associated with the row
			Treeview tv = list.get(position);

			// set the view states based on the data saved in teh data object
			holder.checkbox.setChecked(tv.isSelected());
			holder.text.setText(tv.name);
			if (tv.isDir()) {

				holder.text.setOnClickListener(new DirListClickListener(this,
						position, list, holder.progressbar, context));
				holder.image.setImageResource(R.drawable.folder);

			} else {
				holder.text.setOnClickListener(new OnClickListener() {
					// for a file being displayed

					public void onClick(View v) {
						Toast.makeText(context, "Not a directory",
								Toast.LENGTH_SHORT).show();
					}
				});
				holder.image.setImageResource(R.drawable.text);
				holder.progressbar.setVisibility(View.GONE);

			}// end of else

			// use margins for dummy tree-list view. It's all just one big
			// single list.
			final float scale = getContext().getResources().getDisplayMetrics().density;
			int pixels = (int) (20 * scale + 0.5f);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					pixels, pixels);
			pixels = (int) (4 * scale + 0.5f);
			lp.setMargins(tv.depth * 15 + pixels, pixels, pixels, pixels);
			holder.image.setLayoutParams(lp);

			// Automating the clicks for testing the UI
			// Log.v("Value of ", " "+loginClickListener.comeOutFlag);

			// some code #2
			if (LoginClickListener.comeOutFlag < 1) {
				Thread.sleep(1000);
				if (holder.text.getText().equals("10")) {
					LoginClickListener.comeOutFlag++;
					holder.text.performClick();
				}

				//					}					

			}// end of main if


		}// end of try
		catch (Exception e) {
			Log.v(getClass().getSimpleName(), e.toString());
		}

		return convertView;
	}// end of getView

	public List<Treeview> getList() {
		return list;
	}

}// end of class*/
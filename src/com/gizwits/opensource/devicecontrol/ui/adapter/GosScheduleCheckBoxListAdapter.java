/**
 * 
 */
package com.gizwits.opensource.devicecontrol.ui.adapter;

import java.util.ArrayList;

import com.gizwits.opensource.smartlight.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @author Administrator
 *
 */
public class GosScheduleCheckBoxListAdapter extends BaseAdapter {

	ArrayList<GosScheduleCheckBoxListDateHolder> items;
	private LayoutInflater mInflater;

	public GosScheduleCheckBoxListAdapter(Context context, ArrayList<GosScheduleCheckBoxListDateHolder> items) {
		super();
		this.items = items;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_comd_listview_with_checkbox, null);
		} else {
			view = convertView;
		}
		TextView itemName = (TextView) view.findViewById(R.id.tv_item);
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_date);
		itemName.setText(items.get(position).itemName);
		checkBox.setChecked(items.get(position).checked);
		return view;
	}

}

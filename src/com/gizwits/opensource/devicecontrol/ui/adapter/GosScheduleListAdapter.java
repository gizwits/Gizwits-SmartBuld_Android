/**
 * 
 */
package com.gizwits.opensource.devicecontrol.ui.adapter;

import java.util.List;

import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.date.GosScheduleData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Administrator
 *
 */
public class GosScheduleListAdapter extends BaseAdapter {
	private Context context;

	public interface ToggleButtonClickListener {
		public void onclick(View v);
	}

	public interface DeleteButtonClickListener {
		public void onclick(View v);
	}

	private List<GosScheduleData> mList;
	private LayoutInflater mInflater;
	private DeleteButtonClickListener deleteListener;
	private ToggleButtonClickListener toggleListener;

	/**
	 * @param mList
	 * @param mContext
	 */
	public GosScheduleListAdapter(List<GosScheduleData> list, Context context, ToggleButtonClickListener toggleListener,
			DeleteButtonClickListener deleteListener) {
		mList = list;
		this.deleteListener = deleteListener;
		this.toggleListener = toggleListener;
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	public void setmList(List<GosScheduleData> mList) {
		this.mList = mList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GosScheduleViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_comd_schedule_list, null);
			holder = new GosScheduleViewHolder();
			holder.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
			holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
			holder.btnChecked = (Button) convertView.findViewById(R.id.btn_checked);
			holder.rlDelete = (RelativeLayout) convertView.findViewById(R.id.delete2);
			convertView.setTag(holder);
		} else {

			holder = (GosScheduleViewHolder) convertView.getTag();
		}

		holder.tvStatus.setText(mList.get(position).getTvStatus());
		holder.tvTime.setText(mList.get(position).getTvTime());
		holder.tvDate.setText(mList.get(position).getTvDateOrRepeat());
		holder.btnChecked.setSelected(mList.get(position).getBtnIsOpen());
		if (holder.btnChecked.isSelected()) {
			holder.tvStatus.setTextColor(context.getResources().getColor(R.color.text_black));
			holder.tvTime.setTextColor(context.getResources().getColor(R.color.text_black));
			holder.tvDate.setTextColor(context.getResources().getColor(R.color.text_black));
		} else {
			holder.tvStatus.setTextColor(context.getResources().getColor(R.color.text_gray));
			holder.tvTime.setTextColor(context.getResources().getColor(R.color.text_gray));
			holder.tvDate.setTextColor(context.getResources().getColor(R.color.text_gray));
		}
		holder.btnChecked.setTag(position);
		holder.btnChecked.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleListener.onclick(v);

			}
		});
		holder.rlDelete.setTag(position);
		holder.rlDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteListener.onclick(v);
			}
		});
		return convertView;
	}

}

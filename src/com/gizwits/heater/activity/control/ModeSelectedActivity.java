package com.gizwits.heater.activity.control;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.heater.R;

public class ModeSelectedActivity extends BaseActivity implements OnItemClickListener{

	private ListView lvMode;
	
	// 0.智能模式, 1.节能模式, 2.速热模式, 3.加热模式,4.保温模式,5.安全模式
	private int[] ivResources={R.drawable.pattern_intelligence_icon,R.drawable.pattern_energy_icon,
			R.drawable.power_fullpower,
			R.drawable.pattern_heating_icon,R.drawable.pattern_temperature_icon,R.drawable.pattern_safe_icon};
	private int[] tvResources={R.string.pattern_intelligence,R.string.pattern_energy,
			R.string.power_fullpower,R.string.pattern_heating,R.string.pattern_temperature,
			R.string.pattern_safe};
	private ModeAdapter mModeAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_selected);
		initView();
	}
	
	private void initView(){
		lvMode=(ListView) findViewById(R.id.lvMode);
		
		mModeAdapter =new ModeAdapter(this);
		lvMode.setAdapter(mModeAdapter);
		lvMode.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mModeAdapter.setSelected(position);
	}
	
	public void onClick(View view){
		switch(view.getId()){
		case R.id.ivBack:
			onBackPressed();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	class ModeAdapter extends BaseAdapter{

		/** The inflater. */
		private LayoutInflater inflater;
		
		/** The ctx. */
		private Context ctx;
		
		/** The selected. */
		private int selected=0;
		
		public ModeAdapter(Context context){
			this.inflater = LayoutInflater.from(context);
			this.ctx=context;
		}
		
		@Override
		public int getCount() {
			return ivResources.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_mode_list, null);
				holder = new ViewHolder();
				holder.ivIcon = (ImageView) convertView
						.findViewById(R.id.ivIcon);
				holder.tvDetails = (TextView) convertView
						.findViewById(R.id.tvDetails);
				holder.ivMark=(ImageView) convertView
						.findViewById(R.id.ivMark);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.ivIcon.setImageResource(ivResources[position]);
			holder.tvDetails.setText(tvResources[position]);
			
			if(position==selected){
				holder.ivMark.setVisibility(View.VISIBLE);
			}else{
				holder.ivMark.setVisibility(View.INVISIBLE);
			}
			
			return convertView;
		}

		public void setSelected(int selected) {
			this.selected = selected;
			notifyDataSetChanged();
		}
		
	}
	
	/**
	 * 
	 * ClassName: Class ViewHolder. <br/>
	 * <br/>
	 * date: 2015-1-27 14:44:48 <br/>
	 * 
	 * @author Sunny
	 */
	private static class ViewHolder {

		/** The mode ivIcon. */
		ImageView ivIcon;

		/** The mode tvDetails. */
		TextView tvDetails;
		
		/** The mode ivMark. */
		ImageView ivMark;
	}
	
}

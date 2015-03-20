/**
 * Project Name:XPGSdkV4AppBase
 * File Name:ModeSelectedActivity.java
 * Package Name:com.gizwits.heater.activity.control
 * Date:2015-3-20 14:48:07
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gizwits.heater.activity.control;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.gizwits.framework.config.JsonKeys;
import com.gizwits.framework.utils.StringUtils;
import com.gizwits.heater.R;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

/**
 * The Class ModeSelectedActivity.
 * 
 * 模式选择界面
 * 
 * @author Sunny
 */
public class ModeSelectedActivity extends BaseActivity implements
		OnItemClickListener {

	/** 模式列表控件. */
	private ListView lvMode;

	// 0.智能模式, 1.节能模式, 2.速热模式, 3.加热模式,4.保温模式,5.安全模式
	/** 模式图片资源数组. */
	private int[] ivResources = { R.drawable.pattern_intelligence_icon,
			R.drawable.pattern_energy_icon, R.drawable.power_fullpower,
			R.drawable.pattern_heating_icon,
			R.drawable.pattern_temperature_icon, R.drawable.pattern_safe_icon };
	
	/** 模式文字资源数组. */
	private int[] tvResources = { R.string.pattern_intelligence,
			R.string.pattern_energy, R.string.power_fullpower,
			R.string.pattern_heating, R.string.pattern_temperature,
			R.string.pattern_safe };
	
	
	/** 模式适配器. */
	private ModeAdapter mModeAdapter;

	/** The device data map. */
	private ConcurrentHashMap<String, Object> deviceDataMap;

	/** The statu map. */
	private ConcurrentHashMap<String, Object> statuMap = new ConcurrentHashMap<String, Object>();;

	/** 界面更新锁. */
	private boolean isLock = false;

	/** 界面更新锁解锁时间. */
	private int Lock_Time = 2000;

	/**
	 * ClassName: Enum handler_key. <br/>
	 * <br/>
	 * date: 2014-11-26 17:51:10 <br/>
	 * 
	 * @author Lien
	 */
	private enum handler_key {

		/** 更新UI界面 */
		UPDATE_UI,

		/** 解锁 */
		UNLOCK,

		/** 接收到设备的数据 */
		RECEIVED,
	}

	/**
	 * The handler.
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case RECEIVED:
				try {
					if (deviceDataMap.get("data") != null) {
						inputDataToMaps(statuMap,
								(String) deviceDataMap.get("data"));
						handler.sendEmptyMessage(handler_key.UPDATE_UI
								.ordinal());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case UPDATE_UI:
				if (isLock)
					break;

				if (statuMap != null && statuMap.size() > 0) {
					// 更新模式状态
					String mode = (String) statuMap.get(JsonKeys.MODE);
					if (!StringUtils.isEmpty(mode)) {
						mModeAdapter.setSelected(Short.parseShort(mode));
					}
				}
				break;
			case UNLOCK:
				isLock = false;
				handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
				break;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#onCreate(android.os.Bundle
	 * )
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_selected);
		initView();
	}

	/**
	 * Inits the views.
	 */
	private void initView() {
		lvMode = (ListView) findViewById(R.id.lvMode);

		mModeAdapter = new ModeAdapter(this);
		lvMode.setAdapter(mModeAdapter);
		lvMode.setOnItemClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnItemClickListener#onItemClick(android.view.View)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		isLock = true;
		handler.removeMessages(handler_key.UNLOCK.ordinal());

		mCenter.cMode(mXpgWifiDevice, position);
		mModeAdapter.setSelected(position);

		handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(), Lock_Time);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ivBack:
			onBackPressed();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	/**
	 * The Class ModeAdapter.
	 * 
	 * 模式选择适配器
	 * 
	 * @author Sunny
	 */
	class ModeAdapter extends BaseAdapter {

		/** The inflater. */
		private LayoutInflater inflater;

		/** The selected. */
		private int selected = -1;

		public ModeAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
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
				holder.ivMark = (ImageView) convertView
						.findViewById(R.id.ivMark);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.ivIcon.setImageResource(ivResources[position]);
			holder.tvDetails.setText(tvResources[position]);

			if (position == selected) {
				holder.ivMark.setVisibility(View.VISIBLE);
			} else {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		mXpgWifiDevice.setListener(deviceListener);
		mCenter.cGetStatus(mXpgWifiDevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#didReceiveData(com.xtremeprog
	 * .xpgconnect.XPGWifiDevice, java.util.concurrent.ConcurrentHashMap, int)
	 */
	@Override
	protected void didReceiveData(XPGWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int result) {
		this.deviceDataMap = dataMap;
		handler.sendEmptyMessage(handler_key.RECEIVED.ordinal());
	}

	/**
	 * 把状态信息存入表
	 * 
	 * @param map
	 *            the map
	 * @param json
	 *            the json
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void inputDataToMaps(ConcurrentHashMap<String, Object> map,
			String json) throws JSONException {
		Log.i("revjson", json);
		JSONObject receive = new JSONObject(json);
		Iterator actions = receive.keys();
		while (actions.hasNext()) {

			String action = actions.next().toString();
			Log.i("revjson", "action=" + action);
			// 忽略特殊部分
			if (action.equals("cmd") || action.equals("qos")
					|| action.equals("seq") || action.equals("version")) {
				continue;
			}
			JSONObject params = receive.getJSONObject(action);
			Log.i("revjson", "params=" + params);
			Iterator it_params = params.keys();
			while (it_params.hasNext()) {
				String param = it_params.next().toString();
				Object value = params.get(param);
				map.put(param, value);
			}
		}
		handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
	}

}

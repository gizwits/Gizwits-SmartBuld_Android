/**
 * Project Name:XPGSdkV4AppBase
 * File Name:TimerSelectedActivity.java
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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.framework.config.JsonKeys;
import com.gizwits.framework.utils.DialogManager;
import com.gizwits.framework.utils.StringUtils;
import com.gizwits.framework.utils.DialogManager.On2TimingChosenListener;
import com.gizwits.heater.R;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

/**
 * The Class TimerSelectedActivity.
 * 
 * 预约用水界面
 * 
 * @author Sunny
 */
public class TimerSelectedActivity extends BaseActivity implements
		OnClickListener {

	/** The count down tb. */
	private ToggleButton tbCountDown;

	/** The timer tb. */
	private ToggleButton tbTiming;

	/** The count down tv. */
	private TextView tvCountDown;

	/** The timer tv. */
	private TextView tvTiming;

	/** The count down hour. */
	private int CountDownHour = 0;

	/** The count down minute. */
	private int CountDownMin = 0;

	/** The timer hour. */
	private int TimerHour = 0;

	/** The timer minute. */
	private int TimerMin = 0;

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
					// 更新定时模式
					String countDown = (String) statuMap
							.get(JsonKeys.COUNT_DOWN_RESERVE);
					String timer = (String) statuMap.get(JsonKeys.TIME_RESERVE);
					if (!StringUtils.isEmpty(countDown)) {
						setCountDown(Integer.parseInt(countDown));
					}
					if (!StringUtils.isEmpty(timer)) {
						setTimer(
								(Boolean) statuMap.get(JsonKeys.RESERVE_ON_OFF),
								Integer.parseInt(timer));
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
		setContentView(R.layout.activity_timer_selected);
		initView();
		initEvent();
	}

	/**
	 * Inits the views.
	 */
	private void initView() {
		tbCountDown = (ToggleButton) findViewById(R.id.tbCountDownFlag);
		tbTiming = (ToggleButton) findViewById(R.id.tbTimingFlag);
		tvCountDown = (TextView) findViewById(R.id.tvCountDown);
		tvTiming = (TextView) findViewById(R.id.tvTiming);
	}

	/**
	 * Inits the events.
	 */
	private void initEvent() {
		tbCountDown.setOnClickListener(this);
		tbTiming.setOnClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			onBackPressed();
			break;
		// 倒计时
		case R.id.rlCountDown:
			showCountDownDialog();
			break;
		// 定时预约
		case R.id.rlTimer:
			DialogManager
					.get2WheelTimingDialog(
							this,
							new TimerDialogListener(),
							getResources()
									.getString(R.string.appointment_timer),
							TimerHour,
							TimerMin,
							getResources().getString(
									R.string.appointment_timer_label1),
							getResources().getString(
									R.string.appointment_timer_label2),false).show();
			break;
		case R.id.tbCountDownFlag:
			if (!tbCountDown.isChecked()) {
				isLock = true;
				handler.removeMessages(handler_key.UNLOCK.ordinal());

				tvCountDown.setText("00:00后");
				tbCountDown.setChecked(false);
				
				CountDownHour = 0;
				CountDownMin = 0;
				
				mCenter.cCountDown(mXpgWifiDevice, 0);

				handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(),
						Lock_Time);
			} else {
				showCountDownDialog();
			}
			break;
		case R.id.tbTimingFlag:
			isLock = true;
			handler.removeMessages(handler_key.UNLOCK.ordinal());

			mCenter.cTimerSwitch(mXpgWifiDevice, tbTiming.isChecked());
			tbTiming.setChecked(tbTiming.isChecked());

			handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(),
					Lock_Time);
			break;
		}
	}

	/**
	 * show the count down dialog.
	 */
	private void showCountDownDialog() {
		DialogManager.get2WheelTimingDialog(
				this,
				new CountDownDialogListener(),
				getResources().getString(R.string.appointment_count_down),
				CountDownHour,
				CountDownMin,
				getResources()
						.getString(R.string.appointment_count_down_label1),
				getResources()
						.getString(R.string.appointment_count_down_label2),true)
				.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		finish();
	}

	/**
	 * refresh the count down status.
	 */
	private void setCountDown(int mhour) {
		if (mhour > 0) {
			tbCountDown.setChecked(true);
		} else {
			tbCountDown.setChecked(false);
		}

		int min = mhour % 60;
		int hour = mhour / 60;

		CountDownHour = hour;
		CountDownMin = min;

		tvCountDown.setText(String.format("%02d:%02d后", hour, min));
	}

	/**
	 * refresh the timing status.
	 */
	private void setTimer(boolean isTurn, int mhour) {
		tbTiming.setChecked(isTurn);

		int min = mhour % 60;
		int hour = mhour / 60;

		TimerHour = hour;
		TimerMin = min;

		tvTiming.setText(String.format("%02d:%02d", hour, min));
	}

	/**
	 * the count down dialog listener
	 */
	private class CountDownDialogListener implements On2TimingChosenListener {

		@Override
		public void timingChosen(int HourTime, int MinTime) {
			isLock = true;
			handler.removeMessages(handler_key.UNLOCK.ordinal());

			if(HourTime==24){
				setCountDown(1440);
				mCenter.cCountDown(mXpgWifiDevice, 1440);
			}else{
				setCountDown(HourTime * 60 + MinTime);
				mCenter.cCountDown(mXpgWifiDevice, HourTime * 60 + MinTime);
			}

			handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(),
					Lock_Time);
		}

	}

	/**
	 * the timing dialog listener
	 */
	private class TimerDialogListener implements On2TimingChosenListener {

		@Override
		public void timingChosen(int HourTime, int MinTime) {
			isLock = true;
			handler.removeMessages(handler_key.UNLOCK.ordinal());

			setTimer(true, HourTime * 60 + MinTime);
			mCenter.cTimer2(mXpgWifiDevice, HourTime * 60 + MinTime);

			handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(),
					Lock_Time);
		}

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

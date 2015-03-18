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

public class TimerSelectedActivity extends BaseActivity implements
		OnClickListener {

	private ToggleButton tbCountDown;

	private ToggleButton tbTiming;

	private TextView tvCountDown;

	private TextView tvTiming;

	private int CountDownHour = 0;

	private int CountDownMin = 0;

	private int TimerHour = 0;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_selected);
		initView();
		initEvent();
	}

	private void initView() {
		tbCountDown = (ToggleButton) findViewById(R.id.tbCountDownFlag);
		tbTiming = (ToggleButton) findViewById(R.id.tbTimingFlag);
		tvCountDown = (TextView) findViewById(R.id.tvCountDown);
		tvTiming = (TextView) findViewById(R.id.tvTiming);
	}

	private void initEvent() {
		tbCountDown.setOnClickListener(this);
		tbTiming.setOnClickListener(this);
	}

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

	@Override
	public void onBackPressed() {
		finish();
	}

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

	private void setTimer(boolean isTurn, int mhour) {
		tbTiming.setChecked(isTurn);

		int min = mhour % 60;
		int hour = mhour / 60;

		TimerHour = hour;
		TimerMin = min;

		tvTiming.setText(String.format("%02d:%02d", hour, min));
	}

	private class CountDownDialogListener implements On2TimingChosenListener {

		@Override
		public void timingChosen(int HourTime, int MinTime) {
			isLock = true;
			handler.removeMessages(handler_key.UNLOCK.ordinal());

			setCountDown(HourTime * 60 + MinTime);
			mCenter.cCountDown(mXpgWifiDevice, HourTime * 60 + MinTime);

			handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(),
					Lock_Time);
		}

	}

	private class TimerDialogListener implements On2TimingChosenListener {

		@Override
		public void timingChosen(int HourTime, int MinTime) {
			isLock = true;
			handler.removeMessages(handler_key.UNLOCK.ordinal());

			setTimer(tbTiming.isSelected(), HourTime * 60 + MinTime);
			mCenter.cTimer2(mXpgWifiDevice, HourTime * 60 + MinTime);

			handler.sendEmptyMessageDelayed(handler_key.UNLOCK.ordinal(),
					Lock_Time);
		}

	}

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

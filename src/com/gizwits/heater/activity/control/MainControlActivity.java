/**
 * Project Name:XPGSdkV4AppBase
 * File Name:MainControlActivity.java
 * Package Name:com.gizwits.aircondition.activity.control
 * Date:2015-1-27 14:44:17
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gizwits.heater.R;
import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.framework.activity.account.UserManageActivity;
import com.gizwits.framework.activity.device.DeviceListActivity;
import com.gizwits.framework.activity.device.DeviceManageListActivity;
import com.gizwits.framework.activity.help.AboutActivity;
import com.gizwits.framework.activity.help.HelpActivity;
import com.gizwits.framework.adapter.MenuDeviceAdapter;
import com.gizwits.framework.config.JsonKeys;
import com.gizwits.framework.entity.DeviceAlarm;
import com.gizwits.framework.utils.DensityUtil;
import com.gizwits.framework.utils.DialogManager;
import com.gizwits.framework.utils.StringUtils;
import com.gizwits.framework.widget.CircularSeekBar;
import com.gizwits.framework.widget.SlidingMenu;
import com.gizwits.framework.widget.SlidingMenu.SlidingMenuListener;
import com.xpg.common.system.IntentUtils;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

// TODO: Auto-generated Javadoc
/**
 * Created by Lien on 14/12/21.
 * 
 * 设备主控界面
 * 
 * @author Lien
 */
public class MainControlActivity extends BaseActivity implements
		OnClickListener, SlidingMenuListener {

	/** The tag. */
	private final String TAG = "MainControlActivity";

	/** The seek bar. */
	private CircularSeekBar seekBar;

	/** The scl content. */
	private RelativeLayout rlContent;

	/** The m view. */
	private SlidingMenu mView;

	/** The rl alarm tips. */
	private RelativeLayout rlAlarmTips;

	/** The rl power off. */
	private RelativeLayout rlPowerOff;

	/** The ll footer. */
	private LinearLayout llFooter;

	/** The iv menu. */
	private ImageView ivMenu;

	/** The tv title. */
	private TextView tvTitle;

	/** The iv power. */
	private ImageView ivPower;

	/** The tv HeaterTips. */
	private TextView tvHeaterTips;

	/** The tv alarm tips count. */
	private TextView tvAlarmTipsCount;

	/** The tv mode. */
	private TextView tvMode;

	/** The tv timer. */
	private TextView tvTimer;

	/** The tv TempCurrentTips. */
	private TextView tvTempCurrentTips;

	/** The tv current temperature. */
	private TextView tvCurrentTemperature;

	/** The tv setting temerature. */
	private TextView tvSettingTemerature;

	/** The m adapter. */
	private MenuDeviceAdapter mAdapter;

	/** The lv device. */
	private ListView lvDevice;

	/** The rl power on. */
	private RelativeLayout rlPowerOn;

	/** The is show. */
	private boolean isShow;

	/** The device data map. */
	private ConcurrentHashMap<String, Object> deviceDataMap;

	/** The statu map. */
	private ConcurrentHashMap<String, Object> statuMap;

	/** The alarm list. */
	private ArrayList<DeviceAlarm> alarmList;

	/** The alarm list has shown. */
	private ArrayList<String> alarmShowList;

	/** The m fault dialog. */
	private Dialog mFaultDialog;

	/** The m PowerOff dialog. */
	private Dialog mPowerOffDialog;

	/** The progress dialog. */
	private ProgressDialog progressDialogRefreshing;

	/** The disconnect dialog. */
	private Dialog mDisconnectDialog;

	/** 是否超时标志位 */
	private boolean isTimeOut = false;

	/** 侧拉菜单 */
	private ScrollView slMenu;

	/** 开关机标志位 */
	private boolean isPowerOff = false;

	/** 获取状态超时时间 */
	private int GetStatueTimeOut = 30000;

	/** 登陆设备超时时间 */
	private int LoginTimeOut = 5000;

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

		/** 显示警告 */
		ALARM,

		/** 设备断开连接 */
		DISCONNECTED,

		/** 接收到设备的数据 */
		RECEIVED,

		/** 获取设备状态 */
		GET_STATUE,

		/** 获取设备状态超时 */
		GET_STATUE_TIMEOUT,

		/** The login start. */
		LOGIN_START,

		/**
		 * The login success.
		 */
		LOGIN_SUCCESS,

		/**
		 * The login fail.
		 */
		LOGIN_FAIL,

		/**
		 * The login timeout.
		 */
		LOGIN_TIMEOUT,
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
						Log.i("info", (String) deviceDataMap.get("data"));
						inputDataToMaps(statuMap,
								(String) deviceDataMap.get("data"));

					}
					alarmList.clear();
					if (deviceDataMap.get("alters") != null) {
						Log.i("info", (String) deviceDataMap.get("alters"));
						// 返回主线程处理报警数据刷新
						inputAlarmToList((String) deviceDataMap.get("alters"));
					}
					if (deviceDataMap.get("faults") != null) {
						Log.i("info", (String) deviceDataMap.get("faults"));
						// 返回主线程处理错误数据刷新
						inputAlarmToList((String) deviceDataMap.get("faults"));
					}
					// 返回主线程处理P0数据刷新
					handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
					handler.sendEmptyMessage(handler_key.ALARM.ordinal());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			case UPDATE_UI:
				if (mView.isOpen())
					break;

				if (statuMap != null && statuMap.size() > 0) {
					handler.removeMessages(handler_key.GET_STATUE_TIMEOUT
							.ordinal());

					// 更新当前温度
					String curTemp = (String) statuMap.get(JsonKeys.ROOM_TEMP);
					if (!StringUtils.isEmpty(curTemp)) {
						updateCurrentTemp(Short.parseShort(curTemp));
					}
					// 更新设定温度
					String setTemp = (String) statuMap.get(JsonKeys.SET_TEMP);
					if (!StringUtils.isEmpty(setTemp)) {
						updateSettingTemp(Short.parseShort(setTemp));
					}
					// 更新模式状态
					String mode = (String) statuMap.get(JsonKeys.MODE);
					if (!StringUtils.isEmpty(mode)) {
						updateMode(Short.parseShort(mode));
					}
					// 更新定时模式
					String countDown = (String) statuMap
							.get(JsonKeys.COUNT_DOWN_RESERVE);
					if (!StringUtils.isEmpty(countDown)) {
						updateTiming(
								(Boolean) statuMap.get(JsonKeys.RESERVE_ON_OFF),
								Integer.parseInt(countDown));
					}
					// 更新电源开关
					updatePowerSwitch((Boolean) statuMap.get(JsonKeys.ON_OFF));

					DialogManager.dismissDialog(MainControlActivity.this,
							progressDialogRefreshing);
				}
				break;
			case ALARM:
				if (mView.isOpen())
					break;
				
				// 是否需要弹dialog判断
				boolean isNeedDialog = false;
				for (DeviceAlarm alarm : alarmList) {
					if (!alarmShowList.contains((String) alarm.getDesc())) {
						alarmShowList.add(alarm.getDesc());
						isNeedDialog = true;
					}
				}

				alarmShowList.clear();

				for (DeviceAlarm alarm : alarmList) {
					alarmShowList.add(alarm.getDesc());
				}

				if (alarmList != null && alarmList.size() > 0) {
					if (isNeedDialog) {
						DialogManager.showDialog(MainControlActivity.this,
								mFaultDialog);
					}
					setTipsLayoutVisiblity(true, alarmList.size());
				} else {
					setTipsLayoutVisiblity(false, 0);
				}
				break;
			case DISCONNECTED:
				if (!mView.isOpen()) {
					DialogManager.dismissDialog(MainControlActivity.this,
							progressDialogRefreshing);
					DialogManager.dismissDialog(MainControlActivity.this,
							mFaultDialog);
					DialogManager.dismissDialog(MainControlActivity.this,
							mPowerOffDialog);
					DialogManager.showDialog(MainControlActivity.this,
							mDisconnectDialog);
				}
				break;
			case GET_STATUE:
				mCenter.cGetStatus(mXpgWifiDevice);
				break;
			case GET_STATUE_TIMEOUT:
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			case LOGIN_SUCCESS:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				refreshMainControl();
				break;
			case LOGIN_FAIL:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			case LOGIN_TIMEOUT:
				isTimeOut = true;
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			}
		}
	};

	// 0.智能模式, 1.节能模式, 2.速热模式, 3.加热模式,4.保温模式,5.安全模式
	/** The mode images. */
	private int[] modeImages = { R.drawable.home_tab_intelligence_icon,
			R.drawable.home_tab_energy_icon, R.drawable.home_tab_fullpower,
			R.drawable.home_tab_heating_icon,
			R.drawable.home_tab_temperature_icon, R.drawable.home_tab_safe_icon };
	/** The mode str res. */
	private int[] modeStrs = { R.string.pattern_intelligence,
			R.string.pattern_energy, R.string.power_fullpower,
			R.string.pattern_heating, R.string.pattern_temperature,
			R.string.pattern_safe };

	/** 设定温度 */
	private int SettingTemp;

	/** 当前温度 */
	private int CurrentTemp;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#onCreate(android.os.Bundle
	 * )
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_control);
		initViews();
		initEvents();
		initParams();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#onResume()
	 */
	@Override
	public void onResume() {
		if (mView.isOpen()) {
			refreshMenu();
		} else {
			if (!mDisconnectDialog.isShowing())
				refreshMainControl();
		}
		super.onResume();

	}

	/**
	 * 更新菜单界面.
	 * 
	 * @return void
	 */
	private void refreshMenu() {
		initBindList();
		mAdapter.setChoosedPos(-1);
		for (int i = 0; i < bindlist.size(); i++) {
			if (bindlist.get(i).getDid()
					.equalsIgnoreCase(mXpgWifiDevice.getDid()))
				mAdapter.setChoosedPos(i);
		}
		
		//当前绑定列表没有当前操作设备
		if(mAdapter.getChoosedPos()==-1){
		mAdapter.setChoosedPos(0);
		mXpgWifiDevice=mAdapter.getItem(0);
		alarmList.clear();
		}
			
		mAdapter.notifyDataSetChanged();
		
		int px = DensityUtil.dip2px(this, mAdapter.getCount() * 50);
		lvDevice.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, px));
	}

	/**
	 * 更新主控制界面.
	 * 
	 * @return void
	 */
	private void refreshMainControl() {
		mXpgWifiDevice.setListener(deviceListener);
		DialogManager.showDialog(this, progressDialogRefreshing);
		handler.sendEmptyMessageDelayed(
				handler_key.GET_STATUE_TIMEOUT.ordinal(), GetStatueTimeOut);
		handler.sendEmptyMessage(handler_key.GET_STATUE.ordinal());
	}

	/**
	 * Inits the params.
	 */
	private void initParams() {
		statuMap = new ConcurrentHashMap<String, Object>();
		alarmList = new ArrayList<DeviceAlarm>();
		alarmShowList = new ArrayList<String>();

		refreshMenu();
		refreshMainControl();
	}

	/**
	 * Inits the views.
	 */
	private void initViews() {
		mView = (SlidingMenu) findViewById(R.id.main_layout);
		llFooter = (LinearLayout) findViewById(R.id.llFooter);
		rlAlarmTips = (RelativeLayout) findViewById(R.id.rlAlarmTips);
		rlPowerOff = (RelativeLayout) findViewById(R.id.rlPowerOff);
		ivMenu = (ImageView) findViewById(R.id.ivMenu);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvHeaterTips = (TextView) findViewById(R.id.tvHeaterTips);
		ivPower = (ImageView) findViewById(R.id.ivPower);
		tvAlarmTipsCount = (TextView) findViewById(R.id.tvAlarmTipsCount);
		tvMode = (TextView) findViewById(R.id.tvMode);
		tvTimer = (TextView) findViewById(R.id.tvTimer);
		tvCurrentTemperature = (TextView) findViewById(R.id.tvCurrentTemperature);
		tvSettingTemerature = (TextView) findViewById(R.id.tvSettingTemerature);
		tvTempCurrentTips = (TextView) findViewById(R.id.tvTempCurrentTips);
		rlPowerOn = (RelativeLayout) findViewById(R.id.rlPowerOn);
		rlContent = (RelativeLayout) findViewById(R.id.rlContent);
		seekBar = (CircularSeekBar) findViewById(R.id.csbSeekbar);
		seekBar.postInvalidateDelayed(2000);
		seekBar.setMaxProgress(100);
		seekBar.setProgress(30);
		seekBar.setMProgress(0);
		seekBar.postInvalidateDelayed(100);
		seekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				mCenter.cSetTemp(mXpgWifiDevice, SettingTemp);
				updateHeaterTips();
			}
		});
		seekBar.setSeekContinueChangeListener(new CircularSeekBar.OnSeekContinueChangeListener() {
			@Override
			public void onProgressContinueChange(CircularSeekBar view,
					int newProgress) {
				SettingTemp = (short) (newProgress * 45 / 100.00 + 30);
				tvSettingTemerature.setText(SettingTemp + "");
			}
		});
		mPowerOffDialog = DialogManager.getPowerOffDialog(this,
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						mCenter.cSwitchOn(mXpgWifiDevice, false);
						DialogManager.dismissDialog(MainControlActivity.this,
								mPowerOffDialog);
					}
				});
		mFaultDialog = DialogManager.getDeviceErrirDialog(
				MainControlActivity.this, "设备故障", new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_CALL, Uri
								.parse("tel:10086"));
						startActivity(intent);
						mFaultDialog.dismiss();
					}
				});

		mAdapter = new MenuDeviceAdapter(this, bindlist);
		lvDevice = (ListView) findViewById(R.id.lvDevice);
		lvDevice.setAdapter(mAdapter);
		slMenu = (ScrollView) findViewById(R.id.slMenu);

		progressDialogRefreshing = new ProgressDialog(MainControlActivity.this);
		progressDialogRefreshing.setMessage("正在更新状态,请稍后。");
		progressDialogRefreshing.setCancelable(false);

		mDisconnectDialog = DialogManager.getDisconnectDialog(this,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						DialogManager.dismissDialog(MainControlActivity.this,
								mDisconnectDialog);
						IntentUtils.getInstance().startActivity(
								MainControlActivity.this,
								DeviceListActivity.class);
						finish();
					}
				});
	}

	/**
	 * Inits the events.
	 */
	private void initEvents() {
		rlPowerOn.setOnClickListener(this);
		ivPower.setOnClickListener(this);
		rlContent.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isShow = false;
				return false;
			}
		});
		ivMenu.setOnClickListener(this);
		rlAlarmTips.setOnClickListener(this);
		tvTitle.setOnClickListener(this);

		lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!mAdapter.getItem(position).isOnline())
					return;
				
				
				if (mAdapter.getChoosedPos() != position) {
					alarmShowList.clear();
					mAdapter.setChoosedPos(position);
					mXpgWifiDevice = bindlist.get(position);
				}

				
				mView.toggle();
			}
		});
		mView.setSlidingMenuListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (mView.isOpen()) {
			return;
		}

		switch (v.getId()) {
		case R.id.ivMenu:
			slMenu.scrollTo(0, 0);
			mView.toggle();
			break;
		case R.id.ivPower:
			mPowerOffDialog.show();
			break;
		case R.id.rlTimer:
			startActivity(new Intent(MainControlActivity.this,
					TimerSelectedActivity.class));
			break;
		case R.id.rlMode:
			startActivity(new Intent(MainControlActivity.this,
					ModeSelectedActivity.class));
			break;
		case R.id.rlAlarmTips:
		case R.id.tvTitle:
			if (alarmList != null && alarmList.size() > 0) {
				Intent intent = new Intent(MainControlActivity.this,
						AlarmListActicity.class);
				intent.putExtra("alarm_list", alarmList);
				startActivity(intent);
			}
			break;
		case R.id.rlPowerButton:
			mCenter.cSwitchOn(mXpgWifiDevice, true);
			break;
		}
	}

	/**
	 * 菜单界面点击事件监听方法.
	 * 
	 * @return void
	 */
	public void onClickSlipBar(View view) {
		switch (view.getId()) {
		case R.id.rlDevice:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					DeviceManageListActivity.class);
			break;
		case R.id.rlAbout:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					AboutActivity.class);
			break;
		case R.id.rlAccount:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					UserManageActivity.class);
			break;
		case R.id.rlHelp:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					HelpActivity.class);
			break;
		case R.id.btnDeviceList:
			mCenter.cDisconnect(mXpgWifiDevice);
			DisconnectOtherDevice();
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					DeviceListActivity.class);
			finish();
			break;
		}
	}

	/**
	 * 菜单界面返回主控界面.
	 * 
	 * @return void
	 */
	private void backToMain() {
		mXpgWifiDevice=mAdapter.getItem(mAdapter.getChoosedPos());
		
		if (!mXpgWifiDevice.isConnected()) {
			loginDevice(mXpgWifiDevice);
			DialogManager.showDialog(this, progressDialogRefreshing);
		} 
		
		refreshMainControl();
	}

	/**
	 * Login device.
	 * 
	 * @param xpgWifiDevice
	 *            the xpg wifi device
	 */
	private void loginDevice(XPGWifiDevice xpgWifiDevice) {
		mXpgWifiDevice = xpgWifiDevice;
		
		mXpgWifiDevice.setListener(deviceListener);
		mXpgWifiDevice.login(setmanager.getUid(), setmanager.getToken());
		isTimeOut = false;
		handler.sendEmptyMessageDelayed(handler_key.LOGIN_TIMEOUT.ordinal(),
				LoginTimeOut);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.framework.activity.BaseActivity#didLogin(com.xtremeprog.
	 * xpgconnect.XPGWifiDevice, int)
	 */
	@Override
	protected void didLogin(XPGWifiDevice device, int result) {
		if (isTimeOut)
			return;

		if (result == 0) {
			handler.sendEmptyMessage(handler_key.LOGIN_SUCCESS.ordinal());
		} else {
			handler.sendEmptyMessage(handler_key.LOGIN_FAIL.ordinal());
		}

	}

	/**
	 * 检查出了选中device，其他device有没有连接上
	 * 
	 * @param mac
	 *            the mac
	 * @param did
	 *            the did
	 * @return the XPG wifi device
	 */
	private void DisconnectOtherDevice() {
		for (XPGWifiDevice theDevice : bindlist) {
			if (theDevice.isConnected()
					&& !theDevice.getDid().equalsIgnoreCase(
							mXpgWifiDevice.getDid()))
				mCenter.cDisconnect(theDevice);
		}
	}

	/**
	 * 更新开关状态.
	 * 
	 * @param isSwitch
	 *            the isSwitch
	 */
	private void updatePowerSwitch(boolean isSwitch) {
		if (!isSwitch) {
			seekBar.setVisibility(View.INVISIBLE);
			rlPowerOff.setVisibility(View.VISIBLE);
			ivPower.setVisibility(View.GONE);
			tvHeaterTips.setVisibility(View.INVISIBLE);

			for (int i = 0; i < llFooter.getChildCount(); i++) {
				View view = llFooter.getChildAt(i);
				view.setClickable(false);
			}
		} else {
			seekBar.setVisibility(View.VISIBLE);
			rlPowerOff.setVisibility(View.GONE);
			ivPower.setVisibility(View.VISIBLE);
			tvHeaterTips.setVisibility(View.VISIBLE);

			for (int i = 0; i < llFooter.getChildCount(); i++) {
				View view = llFooter.getChildAt(i);
				view.setClickable(true);
			}

			if (!isPowerOff && alarmList.size() != 0) {
				DialogManager.showDialog(this, mFaultDialog);
			}
		}
		isPowerOff = isSwitch;
	}

	/**
	 * 更新定时预约状态.
	 * 
	 * @param timer
	 *            the timer
	 * @param countDown
	 *            the countDown
	 */
	private void updateTiming(boolean timer, int countDown) {
		if (timer || countDown > 0) {
			tvTimer.setText(R.string.appointment_already);
		} else {
			tvTimer.setText(R.string.appointment_water);
		}
	}

	/**
	 * 更新当前温度状态.
	 * 
	 * @param temp
	 *            the temp
	 */
	private void updateCurrentTemp(short temp) {
		CurrentTemp = temp;
		tvCurrentTemperature.setText("" + temp);
		tvTempCurrentTips.setText(Html.fromHtml("当前温度" + "<big><big>" + temp
				+ "°" + "</big></big>"));

		updateHeaterTips();
	}

	/**
	 * 更新设定温度状态.
	 * 
	 * @param temp
	 *            the temp
	 */
	private void updateSettingTemp(short temp) {
		SettingTemp = temp;
		tvSettingTemerature.setText("" + temp);

		int progress = (int) ((temp - 30) * 100.00 / 45);
		if (seekBar != null) {
			seekBar.setMProgress(progress);
			seekBar.postInvalidateDelayed(1000);
		}

		updateHeaterTips();
	}

	/**
	 * 更新加热提示状态.
	 * 
	 */
	private void updateHeaterTips() {
		if (SettingTemp - CurrentTemp > 5) {
			tvHeaterTips.setText(R.string.heater_is_heating);
		} else if (SettingTemp - CurrentTemp <= 5
				&& SettingTemp - CurrentTemp >= 0) {
			tvHeaterTips.setText(R.string.heater_is_keeping_warm);
		} else {
			tvHeaterTips.setText("");
		}
	}

	/**
	 * 转换模式更新UI.
	 * 
	 * @param num
	 *            the num
	 * @when num = 0 智能模式
	 * @when num = 1 节能模式
	 * @when num = 2 速热模式
	 * @when num = 3 加热模式
	 * @when num = 4 保温模式
	 * @when num = 3 加热模式
	 * 
	 */
	private void updateMode(int num) {
		tvMode.setCompoundDrawablesWithIntrinsicBounds(0, modeImages[num], 0, 0);
		tvMode.setText(modeStrs[num]);
	}

	/**
	 * 设置提示框显示与隐藏,设置故障数量.
	 * 
	 * @param isShow
	 *            the is show
	 * @param count
	 *            the count
	 * @true 显示
	 * @false 隐藏
	 */
	private void setTipsLayoutVisiblity(boolean isShow, int count) {
		rlAlarmTips.setVisibility(isShow ? View.VISIBLE : View.GONE);
		tvAlarmTipsCount.setText(count + "");
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
		if(!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;
		
		this.deviceDataMap = dataMap;
		handler.sendEmptyMessage(handler_key.RECEIVED.ordinal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (mView.isOpen()) {
			mView.toggle();
		} else {
			if (mXpgWifiDevice != null && mXpgWifiDevice.isConnected()) {
				mCenter.cDisconnect(mXpgWifiDevice);
				mXpgWifiDevice = null;
			}
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#didDisconnected(com.xtremeprog
	 * .xpgconnect.XPGWifiDevice)
	 */
	@Override
	protected void didDisconnected(XPGWifiDevice device) {
		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;
			
		handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
	}
	
	/**
	 * 把警告信息存入列表
	 * 
	 * @param json
	 *            the json
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void inputAlarmToList(String json) throws JSONException {
		Log.i("revjson", json);
		JSONObject receive = new JSONObject(json);
		Iterator actions = receive.keys();
		while (actions.hasNext()) {

			String action = actions.next().toString();
			Log.i("revjson", "action=" + action);
			DeviceAlarm alarm = new DeviceAlarm(getDateCN(new Date()), action);
			alarmList.add(alarm);
		}
		handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
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
				Log.i(TAG, "Key:" + param + ";value" + value);
			}
		}
		handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
	}

	/**
	 * 获取格式：2014年6月24日 17:23.
	 * 
	 * @param date
	 *            the date
	 * @return the date cn
	 */
	public static String getDateCN(Date date) {
		int y = date.getYear();
		int m = date.getMonth() + 1;
		int d = date.getDate();

		int h = date.getHours();
		int mt = date.getMinutes();

		return (y + 1900) + "年" + m + "月" + d + "日  " + h + ":" + mt;

	}

	@Override
	public void OpenFinish() {
	}

	@Override
	public void CloseFinish() {
		backToMain();
	}

}

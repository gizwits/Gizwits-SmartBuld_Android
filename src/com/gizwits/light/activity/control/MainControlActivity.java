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
package com.gizwits.light.activity.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

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
import com.gizwits.framework.widget.AboutVersionActivity;
import com.gizwits.framework.widget.CircularSeekBar;
import com.gizwits.framework.widget.SlidingMenu;
import com.gizwits.framework.widget.SlidingMenu.SlidingMenuListener;
import com.gizwits.light.R;
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
public class MainControlActivity extends BaseActivity implements OnClickListener, SlidingMenuListener {

	/** The tag. */
	private final String TAG = "MainControlActivity";

	/** The seek bar. */
	private CircularSeekBar seekBar;

	/** The m view. */
	private SlidingMenu mView;

	/** The ll footer. */
	private LinearLayout llFooter;

	/** The iv power off. */
	private ImageView ivPowerOff;

	/** The iv menu. */
	private ImageView ivMenu;

	private ImageView ivColorful;

	private ImageView ivColorTemp;

	private ImageView ivPower;

	/** The ColorTemp Seekbar. */
	private SeekBar sbColorTemp;

	/** The Brighteness Seekbar. */
	private SeekBar sbBrighteness;

	/** The m adapter. */
	private MenuDeviceAdapter mAdapter;

	/** The lv device. */
	private ListView lvDevice;

	/** The tv colorFul. */
	private TextView tvColorful;

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
				if (deviceDataMap == null) {
					return;
				}
				try {
					if (deviceDataMap.get("data") != null) {
						Log.i("info", (String) deviceDataMap.get("data"));
						inputDataToMaps(statuMap, (String) deviceDataMap.get("data"));

					}
					alarmList.clear();
					if (deviceDataMap.get("alters") != null) {
						Log.i("info", (String) deviceDataMap.get("alters"));
					}
					if (deviceDataMap.get("faults") != null) {
						Log.i("info", (String) deviceDataMap.get("faults"));
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
					handler.removeMessages(handler_key.GET_STATUE_TIMEOUT.ordinal());
					// 更新当前颜色
					String R = (String) statuMap.get(JsonKeys.COLOR_RED);
					String G = (String) statuMap.get(JsonKeys.COLOR_GREEN);
					String B = (String) statuMap.get(JsonKeys.COLOR_BLUE);
					if (!StringUtils.isEmpty(R) && !StringUtils.isEmpty(G) && !StringUtils.isEmpty(B)) {
						int r = Integer.parseInt(R);
						int g = Integer.parseInt(G);
						int b = Integer.parseInt(B);

						updateColor(Color.argb(255, r, g, b));
					}

					// 更新当前色温
					String colorTemp = (String) statuMap.get(JsonKeys.COLOR_TEMPERATURE);
					if (!StringUtils.isEmpty(colorTemp)) {
						updateColorTemp(Integer.parseInt(colorTemp));
					}

					// 更新当前亮度
					String brightness = (String) statuMap.get(JsonKeys.BRIGHTNESS);
					if (!StringUtils.isEmpty(brightness)) {
						updateBrighteness(Integer.parseInt(brightness));
					}

					// 更新当前模式
					String mode = (String) statuMap.get(JsonKeys.MODE);
					if (!StringUtils.isEmpty(mode)) {
						updateMode(Integer.parseInt(mode));
					}

					// 更新开关机
					updatePowerSwitch((Boolean) statuMap.get(JsonKeys.ON_OFF));

					DialogManager.dismissDialog(MainControlActivity.this, progressDialogRefreshing);
				}
				break;
			case ALARM:
				break;
			case DISCONNECTED:
				if (!mView.isOpen()) {
					DialogManager.dismissDialog(MainControlActivity.this, progressDialogRefreshing);
					DialogManager.dismissDialog(MainControlActivity.this, mFaultDialog);
					DialogManager.dismissDialog(MainControlActivity.this, mPowerOffDialog);
					DialogManager.showDialog(MainControlActivity.this, mDisconnectDialog);
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
			if (bindlist.get(i).getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
				mAdapter.setChoosedPos(i);
		}

		// 当前绑定列表没有当前操作设备
		if (mAdapter.getChoosedPos() == -1) {
			mAdapter.setChoosedPos(0);
			mXpgWifiDevice = mAdapter.getItem(0);
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
		handler.sendEmptyMessageDelayed(handler_key.GET_STATUE_TIMEOUT.ordinal(), GetStatueTimeOut);
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
		ivMenu = (ImageView) findViewById(R.id.ivMenu);

		llFooter = (LinearLayout) findViewById(R.id.llFoot);
		ivPowerOff = (ImageView) findViewById(R.id.ivPowerOff);
		tvColorful = (TextView) findViewById(R.id.tvColorful);
		ivColorful = (ImageView) findViewById(R.id.ivColorful);
		ivColorTemp = (ImageView) findViewById(R.id.ivColorTemp);
		sbBrighteness = (SeekBar) findViewById(R.id.sbBrighteness);
		sbColorTemp = (SeekBar) findViewById(R.id.sbColorTemp);
		ivPower = (ImageView) findViewById(R.id.ivPower);

		seekBar = (CircularSeekBar) findViewById(R.id.csbSeekbar);
		seekBar.postInvalidateDelayed(2000);
		seekBar.setMaxProgress(100);
		seekBar.setProgress(30);
		seekBar.setMProgress(0);
		seekBar.postInvalidateDelayed(100);
		seekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
			@Override
			public void onProgressChange(CircularSeekBar view, int color) {
				updateMode(0);// 色彩模式
				mCenter.cColor(mXpgWifiDevice, color);
			}
		});
		mPowerOffDialog = DialogManager.getPowerOffDialog(this, new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mCenter.cSwitchOn(mXpgWifiDevice, false);
				DialogManager.dismissDialog(MainControlActivity.this, mPowerOffDialog);
			}
		});
		mFaultDialog = DialogManager.getDeviceErrirDialog(MainControlActivity.this, "设备故障", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:10086"));
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

		mDisconnectDialog = DialogManager.getDisconnectDialog(this, new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogManager.dismissDialog(MainControlActivity.this, mDisconnectDialog);
				IntentUtils.getInstance().startActivity(MainControlActivity.this, DeviceListActivity.class);
				finish();
			}
		});

		sbBrighteness.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sbBrighteness.getParent().requestDisallowInterceptTouchEvent(true);
					break;
				case MotionEvent.ACTION_CANCEL:
					sbBrighteness.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				return false;
			}
		});

		sbBrighteness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mCenter.cBrightness(mXpgWifiDevice, seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
		});

		sbColorTemp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				if (progress == 0) {
					mCenter.cColorTemp(mXpgWifiDevice, 0);
				} else if (progress == 50) {
					mCenter.cColorTemp(mXpgWifiDevice, 1);
				} else if (progress == 100) {
					mCenter.cColorTemp(mXpgWifiDevice, 2);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (progress <= 25) {
					seekBar.setProgress(0);
				} else if (progress >= 75) {
					seekBar.setProgress(100);
				} else {
					seekBar.setProgress(50);
				}
			}
		});

		sbColorTemp.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sbColorTemp.getParent().requestDisallowInterceptTouchEvent(true);
					break;
				case MotionEvent.ACTION_CANCEL:
					sbColorTemp.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				return false;
			}
		});
	}

	/**
	 * Inits the events.
	 */
	private void initEvents() {
		ivMenu.setOnClickListener(this);
		lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
			if (isPowerOff) {
				mPowerOffDialog.show();
			} else {
				mCenter.cSwitchOn(mXpgWifiDevice, true);
			}
			break;
		}
	}

	/**
	 * 菜单界面点击事件监听方法.
	 * 
	 * @return void
	 */
	public void onClickSlipBar(View view) {
		if (!mView.isOpen())
			return;

		switch (view.getId()) {
		case R.id.rlDevice:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, DeviceManageListActivity.class);
			break;
		case R.id.rlAbout:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, AboutActivity.class);
			break;
		case R.id.rlAbout_Demo:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, AboutVersionActivity.class);
			break;
		case R.id.rlAccount:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, UserManageActivity.class);
			break;
		case R.id.rlHelp:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, HelpActivity.class);
			break;
		case R.id.btnDeviceList:
			mCenter.cDisconnect(mXpgWifiDevice);
			DisconnectOtherDevice();
			IntentUtils.getInstance().startActivity(MainControlActivity.this, DeviceListActivity.class);
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
		mXpgWifiDevice = mAdapter.getItem(mAdapter.getChoosedPos());

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
		handler.sendEmptyMessageDelayed(handler_key.LOGIN_TIMEOUT.ordinal(), LoginTimeOut);
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
			if (theDevice.isConnected() && !theDevice.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
				mCenter.cDisconnect(theDevice);
		}
	}

	// ==================================================================================
	/**
	 * 更新开关状态.
	 * 
	 * @param isSwitch
	 *            the isSwitch
	 */
	private void updatePowerSwitch(boolean isSwitch) {
		if (isSwitch) {// 开机
			seekBar.ShowSeekBar();
			ivPower.setSelected(true);
			tvColorful.setTextColor(getResources().getColor(R.color.text_blue));
			powerOn();
		} else {// 关机
			seekBar.hideSeekBar();
			ivColorful.setSelected(false);
			ivColorTemp.setSelected(false);
			ivPower.setSelected(false);
			tvColorful.setTextColor(getResources().getColor(R.color.text_gray));
			powerOff();
		}
		isPowerOff = isSwitch;
	}

	// 开机
	private void powerOn() {
		ivPowerOff.setVisibility(View.INVISIBLE);
		llFooter.setVisibility(View.VISIBLE);
	}

	// 关机
	private void powerOff() {
		Bitmap mBitmap = Bitmap.createBitmap(llFooter.getWidth(), llFooter.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		llFooter.draw(canvas);
		mBitmap = getTransparentBitmap(mBitmap, 50);
		ivPowerOff.setVisibility(View.VISIBLE);
		ivPowerOff.setImageBitmap(mBitmap);
		llFooter.setVisibility(View.INVISIBLE);
	}

	private Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

				.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

		number = number * 255 / 100;

		for (int i = 0; i < argb.length; i++) {

			if (argb[i] != 0) {// 把透明的颜色隔离掉
				int color = argb[i];
				int a = Color.alpha(color);
				int r = Color.red(color);
				int g = Color.green(color);
				int b = Color.blue(color);
				int avg = (r + g + b) / 3;
				argb[i] = Color.argb(a, avg, avg, avg);
			}
		}

		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);

		return sourceImg;
	}

	/**
	 * 更新色彩.
	 * 
	 * @param isSwitch
	 *            the isSwitch
	 */
	private void updateColor(int color) {
		seekBar.setInnerColor(color);
	}

	/**
	 * 更新模式状态.
	 * 
	 * @param mode
	 *            the mode
	 */
	private void updateMode(int mode) {
		switch (mode) {
		case 0:// 色彩模式
			ivColorful.setSelected(true);
			ivColorTemp.setSelected(false);
			break;
		case 1:// 色温模式
			ivColorful.setSelected(false);
			ivColorTemp.setSelected(true);
			break;
		}

	}

	/**
	 * 更新色温状态.
	 * 
	 * @param num
	 *            the num
	 */
	private void updateColorTemp(int num) {
		switch (num) {
		case 0:// 暖白
			sbColorTemp.setProgress(0);
			break;
		case 1:// 中性白
			sbColorTemp.setProgress(50);
			break;
		case 2:// 冷白
			sbColorTemp.setProgress(100);
			break;
		}
	}

	/**
	 * 更新亮度状态.
	 * 
	 * @param num
	 *            the num
	 */
	private void updateBrighteness(int num) {
		sbBrighteness.setProgress(num);
	}

	// ==================================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#didReceiveData(com.
	 * xtremeprog .xpgconnect.XPGWifiDevice,
	 * java.util.concurrent.ConcurrentHashMap, int)
	 */
	@Override
	protected void didReceiveData(XPGWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int result) {
		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
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
				DisconnectOtherDevice();
			}
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#didDisconnected(com.
	 * xtremeprog .xpgconnect.XPGWifiDevice)
	 */
	@Override
	protected void didDisconnected(XPGWifiDevice device) {
		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;

		handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
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
	private void inputDataToMaps(ConcurrentHashMap<String, Object> map, String json) throws JSONException {
		Log.i("revjson", json);
		JSONObject receive = new JSONObject(json);
		Iterator actions = receive.keys();
		while (actions.hasNext()) {

			String action = actions.next().toString();
			Log.i("revjson", "action=" + action);
			// 忽略特殊部分
			if (action.equals("cmd") || action.equals("qos") || action.equals("seq") || action.equals("version")) {
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

	@Override
	public void OpenFinish() {
	}

	@Override
	public void CloseFinish() {
		backToMain();
	}

}

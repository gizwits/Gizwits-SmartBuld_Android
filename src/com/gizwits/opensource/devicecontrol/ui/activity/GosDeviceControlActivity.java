package com.gizwits.opensource.devicecontrol.ui.activity;

import java.util.concurrent.ConcurrentHashMap;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.ui.view.ColorCircularSeekBar;
import com.gizwits.opensource.devicecontrol.ui.view.ColorTempCircularSeekBar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GosDeviceControlActivity extends GosDeviceControlModuleBaseActivity implements OnClickListener{

	/** 返回按钮 */
	private ImageView ivBack;
	
	/** 标题TextView */
	private TextView tvTitle;
	
	/** 设置按钮 */
	private ImageView ivSetting;
	
	/** 延时功能布局 */
	private RelativeLayout rlDelay;
	
	/** 延时TextView */
	private TextView tvDelay;
	
	/** 开关按钮 */
	private Button btnPower;
	
	/** 开关TextView */
	private TextView tvPower;
	
	/** 整个布局覆盖ImageView */
	private ImageView ivmain;
	
	/** 整个布局 */
	private RelativeLayout rl_middle;
	
	/** 关灯布局ImageView */
	private ImageView ivPowerOff;
	
	/** 中部UI布局 */
	private RelativeLayout rl_top;
	
	/** 模式切换按钮 */
	private Button btnmode;
		
	/** 色彩进度条*/
	private ColorCircularSeekBar circularSeekBar;
	
	/** 色温进度条*/
	private ColorTempCircularSeekBar colorTempCircularSeekBar;
	
	/** 亮度进度条 */
	private SeekBar sbBrighteness;
	
	private Runnable mRunnable = new Runnable() {
		public void run() {
			if (isDeviceCanBeControlled()) {
				progressDialog.cancel();
			} else {
				handler.sendEmptyMessage(handler_key.DEV_NOREADY.ordinal());
			}
		}

	};
	private enum handler_key {
		/** 获取设备状态*/
		GET_DEV_STUAT,
		
		/** 接收到设备的数据 */
		RECEIVED,

		/** 设备未就绪 */
		DEV_NOREADY,
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case GET_DEV_STUAT:
				device.getDeviceStatus();
				break;
			case DEV_NOREADY:
				toastDeviceNoReadyAndExit();
				break;
			case RECEIVED:
				progressDialog.cancel();
				getDataFromDateMap();
				upDateUI();
				break;
			default:
				break;
			}
		}

	};
	
	protected void upDateUI() {
		updateTitle();
		updatePowerUI(isPowerOn);
		updateMode(mode_num);
		updateColor(Color.argb(255,color_num_r, color_num_g, color_num_b));
		updateColortemp(Color.argb(255,color_num_temp_r, color_num_temp_g, color_num_temp_b));
		updateBrighteness(bringhtness_num);
		updateDelayUI(isOpenDelaying);
	}
	
	private void updateTitle() {
		tvTitle.setText(getDeviceName());		
	}
	
	/**
	 * 更新电源开关切换.
	 */
	private void updatePowerUI(boolean isPower) {
		if (!isPower) {
			btnPower.setSelected(true);
			tvPower.setText(getString(R.string.openlight));
			ivmain.setVisibility(View.VISIBLE);
			rl_top.setVisibility(View.VISIBLE);	
		} else {
			btnPower.setSelected(false);
			tvPower.setText(getString(R.string.closelight));			
			ivPowerOff.setVisibility(View.INVISIBLE);
			rl_middle.setVisibility(View.VISIBLE);
			ivmain.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 更新模式切换.
	 */
	private void updateMode(Integer mode_num) {
		device.getAlias();
		switch (mode_num) {
		case 0:
			btnmode.setSelected(true);//home_btn_color		
			circularSeekBar.ShowSeekBar();
			circularSeekBar.setVisibility(View.VISIBLE);
			colorTempCircularSeekBar.setVisibility(View.GONE);
			break;
		case 1:
			btnmode.setSelected(false);//home_btn_color_temperature
			circularSeekBar.hideSeekBar();
			circularSeekBar.setVisibility(View.GONE);
			colorTempCircularSeekBar.setVisibility(View.VISIBLE);
			break;
		}	
	}
	
	/*
	 * 更新色彩
	 */
	private void updateColor(int color) {
		circularSeekBar.setInnerColor(Color.argb(255,color_num_r, color_num_g, color_num_b));	

	}
	
	/*
	 * 更新色温
	 */
	private void updateColortemp(int color) {
		colorTempCircularSeekBar.setInnerColor(Color.argb(255,color_num_temp_r, color_num_temp_g, color_num_temp_b));

	}
	/*
	 * 更新亮度值
	 */
	private void updateBrighteness(int bringhtness_num) {
		
		sbBrighteness.setProgress(bringhtness_num);
	}

	/*
	 * 更新延时
	 */
	private void updateDelayUI(Boolean isShow) {
		if (isShow) {			
			tvDelay.setText(countDownMinute + getResources().getString(R.string.apm_min_later));
			rlDelay.setVisibility(View.VISIBLE);
		} else {
			rlDelay.setVisibility(View.GONE);
		}		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_light_device_control);		
		initView();
		initEvent();
		initDevice();		
		upDateUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 每次界面可视时候将获取重新获取设备装备
		getStatusOfSocket();
		upDateUI();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		device.setSubscribe(false);
	}

	private void toastDeviceNoReadyAndExit() {
		myToast(R.string.device_no_ready);
		finish();
	}
	
	private boolean isDeviceCanBeControlled() {
		return device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
	}
	
	/**
	 * Description:判断当前设备是否可控.
	 */
	private void getStatusOfSocket() {

		// 设备是否可控
		if (isDeviceCanBeControlled()) {
			// 可控则查询当前设备状态
			device.getDeviceStatus();
		} else {
			// 显示等待栏
			setProgressDialog(getResources().getString(R.string.wait_for_connet), true, false);
			progressDialog.show();
			if (device.isLAN()) {
				// 小循环10s未连接上设备自动退出
				handler.postDelayed(mRunnable, 10000);
			} else {
				// 大循环20s未连接上设备自动退出
				handler.postDelayed(mRunnable, 20000);
			}
		}
	}
	
	private void initView() {
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivSetting = (ImageView) findViewById(R.id.ivSetting);
		btnPower = (Button) findViewById(R.id.btnPower);
		tvPower = (TextView) findViewById(R.id.tvPower);
		rl_top = (RelativeLayout) findViewById(R.id.rl_top);
		ivmain = (ImageView) findViewById(R.id.ivmain);
		rl_middle = (RelativeLayout) findViewById(R.id.rl_middle);
		ivPowerOff = (ImageView) findViewById(R.id.ivPowerOff);
		//延时预约
		rlDelay = (RelativeLayout) findViewById(R.id.rlDelay);
		tvDelay = (TextView) findViewById(R.id.tvDelay);	
		//模式切换
		btnmode = (Button) findViewById(R.id.btnmode);
		//色彩的进度条
		circularSeekBar = (ColorCircularSeekBar) findViewById(R.id.csbSeekbar2);
		circularSeekBar.postInvalidateDelayed(2000);
		circularSeekBar.setMaxProgress(100);
		circularSeekBar.setProgress(30);
		circularSeekBar.setMProgress(0);
		circularSeekBar.postInvalidateDelayed(100);
		circularSeekBar.setSeekBarChangeListener(new ColorCircularSeekBar.OnSeekChangeListener() {
			@Override
			public void onProgressChange(ColorCircularSeekBar view, int color) {
				// TODO Auto-generated method stub
					//color_num=color;
					cColor(device, color);							
				}
							
		});						
		//色温的进度条
		colorTempCircularSeekBar = (ColorTempCircularSeekBar) findViewById(R.id.csbSeekbar);//色温
		colorTempCircularSeekBar.postInvalidateDelayed(2000);
		colorTempCircularSeekBar.setMaxProgress(100);
		colorTempCircularSeekBar.setProgress(30);
		colorTempCircularSeekBar.setMProgress(0);
		colorTempCircularSeekBar.postInvalidateDelayed(100);
		colorTempCircularSeekBar.setSeekBarChangeListener(new ColorTempCircularSeekBar.OnSeekChangeListener() {
		
			@Override
			public void onProgressChange(ColorTempCircularSeekBar view, int color) {
				// TODO Auto-generated method stub
					cColorTemp(device, color);			
				}
									
		});
		//亮度
		sbBrighteness = (SeekBar) findViewById(R.id.sbBrighteness);
		sbBrighteness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				bringhtness_num = seekBar.getProgress();
				cBrightness( device, seekBar.getProgress());				
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
			}
		});
	}
	
	private void initEvent() {
		ivBack.setOnClickListener(this);
		ivSetting.setOnClickListener(this);
		btnPower.setOnClickListener(this);
		btnmode.setOnClickListener(this);
	}
	
	private void initDevice() {
		Intent intent = getIntent();
		GizWifiDevice dev = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
		if (dev != null) {
			device = dev;
			device.setListener(gizWifiDeviceListener);
		} else {
			toastDeviceNoReadyAndExit();
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.iv_back:
			finish();	
			break;
		case R.id.ivSetting:
			startDeviceSettingActivity();
			break;
		case R.id.btnPower:
			btnPowerAction();
			break;
		case R.id.btnmode:
			btnModeAction();
			break;
		default:
			break;
		}
	}
					
	private void startDeviceSettingActivity() {
		startActivity(new Intent(this, GosDeviceMoreActivity.class));
	}
	
	/* 开关*/
	private void btnPowerAction() {
		sendCommand("Power_Switch", !isPowerOn);		
		isPowerOn =!isPowerOn;		
		if (!isPowerOn == true) {
			btnPowerOff();
		} else {
			ivPowerOff.setVisibility(View.INVISIBLE);
			rl_middle.setVisibility(View.VISIBLE);
			ivmain.setVisibility(View.INVISIBLE);
		}
	}
	
	/* 模式*/
	private void btnModeAction() {
		device.getAlias();
		if (mode_num == 0) {
			btnmode.setSelected(false);//home_btn_color_temperature
			circularSeekBar.setVisibility(View.GONE);
			colorTempCircularSeekBar.setVisibility(View.VISIBLE);
			mode_num = 1;
		} else {			
			btnmode.setSelected(true);//home_btn_color
			circularSeekBar.setVisibility(View.VISIBLE);
			colorTempCircularSeekBar.setVisibility(View.GONE);
			mode_num = 0;
		}
		sendCommand("mode", mode_num);
	}
	
	/* 色彩*/
	public void cColor(GizWifiDevice device, int color){
		sendMoreCommand("Color_R", Color.red(color), 
				"Color_G", Color.green(color), "Color_B", Color.blue(color));
		color_num_r = Color.red(color);
		color_num_g = Color.green(color);
		color_num_b = Color.blue(color);
	}
				
	/* 色温*/
	public void cColorTemp(GizWifiDevice device, int color_num){
		sendMoreCommand("Temperature_R", Color.red(color_num), 
				"Temperature_G", Color.green(color_num), "Temperature_B", Color.blue(color_num));
		color_num_temp_r = Color.red(color_num);
		color_num_temp_g = Color.green(color_num);
		color_num_temp_b = Color.blue(color_num);
	}
		
	/* 亮度*/
	public void cBrightness( GizWifiDevice device, int bringhtness_num) {
		sendCommand("Brightness", bringhtness_num);	
	}	
	
	public void sendMoreCommand(String key, Object value,String key1, Object value1,String key2, Object value2) {
//		int sn = 5; // 如果App不使用sn，此处可写成 int sn = 0;
		int sn = 0;
		ConcurrentHashMap<String, Object> command = new ConcurrentHashMap<String, Object>();
		command.put(key, value);
		command.put(key1, value1);
		command.put(key2, value2);		
		device.write(command, sn);
	}
	
	private void btnPowerOff() {
		ivmain.setVisibility(View.VISIBLE);
		rl_top.setVisibility(View.VISIBLE);
		powerOff();
	}
	
	private void powerOff() {		
		Bitmap mBitmap = Bitmap.createBitmap(rl_middle.getWidth(), rl_middle.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		rl_middle.draw(canvas);
		ivPowerOff.setVisibility(View.VISIBLE);
		ivPowerOff.setImageBitmap(mBitmap);
		rl_middle.setVisibility(View.INVISIBLE);
		
	}
		
	@Override
	protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int sn) {
		super.didReceiveData(result, device, dataMap, sn);
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
			deviceDataMap = dataMap;
			handler.sendEmptyMessage(handler_key.RECEIVED.ordinal());
		}
	}
	
	@Override
	protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
		super.didUpdateNetStatus(device, netStatus);
		if (device.isSubscribed()) {
			if (netStatus == GizWifiDeviceNetStatus.GizDeviceControlled) {
				handler.removeCallbacks(mRunnable);
				progressDialog.cancel();
				// 后面操作是等待sdk下发查询设备状态命令，收到设备状态后再更新界面
			} else {
				myToast(R.string.device_dropped);
				finish();
			}
		}
	}

}

package com.gizwits.opensource.devicecontrol.ui.activity;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.smartlight.R;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

public class GosEditDeviceDelayActivity extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private NumberPicker mPick;

	private TextView tvSave;

	private TextView tvBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_schedule_edit_device_delay);
		initView();
		initEvent();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_save:
			saveAction();
			break;

		case R.id.tv_back:
			finish();
			break;
		default:
			break;
		}
	}

	private void initEvent() {
		tvSave.setOnClickListener(this);
		tvSave.setClickable(false);
		tvBack.setOnClickListener(this);
//		mPick.setMaxValue(59);
		mPick.setMaxValue(60);
		mPick.setMinValue(1);
		mPick.setOnLongPressUpdateInterval(100);
		mPick.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		setNumberPickerDividerColor(mPick);

		if (countDownMinute == 0) {
			mPick.setValue(1);
			tvSave.setClickable(true);
			tvSave.setTextColor(getResources().getColor(R.color.text_blue));
		} else {
			mPick.setValue(countDownMinute);
		}

		mPick.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				tvSave.setClickable(true);
				tvSave.setTextColor(getResources().getColor(R.color.text_blue));
			}
		});
	}

	private void initView() {
		tvSave = (TextView) findViewById(R.id.tv_save);
		tvBack = (TextView) findViewById(R.id.tv_back);
		mPick = (NumberPicker) findViewById(R.id.minute_picker);
	}

	private void setNumberPickerDividerColor(NumberPicker numberPicker) {

		Field[] pickerFields = NumberPicker.class.getDeclaredFields();
		for (Field pf : pickerFields) {

			if (pf.getName().equals("mSelectionDivider")) {
				pf.setAccessible(true);
				try {
					// 设置分割线的颜色值
					pf.set(numberPicker, new ColorDrawable(this.getResources().getColor(R.color.transparent)));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (Resources.NotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	private void saveAction() {
		int sn = 5;
		ConcurrentHashMap<String, Object> command = new ConcurrentHashMap<String, Object>();
		command.put(KEY5_COUNTDOWN_MINUTE, mPick.getValue());
		command.put(KEY8_COUNTDOWN_ONOFF, true);
		device.write(command, sn);
		countDownMinute = mPick.getValue();
		isOpenDelaying = true;
		finish();
	}

	@Override
	protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int sn) {
		super.didReceiveData(result, device, dataMap, sn);
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
			deviceDataMap = dataMap;
			getDataFromDateMap();
		}
	}

}

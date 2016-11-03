package com.gizwits.opensource.devicecontrol.ui.activity;

import java.lang.reflect.Field;
import java.util.TimeZone;

import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.date.GosScheduleData;
import com.gizwits.opensource.devicecontrol.tools.GetUTCTimeUtil;
import com.gizwits.opensource.devicecontrol.ui.activity.GosScheduleListActivity.handler_key;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

public class GosScheduleEditDateAcitivty extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private TextView tvStatus;
	private TextView tvRepeat;
	private TextView tvSave;
	private TextView tvBack;
	private LinearLayout llAction;
	private LinearLayout llRepeat;
	private NumberPicker npHour;
	private NumberPicker npMin;
	private GosScheduleData selectDate;
	private int datePosition;

	enum RequestCode {
		ACTION, REPEAT
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case SET:
				progressDialog.cancel();
				myToast(R.string.site_schedule_success);
				finish();
				break;
			case FAIL:
				progressDialog.cancel();
				myToast(R.string.site_schedule_fail);
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_schedule_edit_date);
		initDate();
		initView();
		initEvent();
	}

	@Override
	protected void onResume() {
		// onActivityResult()发生在onResume()之前
		super.onResume();
		UpdateUI();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_save:
			setRuleOnSiteAndUpdateDateBase();
			break;
		case R.id.tv_back:
			finish();
			break;
		case R.id.ll_action:
			startEditActionActivity();
			break;
		case R.id.ll_repeat:
			startEditRepeatActivity();
			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		RequestCode code = RequestCode.values()[requestCode];
		switch (code) {
		case ACTION:
			if (data != null) {
				selectDate.setOnOff(data.getBooleanExtra("action", false));
				selectDate.setViewContent();
			}
			break;

		case REPEAT:
			if (data != null) {
				selectDate.setUserPickRepeat(data.getStringExtra("repeat"));
				selectDate.setViewContent();
			}
			break;
		default:
			break;
		}

	}

	private void initDate() {

		Intent intent = getIntent();
		datePosition = intent.getIntExtra("position", -1);
		if (datePosition != -1) {
			selectDate = scheduleDates.get(datePosition);
		} else {
			String uid = spf.getString("Uid", "");
			String did = device.getDid();
			selectDate = new GosScheduleData();
			selectDate.setUid(uid);
			selectDate.setDid(did);
			selectDate.setDate(GetUTCTimeUtil.getUTCTodayDateFromLocalDate());
			selectDate.setTime("00:00");
			selectDate.setRepeat("none");
			selectDate.setUserPickRepeat("none");
			selectDate.setRuleID("");
			selectDate.setOnOff(true);
			selectDate.setDeleteOnsite(true);
			selectDate.setViewContent();
			selectDate.setTvTime("00:00");
		}

		setProgressDialog(getResources().getString(R.string.site_setting_time), true, false);
	}

	private void initEvent() {
		tvBack.setOnClickListener(this);
		llAction.setOnClickListener(this);
		tvSave.setOnClickListener(this);
		llRepeat.setOnClickListener(this);

		npMin.setMaxValue(59);
		npMin.setMinValue(0);
		npMin.setOnLongPressUpdateInterval(100);
		npMin.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		npMin.setFormatter(TWO_DIGIT_FORMATTER);
		setNumberPickerDividerColor(npMin);
		npHour.setMaxValue(23);
		npHour.setMinValue(0);
		npHour.setOnLongPressUpdateInterval(100);
		npHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		npHour.setFormatter(TWO_DIGIT_FORMATTER);
		setNumberPickerDividerColor(npHour);

		String[] time = selectDate.getTvTime().split(":");
		int hour = Integer.parseInt(time[0]);
		int min = Integer.parseInt(time[1]);
		npHour.setValue(hour);
		npMin.setValue(min);

	}

	private void initView() {
		tvStatus = (TextView) findViewById(R.id.tv_status);
		tvRepeat = (TextView) findViewById(R.id.tv_repeat);
		tvSave = (TextView) findViewById(R.id.tv_save);
		tvBack = (TextView) findViewById(R.id.tv_back);
		llAction = (LinearLayout) findViewById(R.id.ll_action);
		llRepeat = (LinearLayout) findViewById(R.id.ll_repeat);
		npHour = (NumberPicker) findViewById(R.id.np_hour);
		npMin = (NumberPicker) findViewById(R.id.np_min);
	}

	private void UpdateUI() {
		tvStatus.setText(selectDate.getTvStatus());
		if (selectDate.getUserPickRepeat().equals("none")) {
			tvRepeat.setText(getResources().getString(R.string.apm_once));			
		} else {
			tvRepeat.setText(selectDate.getTvDateOrRepeat());			
		}
	}

	/**
	 * Description:
	 */
	private void startEditActionActivity() {
	//	startActivityForResult(new Intent(this, GosScheduleEditActionAcitvity.class), RequestCode.ACTION.ordinal());
		Intent intent=new Intent(this, GosScheduleEditActionAcitvity.class);
		intent.putExtra("action", selectDate.isOnOff());
		startActivityForResult(intent,RequestCode.ACTION.ordinal());
	}

	/**
	 * Description:
	 */
	private void startEditRepeatActivity() {
		Intent intent = new Intent(this, GosScheduleEditRepeatActivity.class);
		intent.putExtra("repeat", selectDate.getUserPickRepeat());
		startActivityForResult(intent, RequestCode.REPEAT.ordinal());
	}

	/**
	 * Description:根据时间在云端创建规则和更新数据库
	 */
	private void setRuleOnSiteAndUpdateDateBase() {
		final String utcDate;
		final String utcTime;
		Time nowTime = new Time();
		nowTime.setToNow();
		int now = nowTime.hour * 60 + nowTime.minute;
		int pick = npHour.getValue() * 60 + npMin.getValue();

		if (selectDate.getUserPickRepeat().equals("none")) {
			// 没有重复天数
			if (now < pick) {
				// 当前时间早于选择时间，设置为今天
				selectDate.setDateTimeToToday(
						String.format("%02d", npHour.getValue()) + ":" + String.format("%02d", npMin.getValue()));
			} else {
				// 当前时间晚于选择时间，设置为明天
				selectDate.setDateTimeToTomorrow(
						String.format("%02d", npHour.getValue()) + ":" + String.format("%02d", npMin.getValue()));
			}
		} else {
			// 有重复天数
			utcDate = "";
			utcTime = GetUTCTimeUtil.getUTCTimeFromLocal(
					String.format("%02d", npHour.getValue()) + ":" + String.format("%02d", npMin.getValue()));

			setRepeatDateAccordingPickTime(pick);
			selectDate.setDate(utcDate);
			selectDate.setTime(utcTime);
		}

		progressDialog.show();

		selectDate.setOnSite(handler);

	}
	
	private void setRepeatDateAccordingPickTime(int pick) {
		int tz = TimeZone.getDefault().getRawOffset() / 1000 / 60;
		if (tz > 0 && tz > pick) {
			selectDate.setRepeat(wentBackOneDay(selectDate.getUserPickRepeat()));
		} else if (tz < 0 && tz < pick) {
			selectDate.setRepeat(wentFowardOneDay(selectDate.getUserPickRepeat()));
		} else {
			selectDate.setRepeat(selectDate.getUserPickRepeat());
		}
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

}

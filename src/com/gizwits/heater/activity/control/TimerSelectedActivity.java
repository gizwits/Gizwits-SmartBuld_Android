package com.gizwits.heater.activity.control;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.framework.utils.DialogManager;
import com.gizwits.framework.utils.DialogManager.On2TimingChosenListener;
import com.gizwits.heater.R;

public class TimerSelectedActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	private ToggleButton tbCountDown;

	private ToggleButton tbTiming;
	
	private TextView tvCountDown;
	
	private TextView tvTiming;

	private int CountDownHour = 0;

	private int CountDownMin = 0;

	private int TimerHour = 0;

	private int TimerMin = 0;
	
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
		tvCountDown=(TextView) findViewById(R.id.tvCountDown);
		tvTiming=(TextView) findViewById(R.id.tvTiming);
	}

	private void initEvent() {
		tbCountDown.setOnCheckedChangeListener(this);
		tbTiming.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			onBackPressed();
			break;
		// 倒计时
		case R.id.rlCountDown:
			DialogManager.get2WheelTimingDialog(
					this,
					new CountDownDialogListener(),
					getResources().getString(R.string.appointment_count_down),
					CountDownHour,
					CountDownMin,
					getResources().getString(
							R.string.appointment_count_down_label1),
					getResources().getString(
							R.string.appointment_count_down_label2)).show();
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
									R.string.appointment_timer_label2)).show();
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.tbCountDownFlag:
			if(!isChecked)
				tvCountDown.setText("00:00后");
			break;
		case R.id.tbTimingFlag:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	private void setCountDown(int HourTime, int MinTime){
		tvCountDown.setText(String.format("%02d:%02d后",HourTime,MinTime));
	}
	
	private void setTimer(int HourTime, int MinTime){
		tvTiming.setText(String.format("%02d:%02d",HourTime,MinTime));
	}

	private class CountDownDialogListener implements On2TimingChosenListener {

		@Override
		public void timingChosen(int HourTime, int MinTime) {
			setCountDown(HourTime,MinTime);
		}

	}

	private class TimerDialogListener implements On2TimingChosenListener {

		@Override
		public void timingChosen(int HourTime, int MinTime) {
			setTimer(HourTime,MinTime);
		}

	}

}

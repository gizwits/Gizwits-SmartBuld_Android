package com.gizwits.opensource.devicecontrol.ui.activity;

import java.util.ArrayList;

import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleCheckBoxListAdapter;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleCheckBoxListDateHolder;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class GosScheduleEditRepeatActivity extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private TextView tvBack;

	private TextView tvSave;

	private ListView lvRepeat;

	private String repeat;

	private ArrayList<GosScheduleCheckBoxListDateHolder> repeatDates = new ArrayList<GosScheduleCheckBoxListDateHolder>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_schedule_edit_repeat);
		initDate();
		initView();
		initEvent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		UpdateUI();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_back:
			setResult(RESULT_OK, null);
			finish();
			break;
		case R.id.tv_save:
			closeAndSaveDate();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_OK, null);
			finish();
		}
		return false;
	}

	private void initEvent() {
		tvBack.setOnClickListener(this);
		tvSave.setOnClickListener(this);
	}

	private void initView() {
		tvBack = (TextView) findViewById(R.id.tv_back);
		tvSave = (TextView) findViewById(R.id.tv_save);
		lvRepeat = (ListView) findViewById(R.id.lv_repeat);
	}

	private void initDate() {
		Intent intent = getIntent();
		repeat = intent.getStringExtra("repeat");
		String[] week = { "sun", "mon", "tue", "wed", "thu", "fri", "sat"};
		String[] itemNames = {  getResources().getString(R.string.apm_every_sun),getResources().getString(R.string.apm_every_mon),
				getResources().getString(R.string.apm_every_tue), getResources().getString(R.string.apm_every_wed),
				getResources().getString(R.string.apm_every_thu), getResources().getString(R.string.apm_every_fri),
				getResources().getString(R.string.apm_every_sat) };
		for (int i = 0; i < itemNames.length; i++) {
			String itemName = itemNames[i];
			Boolean checked = false;
			if (repeat.contains(week[i])) {
				checked = true;
			}
			GosScheduleCheckBoxListDateHolder item = new GosScheduleCheckBoxListDateHolder(itemName, checked);
			repeatDates.add(item);
		}
	}

	private void UpdateUI() {
		final GosScheduleCheckBoxListAdapter mAdapter = new GosScheduleCheckBoxListAdapter(this, repeatDates);
		lvRepeat.setAdapter(mAdapter);
		lvRepeat.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				repeatDates.get(position).checked = !repeatDates.get(position).checked;
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * Description:
	 */
	private void closeAndSaveDate() {
		StringBuilder repeaText = new StringBuilder();
		String result = "";
		for (GosScheduleCheckBoxListDateHolder i : repeatDates) {
			if (i.checked) {
				if (i.itemName.equals(getResources().getString(R.string.apm_every_mon))) {
					repeaText.append("mon,");
				}
				if (i.itemName.equals(getResources().getString(R.string.apm_every_tue))) {
					repeaText.append("tue,");
				}
				if (i.itemName.equals(getResources().getString(R.string.apm_every_wed))) {
					repeaText.append("wed,");
				}
				if (i.itemName.equals(getResources().getString(R.string.apm_every_thu))) {
					repeaText.append("thu,");
				}
				if (i.itemName.equals(getResources().getString(R.string.apm_every_fri))) {
					repeaText.append("fri,");
				}
				if (i.itemName.equals(getResources().getString(R.string.apm_every_sat))) {
					repeaText.append("sat,");
				}
				if (i.itemName.equals(getResources().getString(R.string.apm_every_sun))) {
					repeaText.append("sun,");
				}
			}
		}
		if (repeaText.length() > 0) {
			result = repeaText.substring(0, repeaText.length() - 1);
		} else {
			result = "none";
		}

		Intent intent = new Intent();
		intent.putExtra("repeat", result);
		setResult(RESULT_OK, intent);
		finish();
	}
}

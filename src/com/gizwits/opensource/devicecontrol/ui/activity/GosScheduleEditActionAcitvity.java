package com.gizwits.opensource.devicecontrol.ui.activity;

import java.util.ArrayList;

import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleCheckBoxListAdapter;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleCheckBoxListDateHolder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class GosScheduleEditActionAcitvity extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private TextView tvBack;
	private ListView lvAction;
	private ArrayList<GosScheduleCheckBoxListDateHolder> actionDates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_schedule_edit_action);
		initDate();
		initView();
		initEvent();
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
		final GosScheduleCheckBoxListAdapter mAdapter = new GosScheduleCheckBoxListAdapter(this, actionDates);
		lvAction.setAdapter(mAdapter);
		lvAction.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				for (GosScheduleCheckBoxListDateHolder date : actionDates) {
					date.checked = false;
				}
				actionDates.get(position).checked = true;
				mAdapter.notifyDataSetChanged();
				Handler handler = new Handler();
				final Intent intent = new Intent();
				if (position == 0) {
					intent.putExtra("action", true);
				} else {
					intent.putExtra("action", false);
				}
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						setResult(RESULT_OK, intent);
						finish();
					}
				}, 100);

			}
		});
	}

	private void initView() {
		tvBack = (TextView) findViewById(R.id.tv_back);
		lvAction = (ListView) findViewById(R.id.lv_action);
	}

	private void initDate() {
		actionDates = new ArrayList<GosScheduleCheckBoxListDateHolder>();
		GosScheduleCheckBoxListDateHolder open = new GosScheduleCheckBoxListDateHolder(
				getResources().getString(R.string.apm_status_open), false);
		GosScheduleCheckBoxListDateHolder close = new GosScheduleCheckBoxListDateHolder(
				getResources().getString(R.string.apm_status_close), false);
		if (getIntent().getExtras().getBoolean("action", false)) {
			open.checked = true;
			close.checked = false;
		} else {
			open.checked = false;
			close.checked = true;
		}
		actionDates.add(open);
		actionDates.add(close);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_back:
			setResult(RESULT_OK, null);
			finish();
			break;

		default:
			break;
		}

	}

}

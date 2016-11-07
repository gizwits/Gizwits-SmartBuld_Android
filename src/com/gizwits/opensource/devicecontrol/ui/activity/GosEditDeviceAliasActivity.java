package com.gizwits.opensource.devicecontrol.ui.activity;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.appkit.DeviceModule.GosDeviceListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class GosEditDeviceAliasActivity extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private TextView tvSave;

	private EditText etAlias;

	private ImageView ivDelete;

	private ImageView ivBack;

	private Handler handler = new Handler();

	private Runnable myRunnable = new Runnable() {
		public void run() {
			if (progressDialog.isShowing()) {
				progressDialog.cancel();
				myToast(R.string.updata_fail);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_edit_device_alias);
		initView();
		initEvent();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(myRunnable);
	}

	private void initEvent() {
		tvSave.setOnClickListener(this);
		tvSave.setClickable(false);
		ivBack.setOnClickListener(this);
		ivDelete.setOnClickListener(this);
		etAlias.setText(getDeviceName());
		etAlias.setSelection(etAlias.length());
		etAlias.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
		etAlias.addTextChangedListener(mTextWatcher);
	}

	private void initView() {
		ivBack = (ImageView) findViewById(R.id.iv_edit_back);
		tvSave = (TextView) findViewById(R.id.tv_save);
		etAlias = (EditText) findViewById(R.id.et_alias);
		ivDelete = (ImageView) findViewById(R.id.iv_delete);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_edit_back:
			finish();
			break;
		case R.id.tv_save:
			saveAlias();
			break;
		case R.id.iv_delete:
			clearEditText();
			break;
		default:
			break;
		}
	}

	private void saveAlias() {
		device.setCustomInfo(null, etAlias.getText().toString());
		setProgressDialog(getResources().getString(R.string.reflashing_data), true, false);
		// 显示等待栏
		progressDialog.show();
		handler.postDelayed(myRunnable, 60000);
	}

	private void backToDeviceList() {
		Intent intent = new Intent(this, GosDeviceListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void clearEditText() {
		etAlias.setText("");
	}

	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if (etAlias.getText().toString() != null && !etAlias.getText().toString().equals("")) {
				ivDelete.setVisibility(View.VISIBLE);
				tvSave.setTextColor(getResources().getColor(R.color.text_blue));
				tvSave.setClickable(true);
			} else {
				tvSave.setTextColor(getResources().getColor(R.color.gray));
				tvSave.setClickable(false);
				ivDelete.setVisibility(View.INVISIBLE);
			}
		}

	};

	@Override
	protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device2) {
		super.didSetCustomInfo(result, device2);
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
			myToast(R.string.modify_succeed);
			progressDialog.cancel();
			backToDeviceList();
		} else {
			progressDialog.cancel();
			// 修改失败
			myToast(R.string.updata_fail);
		}
	}
}

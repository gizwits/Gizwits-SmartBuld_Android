package com.gizwits.opensource.devicecontrol.ui.activity;

import java.util.concurrent.ConcurrentHashMap;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.appkit.DeviceModule.GosDeviceListActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GosDeviceMoreActivity extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private ImageView ivBack;

	private LinearLayout llSetAlias;

	private TextView tvAlias;

	private Button btnDeleteDevice;

	private LinearLayout llApointment;

	private LinearLayout llDelay;

	private Button btnDelay;

	private LinearLayout llBtnDelayDelegate;

	private TextView tvDelayCotent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comd_device_more);
		initView();
		initEvent();
		updateUI();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateUI();
	}

	private void initEvent() {
		ivBack.setOnClickListener(this);
		llSetAlias.setOnClickListener(this);
		btnDeleteDevice.setOnClickListener(this);
		llApointment.setOnClickListener(this);
		llDelay.setOnClickListener(this);
		btnDelay.setOnClickListener(this);
		llBtnDelayDelegate.setOnClickListener(this);
	}

	private void initView() {
		ivBack = (ImageView) findViewById(R.id.iv_alias_back);
		llSetAlias = (LinearLayout) findViewById(R.id.ll_set_alias);
		tvAlias = (TextView) findViewById(R.id.tv_alias);
		btnDeleteDevice = (Button) findViewById(R.id.btn_del_device);

		llApointment = (LinearLayout) findViewById(R.id.ll_schedule_item);
		llDelay = (LinearLayout) findViewById(R.id.ll_delay_item);
		btnDelay = (Button) findViewById(R.id.tb_delay);
		llBtnDelayDelegate = (LinearLayout) findViewById(R.id.ll_btn_delegate);
		tvDelayCotent = (TextView) findViewById(R.id.tv_delay_content);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_alias_back:
			finish();
			break;
		case R.id.ll_set_alias:
			startEditAliasActivity();
			break;
		case R.id.btn_del_device:
			showUnBindDialog();
			break;
		case R.id.ll_schedule_item:
			startEditDeviceTimingActivity();
			break;
		case R.id.ll_delay_item:
			startEditDeviceDelayActivity();
			break;
		case R.id.tb_delay:
		case R.id.ll_btn_delegate:
			btnDelayAction(!isOpenDelaying);
			break;
		default:
			break;
		}
	}

	private void btnDelayAction(boolean isChecked) {
		if (countDownMinute == 0) {
			startEditDeviceDelayActivity();
		} else {
			sendCommand(KEY8_COUNTDOWN_ONOFF, isChecked);
			isOpenDelaying = !isOpenDelaying;
			updateUI();
		}
	}

	/**
	 * Description:
	 */
	private void updateUI() {
		btnDelay.setSelected(isOpenDelaying);
		tvAlias.setText(getDeviceName());
		if (isOpenDelaying) {
			tvDelayCotent.setText(countDownMinute + getResources().getString(R.string.apm_min_later));
		} else {
			tvDelayCotent.setText(null);
		}
	}

	/**
	 * Description:
	 */
	private void startEditDeviceDelayActivity() {
		startActivity(new Intent(this, GosEditDeviceDelayActivity.class));
	}

	/**
	 * Description:
	 */
	private void startEditDeviceTimingActivity() {
		startActivity(new Intent(this, GosScheduleListActivity.class));
	}

	private void startEditAliasActivity() {
		if (!device.isBind()) {
			myToast(R.string.device_not_bind);
			return;
		}
		startActivity(new Intent(this, GosEditDeviceAliasActivity.class));
	}

	private void unboundAtion() {
		setProgressDialog(getResources().getString(R.string.unbounding_device), true, true);
		// 显示等待栏
		progressDialog.show();
		GizWifiSDK.sharedInstance().unbindDevice(spf.getString("Uid", ""), spf.getString("Token", ""), device.getDid());

	}

	private void backToDeviceList() {
		Intent intent = new Intent(this, GosDeviceListActivity.class);
		// 关闭在GosDeviceListActivity后面所有打开的Activity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * Description: 显示解除绑定提示框
	 */
	@SuppressLint("InflateParams")
	private void showUnBindDialog() {
		if (!device.isBind()) {
			myToast(R.string.device_not_bind);
			return;
		}
		final Dialog dialog = new Dialog(this, R.style.noBackgroundDialog) {
		};
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View v = layoutInflater.inflate(R.layout.dialog_comd_unbind_device, null);
		Button leftBtn = (Button) v.findViewById(R.id.left_btn);
		Button rightBtn = (Button) v.findViewById(R.id.right_btn);
		leftBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		rightBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				unboundAtion();
			}
		});
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.setContentView(v);
		dialog.show();
	}

	@Override
	protected void didUnbindDevice(GizWifiErrorCode result, String did) {
		super.didUnbindDevice(result, did);
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
			myToast(R.string.unbound_succeed);
			backToDeviceList();
		} else {
			// 解绑失败
			myToast(R.string.unbound_failed);
		}
		progressDialog.cancel();
	}

	@Override
	protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int sn) {
		super.didReceiveData(result, device, dataMap, sn);
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
			deviceDataMap = dataMap;
			getDataFromDateMap();
			updateUI();
		}
	}

}

package com.gizwits.opensource.appkit.UserModule;

import java.util.Timer;
import java.util.TimerTask;

import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizUserAccountType;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.appkit.CommonModule.GosDeploy;
import com.gizwits.opensource.smartlight.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

@SuppressLint("HandlerLeak")
public class GosRegisterUserActivity extends GosUserModuleBaseActivity implements OnClickListener {

	/** The et Name */
	private EditText etName;

	/** The btn GetCode */
	private Button btnGetCode;

	/** The et Code */
	private EditText etCode;

	/** The et Psw */
	private EditText etPsw;

	/** The btn Rrgister */
	private Button btnRrgister;

	/** The cb Laws */
	private CheckBox cbLaws;

	/**
	 * 验证码重发倒计时
	 */
	int secondleft = 60;

	/**
	 * The timer.
	 */
	Timer timer;

	/** 数据变量 */
	String name, code, psw;

	private enum handler_key {

		/** 获取验证码. */
		GETCODE,

		/** 提示信息 */
		TOAST,

		/** 手机验证码发送成功. */
		SENDSUCCESSFUL,

		/**
		 * 倒计时通知
		 */
		TICK_TIME,

		/** 注册 */
		REGISTER,
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case GETCODE:
				progressDialog.show();
				String AppSecret = GosDeploy.setAppSecret();
				GizWifiSDK.sharedInstance().requestSendPhoneSMSCode(AppSecret, msg.obj.toString());

				break;
			case TOAST:
				Toast.makeText(GosRegisterUserActivity.this, msg.obj.toString(), 2000).show();
				String successfulText = (String) getText(R.string.register_successful);

				if (msg.obj.toString().equals(successfulText)) {
					spf.edit().putString("UserName", name).commit();
					spf.edit().putString("PassWord", psw).commit();
					finish();
				}
				break;
			case SENDSUCCESSFUL:
				etName.setEnabled(false);
				etName.setTextColor(getResources().getColor(R.color.text_gray_light));
				isStartTimer();

				break;

			case TICK_TIME:
				String getCodeAgain = getString(R.string.getcode_again);
				String timerMessage = getString(R.string.timer_message);
				secondleft--;
				if (secondleft <= 0) {
					timer.cancel();
					btnGetCode.setBackgroundResource(R.drawable.btn_getcode_shape);
					// btnGetCode.setBackgroundDrawable(GosDeploy.setButtonBackgroundColor());
					btnGetCode.setTextColor(GosDeploy.setButtonTextColor());
					btnGetCode.setEnabled(true);
					btnGetCode.setText(getCodeAgain);
				} else {
					btnGetCode.setText(secondleft + timerMessage);
				}
				break;
			case REGISTER:
				progressDialog.show();
				GizWifiSDK.sharedInstance().registerUser(name, psw, code, GizUserAccountType.GizUserPhone);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gos_register_user);
		// 设置ActionBar
		setActionBar(true, true, R.string.register);
		initView();
		initEvent();
	}

	private void initView() {
		etName = (EditText) findViewById(R.id.etName);
		btnGetCode = (Button) findViewById(R.id.btnGetCode);
		etCode = (EditText) findViewById(R.id.etCode);
		etPsw = (EditText) findViewById(R.id.etPsw);
		btnRrgister = (Button) findViewById(R.id.btnRrgister);
		cbLaws = (CheckBox) findViewById(R.id.cbLaws);

		// 配置文件部署
		// btnGetCode.setBackgroundDrawable(GosDeploy.setButtonBackgroundColor());
		// btnGetCode.setTextColor(GosDeploy.setButtonTextColor());
		btnRrgister.setBackgroundDrawable(GosDeploy.setButtonBackgroundColor());
		btnRrgister.setTextColor(GosDeploy.setButtonTextColor());

	}

	private void initEvent() {
		final Timer etTimer = new Timer();
		etTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				etName.requestFocus();
				InputMethodManager imm = (InputMethodManager) etName.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
				etTimer.cancel();

			}
		}, 500);

		btnGetCode.setOnClickListener(this);
		btnRrgister.setOnClickListener(this);

		cbLaws.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String psw = etPsw.getText().toString();

				if (isChecked) {
					etPsw.setInputType(0x90);
				} else {
					etPsw.setInputType(0x81);
				}
				etPsw.setSelection(psw.length());
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnGetCode:
			name = etName.getText().toString();
			if (TextUtils.isEmpty(name)) {
				Toast.makeText(GosRegisterUserActivity.this, R.string.toast_name_wrong, 2000).show();
				return;
			}
			Message msg = new Message();
			msg.obj = name;
			msg.what = handler_key.GETCODE.ordinal();
			handler.sendMessage(msg);
			break;

		case R.id.btnRrgister:
			name = etName.getText().toString();
			code = etCode.getText().toString();
			psw = etPsw.getText().toString();
			if (TextUtils.isEmpty(name)) {
				Toast.makeText(GosRegisterUserActivity.this, R.string.toast_name_wrong, 2000).show();
				return;
			}
			if (code.length() != 6) {
				Toast.makeText(GosRegisterUserActivity.this, R.string.no_getcode, 2000).show();
				return;
			}
			if (TextUtils.isEmpty(psw)) {
				Toast.makeText(GosRegisterUserActivity.this, R.string.toast_psw_wrong, 2000).show();
				return;
			}
			if (psw.length() < 6) {
				Toast.makeText(GosRegisterUserActivity.this, R.string.toast_psw_short, 2000).show();
				return;
			}
			handler.sendEmptyMessage(handler_key.REGISTER.ordinal());
			break;
		}
	}

	/** 手机验证码回调 */
	@Override
	protected void didRequestSendPhoneSMSCode(GizWifiErrorCode result, String token) {
		progressDialog.cancel();
		Message msg = new Message();
		if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
			// String sendFailed = (String) getText(R.string.send_failed);
			msg.what = handler_key.TOAST.ordinal();
			msg.obj = toastError(result);// sendFailed + "\n" + errorMessage;
			handler.sendMessage(msg);
		} else {
			handler.sendEmptyMessage(handler_key.SENDSUCCESSFUL.ordinal());
			msg.what = handler_key.TOAST.ordinal();
			String sendSuccessful = (String) getText(R.string.send_successful);
			msg.obj = sendSuccessful;
			handler.sendMessage(msg);
		}
	}

	/** 用户注册回调 */
	@Override
	protected void didRegisterUser(GizWifiErrorCode result, String uid, String token) {
		progressDialog.cancel();
		if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
			// String registerFailed = (String)
			// getText(R.string.register_failed);
			Message msg = new Message();
			msg.what = handler_key.TOAST.ordinal();
			msg.obj = toastError(result);// registerFailed + "\n" +
											// errorMessage;
			handler.sendMessage(msg);
		} else {
			Message msg = new Message();
			msg.what = handler_key.TOAST.ordinal();
			String registerSuccessful = (String) getText(R.string.register_successful);
			msg.obj = registerSuccessful;
			handler.sendMessage(msg);
		}
	}

	/**
	 * 倒计时
	 */
	public void isStartTimer() {
		btnGetCode.setEnabled(false);
		btnGetCode.setBackgroundResource(R.drawable.btn_getcode_shape_gray);
		secondleft = 60;
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
			}
		}, 1000, 1000);
	}

}

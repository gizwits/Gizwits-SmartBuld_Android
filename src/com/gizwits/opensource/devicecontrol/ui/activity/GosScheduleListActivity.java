package com.gizwits.opensource.devicecontrol.ui.activity;

import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.date.GosScheduleData;
import com.gizwits.opensource.devicecontrol.tools.GetUTCTimeUtil;
import com.gizwits.opensource.devicecontrol.tools.GosScheduleSiteTool;
import com.gizwits.opensource.devicecontrol.tools.GosScheduleSiteTool.OnResponListener;
import com.gizwits.opensource.devicecontrol.tools.GosScheduleSiteTool.OnResponseGetDeviceDate;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleListAdapter;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleListAdapter.DeleteButtonClickListener;
import com.gizwits.opensource.devicecontrol.ui.adapter.GosScheduleListAdapter.ToggleButtonClickListener;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DaoConfig;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.gizwits.opensource.devicecontrol.ui.adapter.SlideListView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GosScheduleListActivity extends GosDeviceControlModuleBaseActivity implements OnClickListener {

	private SlideListView slideListview;

	private GosScheduleListAdapter mAdapter;

	private GosScheduleSiteTool siteTool;

	private DbUtils dbUtils;

	private TextView tvNoRule;

	private View diverTop;

	private View diverBottom;

	private ImageView ivAdd;

	private ImageView ivBack;

	public enum handler_key {

		/** 在云端删除 */
		DELETE,

		/** 在云端设置 */
		SET,

		/** 在云端设置 失败 */
		FAIL,
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case DELETE:
				upDateListViewUI();
				break;

			case SET:
				upDateListViewUI();
				myToast(R.string.site_schedule_success);
				break;
			case FAIL:
				upDateListViewUI();
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
		setContentView(R.layout.activity_comd_schedule_list);
		initDate();
		initView();
		initEvent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		reflashDateOnSiteOrLocal();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_apm_add:
			// 点击添加按钮进入编辑界面
			Intent intent = new Intent(this, GosScheduleEditDateAcitivty.class);
			intent.putExtra("position", -1);
			startActivity(intent);
			break;
		case R.id.iv_schedule_back:
			finish();
			break;
		default:
			break;
		}

	}

	//////////////////////////////////////////////////////////////
	////////////////////////////////////////////
	// 注意这里的代码要移动到gosapplication中
	private void initDate() {
		siteTool = new GosScheduleSiteTool(this, device, spf.getString("Token", ""));
		DbUtils.DaoConfig config = new DaoConfig(this);
		config.setDbName("gizwits");
		config.setDbVersion(1); // db版本
		dbUtils = DbUtils.create(config);// db还有其他的一些构造方法，比如含有更新表版本的监听器的DbUtils
		try {
			// 创建一张表
			dbUtils.createTableIfNotExist(GosScheduleData.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
		GosScheduleData.setSiteTool(siteTool);
		GosScheduleData.setDbUtils(dbUtils);
		GosScheduleData.setContext(getApplicationContext());
		setProgressDialog(getResources().getString(R.string.site_setting_time), true, false);
	}

	private void initEvent() {
		ivAdd.setOnClickListener(this);
		ivBack.setOnClickListener(this);
	}

	private void initView() {
		slideListview = (SlideListView) findViewById(R.id.slide_listView);
		slideListview.initSlideMode(SlideListView.MOD_RIGHT);

		tvNoRule = (TextView) findViewById(R.id.tv_no_rule);
		diverTop = (View) findViewById(R.id.vw_list_top_divider);
		diverBottom = (View) findViewById(R.id.vw_list_bottom_divider);
		ivAdd = (ImageView) findViewById(R.id.iv_apm_add);
		ivBack = (ImageView) findViewById(R.id.iv_schedule_back);
	}

	private void UpDataUI() {
		if (scheduleDates.size() > 0) {
			tvNoRule.setVisibility(View.GONE);
			slideListview.setVisibility(View.VISIBLE);
			diverTop.setVisibility(View.VISIBLE);
			diverBottom.setVisibility(View.VISIBLE);
		} else {
			tvNoRule.setVisibility(View.VISIBLE);
			slideListview.setVisibility(View.GONE);
			diverTop.setVisibility(View.GONE);
			diverBottom.setVisibility(View.GONE);
		}
		mAdapter = new GosScheduleListAdapter(scheduleDates, this, tbListener, deleListener);
		slideListview.setAdapter(mAdapter);
		slideListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 点击栏目进入编辑
				Intent intent = new Intent(GosScheduleListActivity.this, GosScheduleEditDateAcitivty.class);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});
	}

	// 用户点击删除按钮
	private DeleteButtonClickListener deleListener = new DeleteButtonClickListener() {

		@Override
		public void onclick(View v) {

			final int positon = (Integer) v.getTag();
			final GosScheduleData tempDate = scheduleDates.get(positon);
			// 删除时候先判断该栏目是否已经在云端删除,如果已经在云端删除，则直接删除数据库
			if (tempDate.isDeleteOnsite()) {
				deleInDateBaseAndUpdateUI(positon, tempDate);
			} else {
				// 在云端存在，则同时删除云端与数据库
				siteTool.deleteTimeOnSite(tempDate.getRuleID(), new OnResponListener() {

					@Override
					public void OnRespon(int result, String arg0) {
						if (result == 0) {
							deleInDateBaseAndUpdateUI(positon, tempDate);
						}

					}
				});
			}

		}

		private void deleInDateBaseAndUpdateUI(final int positon, final GosScheduleData tempDate) {
			// 删除数据库
			try {
				dbUtils.delete(tempDate);
			} catch (DbException e) {
				e.printStackTrace();
			}
			// 删除列表
			scheduleDates.remove(positon);
			upDateListViewUI();
			slideListview.slideBack();
		}

	};

	// 用户点击关闭按钮
	private ToggleButtonClickListener tbListener = new ToggleButtonClickListener() {

		@Override
		public void onclick(View v) {

			final GosScheduleData tempDate = scheduleDates.get((Integer) v.getTag());

			if (v.isSelected()) {
				// 打开状态变关闭
				deleRuleOnSite(tempDate);

			} else {
				// 关闭状态变打开
				setRuleOnSite(tempDate);
			}

		}

	};

	private void reflashDateOnSiteOrLocal() {

		// 该用户是否第一次进入该页面，第一次进入则将其数据写入本地
		if (spf.getBoolean("isFisrstLoad" + spf.getString("Uid", ""), true)) {
			// 显示等待栏
			setProgressDialog(getResources().getString(R.string.reflashing_data), true, false);
			progressDialog.show();

			siteTool.getTimeOnSite(new OnResponseGetDeviceDate() {
				@Override
				public void onReceviceDate(List<ConcurrentHashMap<String, Object>> dataList) {
					progressDialog.cancel();
					if (dataList != null) {
						spf.edit().putBoolean("isFisrstLoad" + spf.getString("Uid", ""), false).commit();
						writeToLocalDatabase(dataList);
						getDateFromDateBaseAndInitDate();
						UpDataUI();
					} else {
						myToast(R.string.updata_fail);
						finish();
					}
				}
			});
		} else {
			// 从本地数据库获取数据并赋值给存储的列表scheduleDates
			getDateFromDateBaseAndInitDate();
		}


		// 更新ui
		UpDataUI();
	}

	/**
	 * Description:点击关闭按钮，在云端删除规则
	 * 
	 * @param tempDate
	 */
	private void deleRuleOnSite(GosScheduleData tempDate) {

		progressDialog.show();

		tempDate.deleteOnSite(handler);
	}

	/**
	 * Description:点击打开按钮，在云端创建规则
	 * 
	 * @param scheduleData
	 */
	protected void setRuleOnSite(GosScheduleData scheduleData) {

		progressDialog.show();

		// 先判断有没有重复周数
		if (scheduleData.getRepeat().equals("none")) {
			// 没有重复周数，当前时间晚于数据库记录时间，即数据库时间已经无效,需要设置为明天
			if (scheduleData.isLocalTimeLaterThanSiteTime()) {
				if (scheduleData.isTvTimeLaterThanLocalTime()) {
					// 当前栏显示时间是否比当前的时间晚，是的话设置为今天,这个判断是防止多天后更改定时预约出错
					scheduleData.setDateTimeToToday(scheduleData.getTvTime());
				} else {
					scheduleData.setDateTimeToTomorrow(scheduleData.getTvTime());
				}
			} else {
				// 当前时间早于数据库记录时间，即数据库时间已经有效，可以直接设置
			}
		}
		scheduleData.setOnSite(handler);
	}

	private void getDateFromDateBaseAndInitDate() {
		String uid = spf.getString("Uid", "");
		String did = device.getDid();
		try {
			scheduleDates.clear();
			scheduleDates = dbUtils.findAll(
					Selector.from(GosScheduleData.class).where("uid", "=", uid).and(WhereBuilder.b("did", "=", did)));
		} catch (DbException e) {
			e.printStackTrace();
		}
		for (GosScheduleData i : scheduleDates) {
			i.setViewContent();
		}
	}

	/**
	 * Description:将云端信息写入到本地数据库
	 * 
	 * @param dataList
	 */
	@SuppressWarnings("unchecked")
	protected void writeToLocalDatabase(List<ConcurrentHashMap<String, Object>> dataList) {

		String uid = spf.getString("Uid", "");

		for (ConcurrentHashMap<String, Object> map : dataList) {
			String date = (String) map.get("date");
			String time = (String) map.get("time");
			String repeat = (String) map.get("repeat");
			String ruleID = (String) map.get("ruleID");
			String did = (String) map.get("did");
			ConcurrentHashMap<String, Object> dataMap = (ConcurrentHashMap<String, Object>) map.get("dataMap");
			Boolean onOff = (Boolean) dataMap.get("Power_Switch");
			GosScheduleData newDate = new GosScheduleData();
			newDate.setUid(uid);
			newDate.setDid(did);
			newDate.setDate(date);
			newDate.setTime(time);
			newDate.setRepeat(repeat);
			setUserPickRepeat(time, repeat, newDate);
			newDate.setRuleID(ruleID);
			newDate.setOnOff(onOff);
			newDate.setDeleteOnsite(false);
			
			try {
				dbUtils.save(newDate);
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		
	}
	
	private void setUserPickRepeat(String time, String repeat, GosScheduleData newDate) {
		if (repeat.equals("none")) {
			newDate.setUserPickRepeat("none");
		} else {
			int tz = TimeZone.getDefault().getRawOffset() / 1000 / 60;
			String[] str = GetUTCTimeUtil.getLocalTimeFromUTC(time).split(":");
			int tm = Integer.parseInt(str[0]) + Integer.parseInt(str[1]) * 60;
			if (tz > 0 && tz > tm) {
				newDate.setUserPickRepeat(wentFowardOneDay(repeat));
			} else if (tz < 0 && tz < tm) {
				newDate.setUserPickRepeat(wentBackOneDay(repeat));
			} else {
				newDate.setUserPickRepeat(repeat);
			}
		}
	}

	private void upDateListViewUI() {
		progressDialog.cancel();
		mAdapter.setmList(scheduleDates);
		mAdapter.notifyDataSetChanged();
		if (scheduleDates.size() == 0) {
			UpDataUI();
		}
	}

}

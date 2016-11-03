/**
 * 
 */
package com.gizwits.opensource.devicecontrol.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import com.gizwits.opensource.smartlight.R;
import com.gizwits.opensource.devicecontrol.tools.GetUTCTimeUtil;
import com.gizwits.opensource.devicecontrol.tools.GosScheduleSiteTool;
import com.gizwits.opensource.devicecontrol.tools.GosScheduleSiteTool.OnResponListener;
import com.gizwits.opensource.devicecontrol.ui.activity.GosScheduleListActivity;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;
import com.lidroid.xutils.exception.DbException;

import android.content.Context;
import android.os.Handler;
import android.text.format.Time;

/**
 * Description:此数据类是采用了xutil数据库框架进行构造的
 * 
 * @author Refon
 */
@Table // 记录云端预约信息的表
public class GosScheduleData {

	@Id // 主键，当为int类型时，默认自增。 非自增时，需要设置id的值
	private int id;
	@Column // 列名，用户uid
	private String uid;
	@Column // 列名，当前设备的did
	private String did;
	@Column // 列名，云端保存的日期，UTC时间
	private String date;
	@Column // 列名，云端保存的时间，UTC时间
	private String time;
	@Column // 列名，云端保存的重复天数，
	private String repeat;
	@Column(column = "user_pick_repeat") // 列名，用户选择的重复周数，跟云端保存的可能不太一致
	private String userPickRepeat;	
	@Column // 列名，云端保存的数据点操作的值
	private boolean onOff;
	@Column // 列名，云端记录的id
	private String ruleID;
	@Column // 列名,是否在云端被删除
	private boolean isDeleteOnsite;

	// 不写入数据库表结构
	@Transient // 列表中显示日期还是重复，还是什么都不显示
	private String tvDateOrRepeat;
	@Transient // 列表中显示开启或关闭
	private String tvStatus;
	@Transient // 列表中显示时间
	private String tvTime;
	@Transient // 列表中按钮是否打开
	private Boolean btnIsOpen;

	private static Context context;
	private static GosScheduleSiteTool siteTool;
	private static DbUtils dbUtils;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getDid() {
		return did;
	}

	public void setDid(String did) {
		this.did = did;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public boolean isOnOff() {
		return onOff;
	}

	public void setOnOff(boolean onOff) {
		this.onOff = onOff;
	}

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}

	public boolean isDeleteOnsite() {
		return isDeleteOnsite;
	}

	public void setDeleteOnsite(boolean isDeleteOnsite) {
		this.isDeleteOnsite = isDeleteOnsite;
	}

	public String getTvDateOrRepeat() {
		return tvDateOrRepeat;
	}

	public void setTvDateOrRepeat(String tvDateOrRepeat) {
		this.tvDateOrRepeat = tvDateOrRepeat;
	}

	public String getTvStatus() {
		return tvStatus;
	}

	public void setTvStatus(String tvStatus) {
		this.tvStatus = tvStatus;
	}

	public String getTvTime() {
		return tvTime;
	}

	public void setTvTime(String tvTime) {
		this.tvTime = tvTime;
	}

	public Boolean getBtnIsOpen() {
		return btnIsOpen;
	}

	public void setBtnIsOpen(Boolean btnIsOpen) {
		this.btnIsOpen = btnIsOpen;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		GosScheduleData.context = context;
	}

	public static DbUtils getDbUtils() {
		return dbUtils;
	}

	public static void setDbUtils(DbUtils dbUtils) {
		GosScheduleData.dbUtils = dbUtils;
	}

	public static GosScheduleSiteTool getSiteTool() {
		return siteTool;
	}

	public static void setSiteTool(GosScheduleSiteTool siteTool) {
		GosScheduleData.siteTool = siteTool;
	}

	public String getUserPickRepeat() {
		return userPickRepeat;
	}

	public void setUserPickRepeat(String userPickRepeat) {
		this.userPickRepeat = userPickRepeat;
	}
	
	// -----------根据存储的值的内容来进行设定显示的内容---------------

	/**
	 * Description:根据保存的数据设置list中一个栏目显示的内容
	 */
	public void setViewContent() {

		// 下列方法执行顺序不能更改
		setTvStatusTextWithOnOff();
		setTvTimeTextWithSavedUTCTime();
		setBtnIsOpen(getIsOpenAccordingDateAndTime());
		setDateOrRepeatTextAccordingRepeat();
	}

	/**
	 * Description:设置重复天数显示内容
	 */
	private void setDateOrRepeatTextAccordingRepeat() {
		
		if (userPickRepeat.equals("none")) {

			// 是否处于关闭状态或者是否在云端删除，是的话则显示仅一次
			if (!getBtnIsOpen() || isDeleteOnsite()) {
				setTvDateOrRepeat(context.getResources().getString(R.string.apm_once));
				return;
			}

			// 转换为本地时间来比较该时间是否有效
			String localTime = getLocalTimeString();
			String siteTime = getSiteTimeString(getDate(), getTime());
			// 去掉后面四个字符表示为当前日期
			Long longLocalDate = Long.parseLong(localTime.substring(0, localTime.length() - 4));
			Long longSiteDate = Long.parseLong(siteTime.substring(0, siteTime.length() - 4));
			if ((longLocalDate - longSiteDate) > 0) {
				// 云端日期是昨天之前
				setTvDateOrRepeat(context.getResources().getString(R.string.apm_once));
			}
			if ((longLocalDate - longSiteDate) == 0) {
				// 云端日期是今天
				Long longLocalTime = Long.parseLong(localTime);
				Long longSiteTime = Long.parseLong(siteTime);
				if (longLocalTime - longSiteTime < 0) {
					// 今天未执行的任务，今天仅一次
					setTvDateOrRepeat(context.getResources().getString(R.string.apm_today));
				} else {
					// 今天已执行的任务
					setTvDateOrRepeat(context.getResources().getString(R.string.apm_once));
				}

			}
			if ((longLocalDate - longSiteDate) < 0) {
				// 云端日期是明天，明天|仅一次
				setTvDateOrRepeat(context.getResources().getString(R.string.apm_tomorrow));
			}

		} else {
			setTvRepeatDates(userPickRepeat);
		}
	}

	private void setTvRepeatDates(String repeat) {
		StringBuilder repeaText = new StringBuilder();
		int i = 0;
		if (repeat.contains("mon")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_mon) + " ");
		}
		if (repeat.contains("tue")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_tue) + " ");
		}
		if (repeat.contains("wed")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_wed) + " ");
		}
		if (repeat.contains("thu")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_thu) + " ");
		}
		if (repeat.contains("fri")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_fri) + " ");
		}
		if (repeat.contains("sat")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_sat) + " ");
		}
		if (repeat.contains("sun")) {
			i++;
			repeaText.append(context.getResources().getString(R.string.apm_sun) + " ");
		}
		if (i == 7) {
			setTvDateOrRepeat(context.getResources().getString(R.string.apm_every_day));
		} else {
			setTvDateOrRepeat(repeaText.toString());
		}
	}

	/**
	 * Description:根据UTC时间转换为当前时区的时间，比如201608061120，中间没有任何符号
	 */
	private String getSiteTimeString(String date, String time) {
		String result = GetUTCTimeUtil.getLocalDateTimeFromUTCDateTime(date + " " + time);
		result = result.replace("-", "");
		result = result.replace(" ", "");
		result = result.replace(":", "");
		return result;
	}

	/**
	 * Description:返回当前时间，比如201608061120，中间没有任何符号
	 */
	private String getLocalTimeString() {
		Time nowTime = new Time();
		nowTime.setToNow();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(nowTime.year);
		stringBuilder.append(String.format("%02d", nowTime.month + 1));
		stringBuilder.append(String.format("%02d", nowTime.monthDay));
		stringBuilder.append(String.format("%02d", nowTime.hour));
		stringBuilder.append(String.format("%02d", nowTime.minute));
		return stringBuilder.toString();
	}

	/**
	 * Description:根据保存的UTC时间设置时间文本框显示内容
	 */
	private void setTvTimeTextWithSavedUTCTime() {
		setTvTime(GetUTCTimeUtil.getLocalTimeFromUTC(getTime()));
	}

	/**
	 * Description:根据保存的插座开关动作设置状态文本框显示内容
	 */
	private void setTvStatusTextWithOnOff() {
		if (isOnOff()) {
			setTvStatus(context.getResources().getString(R.string.apm_status_open));
		} else {
			setTvStatus(context.getResources().getString(R.string.apm_status_close));
		}
	}

	/**
	 * Description:获取没有分隔符的时间文本
	 */
	private String getTimeStringWithoutSeparator(String time) {
		return time.replace("-", "").replace(" ", "").replace(":", "");
	}

	/**
	 * Description:获取保存的设置数据点的集合
	 */
	public ConcurrentHashMap<String, Object> getAttrsMapFromDate() {
		ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<String, Object>();
		attrs.put("Power_Switch", isOnOff());
		return attrs;
	}

	/**
	 * Description:根据日期和时间来获取是否打开
	 */
	private Boolean getIsOpenAccordingDateAndTime() {
		Boolean result = true;

		if (getRepeat().equals("none")) {
			// 没有重复的情况，如果在云端已经删除或者已经被执行了，则关闭
			if (isLocalTimeLaterThanSiteTime() || isDeleteOnsite()) {
				result = false;
			}

		} else {
			if (isDeleteOnsite()) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Description:当前时间晚于保存的时间
	 * 
	 * @return true:表示当前时间晚于保存时间
	 */
	public boolean isLocalTimeLaterThanSiteTime() {
		String localUTCTime = getTimeStringWithoutSeparator(GetUTCTimeUtil.getPresentUTCTimeStr());
		String siteUTCTime = getTimeStringWithoutSeparator(getDate() + getTime());
		Long longLocal = Long.parseLong(localUTCTime);
		Long longSite = Long.parseLong(siteUTCTime);
		if (longLocal >= longSite) {
			return true;
		}
		return false;
	}

	/**
	 * Description:云端日期是否晚于当前日期,即云端日期是否为明天
	 */
	public boolean isTvTimeLaterThanLocalTime() {
		Time nowTime = new Time();
		nowTime.setToNow();
		int now = nowTime.hour * 60 + nowTime.minute;
		String str[] = getTvTime().split(":");
		int site = Integer.parseInt(str[0]) * 60 + Integer.parseInt(str[1]);
		if (site > now) {
			return true;
		}
		return false;
	}

	/**
	 * Description:设置为今天的这个时间
	 */
	public void setDateTimeToToday(String localTime) {

		String[] str = GetUTCTimeUtil.getUTCDateTimeFromLocalDateTime(getNowDayTime(localTime)).split(" ");
		setDate(str[0]);
		setTime(str[1]);

	}

	/**
	 * Description:设置为明天的这个时间
	 */
	public void setDateTimeToTomorrow(String localTime) {
		String str = GetUTCTimeUtil.getUTCDateTimeFromLocalDateTime(getNowDayTime(localTime));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		try {
			Date today = format.parse(str);
			String tomorrow = format.format(today.getTime() + 24 * 60 * 60 * 1000);
			String[] str2 = tomorrow.split(" ");
			setDate(str2[0]);
			setTime(str2[1]);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description:根据传入时间转换为当天此刻的时间，比如今天为2016年8月20日， 传入00：00，这转换为今天的00：00,即2016-
	 * 08-20 00：00
	 */
	private String getNowDayTime(String localTime) {
		StringBuffer UTCTimeBuffer = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		UTCTimeBuffer.append(year).append("-").append(String.format("%02d", month)).append("-")
				.append(String.format("%02d", day));

		UTCTimeBuffer.append(" " + localTime);
		return UTCTimeBuffer.toString();
	}

	// --------------云端交互代码--------------------

	/**
	 * Description:将此数据发送之云端
	 * 
	 * @param handler
	 *            利用handler来进行结果的异步回调
	 */
	public void setOnSite(final Handler handler) {
		// 判断该数据是否已在云端被删除
		if (isDeleteOnsite()) {
			// 已被删除，直接设置
			immediatelySetToSite(handler);
		} else {
			// 先删除再设置
			siteTool.deleteTimeOnSite(getRuleID(), new OnResponListener() {

				@Override
				public void OnRespon(int result, String arg0) {
					if (result == 0) {
						setDeleteOnsite(true);// 更新删除状态
						try {
							dbUtils.saveOrUpdate(GosScheduleData.this);// 更新整条数据
						} catch (DbException e) {
							e.printStackTrace();
						}
						immediatelySetToSite(handler);
					}
				}
			});
		}
	}

	private void immediatelySetToSite(final Handler handler) {
		siteTool.setCommadOnSite(getDate(), getTime(), getRepeat(), getAttrsMapFromDate(), new OnResponListener() {

			@Override
			public void OnRespon(int result, String arg0) {
				if (result == 0) {
					// 数据库已经存在
					setRuleID(arg0);// 创建成功后记录id
					setDeleteOnsite(false);// 更新删除状态
					setViewContent();// 更新显示内容
					try {
						dbUtils.saveOrUpdate(GosScheduleData.this);// 更新整条数据
					} catch (DbException e) {
						e.printStackTrace();
					}
					handler.sendEmptyMessage(GosScheduleListActivity.handler_key.SET.ordinal());
				} else {
					handler.sendEmptyMessage(GosScheduleListActivity.handler_key.FAIL.ordinal());
				}
			}
		});
	}

	/**
	 * Description:将此数据从云端中删除
	 * 
	 * @param handler
	 *            利用handler来进行结果的异步回调
	 */
	public void deleteOnSite(final Handler handler) {
		siteTool.deleteTimeOnSite(getRuleID(), new OnResponListener() {

			@Override
			public void OnRespon(int result, String arg0) {
				if (result == 0) {
					setDeleteOnsite(true);// 更新删除状态
					setViewContent();// 更新显示内容
					try {
						dbUtils.saveOrUpdate(GosScheduleData.this);// 更新整条数据
					} catch (DbException e) {
						e.printStackTrace();
					}
					handler.sendEmptyMessage(GosScheduleListActivity.handler_key.DELETE.ordinal());
				}
			}
		});

	}
}

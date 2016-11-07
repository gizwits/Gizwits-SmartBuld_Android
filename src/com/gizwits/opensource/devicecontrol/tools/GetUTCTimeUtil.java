/**
 * 
 */
package com.gizwits.opensource.devicecontrol.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class GetUTCTimeUtil {

	/**
	 * 根据本地时间获取，得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm"<br />
	 * 如果获取失败，返回null
	 * 
	 * @return
	 */
	public static String getPresentUTCTimeStr() {
		StringBuffer UTCTimeBuffer = new StringBuffer();
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		UTCTimeBuffer.append(year).append("-").append(String.format("%02d", month)).append("-")
				.append(String.format("%02d", day));
		UTCTimeBuffer.append(" ").append(String.format("%02d", hour)).append(":").append(String.format("%02d", minute));
		return UTCTimeBuffer.toString();
	}

	/**
	 * 将UTC时间转换为北京时间，时间格式为"HH:mm"
	 * 
	 * @param UTCTime
	 * @return
	 */
	public static String getLocalTimeFromUTC(String UTCTime) {
		java.util.Date UTCDate = null;
		String localTimeStr = null;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
		try {
			UTCDate = format.parse(UTCTime);
			localTimeStr = format.format(UTCDate.getTime() + TimeZone.getDefault().getRawOffset());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return localTimeStr;
	}

	/**
	 * 将北京时间转换为UTC时间，时间格式为"HH:mm"
	 * 
	 * @param UTCTime
	 * @return
	 */
	public static String getUTCTimeFromLocal(String localTime) {
		java.util.Date UTCDate = null;
		String localTimeStr = null;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
		try {
			UTCDate = format.parse(localTime);
			localTimeStr = format.format(UTCDate.getTime() - TimeZone.getDefault().getRawOffset());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return localTimeStr;
	}

	/**
	 * Description:将北京时间转换为UTC时间，格式为"yyyy-MM-dd HH:mm"
	 * 
	 * @param localTimeStr
	 * @return
	 */
	public static String getUTCDateTimeFromLocalDateTime(String localTimeStr) {
		java.util.Date nowDate = null;
		String UTCDate = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		try {
			nowDate = format.parse(localTimeStr);
			UTCDate = format.format(nowDate.getTime() - TimeZone.getDefault().getRawOffset());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return UTCDate;

	}

	/**
	 * Description:获取今天日期对应的utc日期
	 * 
	 * @param localTimeStr
	 * @return
	 */
	public static String getUTCTodayDateFromLocalDate() {
		StringBuffer UTCTimeBuffer = new StringBuffer();
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		UTCTimeBuffer.append(year).append("-").append(String.format("%02d", month)).append("-")
				.append(String.format("%02d", day));
		return UTCTimeBuffer.toString();

	}

	/**
	 * Description:获取明天日期对应的utc日期
	 * 
	 * @param localTimeStr
	 * @return
	 */
	public static String getUTCTomorrowDateFromLocalDate() {
		StringBuffer UTCTimeBuffer = new StringBuffer();
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH) + 1;
		UTCTimeBuffer.append(year).append("-").append(String.format("%02d", month)).append("-")
				.append(String.format("%02d", day));
		return UTCTimeBuffer.toString();

	}

	/**
	 * Description:将UTC时间转换为北京时间，格式为"yyyy-MM-dd HH:mm"
	 * 
	 * @param localTimeStr
	 * @return
	 */
	public static String getLocalDateTimeFromUTCDateTime(String UTCDateTime) {
		java.util.Date nowDate = null;
		String UTCDate = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		try {
			nowDate = format.parse(UTCDateTime);
			UTCDate = format.format(nowDate.getTime() + TimeZone.getDefault().getRawOffset());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return UTCDate;

	}
}
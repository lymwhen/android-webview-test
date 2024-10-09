package com.chunshu.jdsjwt.utils;

import com.alibaba.fastjson.JSON;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Describe: String工具类
 */
public class StringUtils {

	public static String getDateStr(Date date, String format) {
		SimpleDateFormat sdf1 = new SimpleDateFormat(format);
		try {
			return sdf1.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isTimeBetween(Date date, Date startTime, Date endTime){
		Date dateBase = inBaseDate(date);
		Date startTimeBase = inBaseDate(startTime);
		Date endTimeBase = inBaseDate(endTime);
		return dateBase.getTime() >= startTime.getTime() && dateBase.getTime() <= endTime.getTime();
	}

	public static Date inBaseDate(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.YEAR, 2000);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();
	}

	public static boolean isEmpty(String str){
		return str == null || str.length() == 0 || str.equals("null");
	}

	public static boolean isNotEmpty(String str){
		return !isEmpty(str);
	}

	public static String urlEncode(String str){
		try{
			str = URLEncoder.encode(str, "UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}

	public static String toJSONString(Object o){
		return JSON.toJSONString(o);
	}
}

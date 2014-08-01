package com.boredream.dbhelper.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.boredream.dbhelper.DBConstants;

public class DateUtils {
	private static final SimpleDateFormat formater = new SimpleDateFormat(
			DBConstants.DATE_FORMAT, Locale.CHINA);

	public static Date String2Date(String str) {
		Date date = null;
		if (str != null) {
			try {
				date = formater.parse(str);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}

	public static String Date2String(Date date) {
		return formater.format(date);
	}
	
	public static String Date2String(Object obj) {
		return formater.format(obj);
	}
}

package com.boredream.dbhelper;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HelperUtils {
	static final String template = "yyyy-MM-dd HH:mm:ss";
	static final SimpleDateFormat formater = new SimpleDateFormat(
			template, Locale.CHINA);

	static Date String2Date(String str) {
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

	static String Date2String(Date date) {
		return formater.format(date);
	}
	
	static String Date2String(Object obj) {
		return formater.format(obj);
	}
	
	/**
	 * 成员变量是否为可以存至数据库的类型
	 * 
	 * <p>支持八大基础类型,日期类型,String型
	 * @param field
	 * @return
	 */
	static boolean isDBableType(Field field){
		boolean isDBableType = false;
		Class<?> clazz = field.getType();
		// 注意:isPrimitive返回true的条件是八大基础类型 boolean, byte, char, short, 
		// int, long, float, double 加上 void.
		// 这里排除void类型
		if (clazz.equals(void.class) || clazz.equals(Void.class)) {
			isDBableType = false;
		} else {
			isDBableType = clazz.isPrimitive() 
					// String不属于八大基础类型
					|| clazz.equals(String.class)
					|| clazz.equals(java.util.Date.class) 
					|| clazz.equals(java.sql.Date.class);
		}
		return isDBableType;
	}
}

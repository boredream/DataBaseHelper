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
	 * ��Ա�����Ƿ�Ϊ���Դ������ݿ������
	 * 
	 * <p>֧�ְ˴��������,��������,String��
	 * @param field
	 * @return
	 */
	static boolean isDBableType(Field field){
		boolean isDBableType = false;
		Class<?> clazz = field.getType();
		// ע��:isPrimitive����true�������ǰ˴�������� boolean, byte, char, short, 
		// int, long, float, double ���� void.
		// �����ų�void����
		if (clazz.equals(void.class) || clazz.equals(Void.class)) {
			isDBableType = false;
		} else {
			isDBableType = clazz.isPrimitive() 
					// String�����ڰ˴��������
					|| clazz.equals(String.class)
					|| clazz.equals(java.util.Date.class) 
					|| clazz.equals(java.sql.Date.class);
		}
		return isDBableType;
	}
}

package com.boredream.dbhelper;

public class ClassUtils {

	public static String parseDBPriType(Class<?> clazzType) {
		String type = null;
		if (clazzType == int.class || clazzType == Integer.class
				|| clazzType == long.class || clazzType == Long.class) {
			type = "INTEGER";
		} else if (clazzType == float.class || clazzType == Float.class
				|| clazzType == double.class || clazzType == Double.class) {
			type = "REAL";
		} else if (clazzType == char.class || clazzType == Character.class
				|| clazzType == String.class) {
			type = "TEXT";
		} else if (clazzType == boolean.class || clazzType == Boolean.class) {
			type = "TEXT";
		} else if( clazzType == java.util.Date.class 
				|| clazzType == java.sql.Date.class ) {
			type = "TEXT";
		} else if(clazzType == BaseData.class) {
//			Íâ¼ü
		}
		return type;
	}
}

package com.boredream.dbhelper.utils;

import java.lang.reflect.Field;

import com.boredream.dbhelper.Id;

public class FieldUtils {
	
	/**
	 * 获取主键属性的变量(带有Id的注释)
	 * 
	 * @return null-无主键属性变量
	 */
	public static <T> Field getKeyField(Class<T> clazz) {
		Field keyField = null;
		for(Field field : clazz.getFields()) {
			if(field.getAnnotation(Id.class) != null) {
				keyField = field;
				break;
			}
		}
		return keyField;
	}

	/**
	 * 成员变量是否为可以存至数据库的类型
	 * 
	 * <p>支持八大基础类型,日期类型,String型
	 * @param field
	 * @return
	 */
	public static boolean isDBableType(Field field){
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

	/**
	 * 将八大基础类型以及String和Date类型转为数据库对应参数类型(boolean和date都作为字符类型处理)
	 * @param clazzType 数据类型class
	 * @return 转换好后的数据库类型名称
	 */
	public static String parsePri2DBType(Class<?> clazzType) {
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
		} else {
			
		}
		return type;
	}
}

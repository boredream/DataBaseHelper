package com.boredream.dbhelper.utils;

import java.lang.reflect.Field;

import com.boredream.dbhelper.Id;

public class FieldUtils {
	
	/**
	 * ��ȡ�������Եı���(����Id��ע��)
	 * 
	 * @return null-���������Ա���
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
	 * ��Ա�����Ƿ�Ϊ���Դ������ݿ������
	 * 
	 * <p>֧�ְ˴��������,��������,String��
	 * @param field
	 * @return
	 */
	public static boolean isDBableType(Field field){
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

	/**
	 * ���˴���������Լ�String��Date����תΪ���ݿ��Ӧ��������(boolean��date����Ϊ�ַ����ʹ���)
	 * @param clazzType ��������class
	 * @return ת���ú�����ݿ���������
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

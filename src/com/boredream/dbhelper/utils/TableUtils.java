package com.boredream.dbhelper.utils;

import java.lang.reflect.Field;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.boredream.dbhelper.DBConstants;
import com.boredream.dbhelper.Id;

public class TableUtils {
	/**
	 * ��ʼ�����ݿ��еı�,��û�����Ӧ�ı�ʱ�½�һ��
	 * @param <T>
	 */
	public static <T> void initTables(SQLiteDatabase db, Class<T> clazz) {
		Cursor cursor = null;
		try {
			boolean isTableExit;
			cursor = db.rawQuery(
					"SELECT * FROM sqlite_master WHERE TYPE = ? AND name = ?",
					new String[] { "table", clazz.getSimpleName() });
			isTableExit = cursor.moveToFirst();
			if (!isTableExit) {
				createTable(db, clazz);
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
	}

	public static <T> void createTable(SQLiteDatabase db, Class<T> clazz) {
		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(clazz.getSimpleName() + "(");

		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(FieldUtils.isDBableType(field)) {
				String type = FieldUtils.parsePri2DBType(field.getType());
				String fieldName = field.getName();
				
				// ��������Idע��,����Ϊ����
				if(field.getAnnotation(Id.class) != null) {
					sb.append(fieldName + " INTEGER PRIMARY KEY");
					// �����int����long,�������������
					if (field.getType() == int.class || field.getType() == Integer.class || 
						field.getType() == long.class || field.getType() == Long.class) {
						sb.append(" AUTOINCREMENT");
					}
					if(i < fields.length - 1) {
						sb.append(", ");
					}
					continue;
				}
				sb.append(fieldName + " " + type);
				if(i < fields.length - 1) {
					sb.append(", ");
				}
			}
		}
		sb.append(");");
		Log.i(DBConstants.TAG, "first create table, sql = \n" + sb.toString());
		db.execSQL(sb.toString());
	}
}

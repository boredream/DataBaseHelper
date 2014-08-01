package com.boredream.dbhelper;

import java.lang.reflect.Field;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class TableUtils {
	/**
	 * 初始化数据库中的表,若没有类对应的表时新建一个
	 * @param <T>
	 */
	static <T> void initTables(SQLiteDatabase db, Class<T> clazz) {
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

	static <T> void createTable(SQLiteDatabase db, Class<T> clazz) {
		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(clazz.getSimpleName() + "(");

		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(HelperUtils.isDBableType(field)) {
				String type = ClassUtils.parseDBPriType(field.getType());
				String fieldName = field.getName();
				
				// 如果标记了Id注释,或者参数名为"_id"则将作为主键
				if(field.getAnnotation(Id.class) != null || fieldName.equals(BaseColumns._ID)) {
					sb.append(fieldName + " INTEGER PRIMARY KEY");
					// 如果是int或者long,则添加自增属性
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
	
	static <T> void createTable2(SQLiteDatabase db, Class<T> clazz) {
		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(clazz.getSimpleName() + "(");
		sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT");

		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(HelperUtils.isDBableType(field)) {
				String type = ClassUtils.parseDBPriType(field.getType());
				String fieldName = field.getName();

				if(fieldName.equals(BaseColumns._ID)) {
					continue;
				}
				sb.append(", " + fieldName + " " + type);
			}
		}
		sb.append(");");
		Log.i(DBConstants.TAG, "first create table, sql = \n" + sb.toString());
		db.execSQL(sb.toString());
	}
}

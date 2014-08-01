package com.boredream.dbhelper.utils;

import java.lang.reflect.Field;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.boredream.dbhelper.DBConstants;
import com.boredream.dbhelper.Id;

public class TableUtils {
	/**
	 * 初始化数据库中的表,若没有类对应的表时新建一个
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
				
				// 如果标记了Id注释,则将作为主键
				if(field.getAnnotation(Id.class) != null) {
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
}

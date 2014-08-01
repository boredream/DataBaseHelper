package com.boredream.dbhelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.boredream.dbhelper.exception.NotFindFieldException;
import com.boredream.dbhelper.exception.NotFindKeyFieldException;
import com.boredream.dbhelper.exception.QueryValueNullException;
import com.boredream.dbhelper.utils.DateUtils;
import com.boredream.dbhelper.utils.FieldUtils;
import com.boredream.dbhelper.utils.TableUtils;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "meowmomentData";// 数据库的名字
	private static final int DATABASE_VERSION = 1;// 数据库的版本

	private static DBHelper instance;
	private SQLiteDatabase db;

	public static synchronized DBHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DBHelper(context);
		}
		return instance;
	}

	private DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if (db == null || !db.isOpen()) {
			db = getWritableDatabase();
		}
		Log.i(DBConstants.TAG, "DatabaseHelper construct");
	}

	/**
	 * 该函数是在第一次创建数据库的时候执行，实际上是第一次得到SQLiteDatabase对象的时候才会调用这个方法
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i(DBConstants.TAG, "onCreaDB)");
	}

	/**
	 * 当传入的数据库版本version发生变化时，就会调用这个方法--该方法主要用于开发后期要对数据库升级优化时，才会用到
	 * 在这里可以对表进行修改，例如：增减字段，修改字段，更改字段的属性等等。。。
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i(DBConstants.TAG, "方法onUpgrade() oldVersion = " + oldVersion
				+ "/ newVersion = " + newVersion);
		/*
		 * db.execSQL("drop table sms");//如果需要删除此表而重新建立此表时，但是这样会丢失以前的数据---慎用
		 * onCreate(db);//重新建表
		 */
		// 在这里可以对表进行修改，例如：增减字段，修改字段，更改字段的属性等等。。。
		db.execSQL("alter table sms add count integer");// 给表再加一列
	}



	/**
	 * 添加数据
	 * @param <T>
	 * 
	 * @param data
	 * @return true-插入成功
	 * @throws Exception
	 */
	public <T> boolean save(T data) {
		TableUtils.initTables(db, data.getClass());

		Class<?> clazz = data.getClass();
		ContentValues values = new ContentValues();

		Field[] fields = clazz.getDeclaredFields();
		try {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if(!FieldUtils.isDBableType(field)) {
					continue;
				}
				
				field.setAccessible(true);
				if (field.getAnnotation(Id.class) != null || field.get(data) == null) {
					continue;
				}
				if(field.getType() == java.util.Date.class
						|| field.getType() == java.sql.Date.class) {
					values.put(field.getName(), DateUtils.Date2String(field.get(data)));
				} else {
					values.put(field.getName(), field.get(data).toString());
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		long rowId = db.insert(clazz.getSimpleName(), null, values);
		return rowId != -1;
	}

	/**
	 * 批量保存
	 * @param <T>
	 * 
	 * @param dataList
	 * @return 保存失败的数据列表
	 */
	public <T> List<T> saveAll(List<T> dataList) {
		List<T> failAddDataList = new ArrayList<T>();
		try {
			// 批量保存采用事务处理,提高效率
			db.beginTransaction();
			T data;
			boolean isAddSuccess;
			for (int i = 0; i < dataList.size(); i++) {
				data = dataList.get(i);
				isAddSuccess = save(data);
				if (isAddSuccess) {
					failAddDataList.add(data);
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return failAddDataList;
	}

	/**
	 * 删除指定数据,实质还是利用对象_id删除
	 * @param <T>
	 * 
	 * @param data
	 *            需要删除的数据
	 * @return true-删除成功
	 * @throws NotFindKeyFieldException 
	 * 			该类中未找到主键参数
	 */
	public <T> boolean deleteData(T data) throws NotFindKeyFieldException {
		boolean success = false;
		Class<?> clazz = data.getClass();
		Field keyField = FieldUtils.getKeyField(data.getClass());
		if(keyField == null) {
			throw new NotFindKeyFieldException();
		}
		try {
			keyField.setAccessible(true);
			success = deleteDataById(clazz, String.valueOf(keyField.get(data)));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 利用_id删除某条数据
	 * 
	 * @param clazz
	 *            需要删除的数据类型
	 * @param delId
	 *            目标数据的id值
	 *            
	 * @return true-删除成功
	 * 
	 * @throws NotFindKeyFieldException 
	 * 				该类中未找到主键参数
	 * @throws NotFindFieldException
	 *             该类中未找到对应参数
	 * @throws QueryValueNullException
	 *             查询值为空
	 */
	public boolean deleteDataById(Class<?> clazz, String delId) throws NotFindKeyFieldException {
		boolean isSuccess = false;
		Field keyField = FieldUtils.getKeyField(clazz);
		if(keyField == null) {
			throw new NotFindKeyFieldException();
		}
		try {
			isSuccess = deleteDataByField(clazz, keyField.getName(), delId) > 0;
		} catch (NotFindFieldException e) {
			e.printStackTrace();
		} catch (QueryValueNullException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	/**
	 * 利用参数删除数据,可能多条
	 * 
	 * @param clazz
	 *            需要删除的数据类型
	 * @param fieldName
	 *            索引的参数名
	 * @param value
	 *            需要搜索参数对应的值
	 * @return 0-删除失败; >0删除成功的条数
	 * @throws NotFindFieldException
	 *             该类中未找到对应参数
	 * @throws QueryValueNullException
	 *             查询值为空
	 */
	public int deleteDataByField(Class<?> clazz,
			String fieldName, String value) throws NotFindFieldException,
			QueryValueNullException {
		TableUtils.initTables(db, clazz);
		
		List<String> fieldNames = new ArrayList<String>();
		for (Field field : clazz.getDeclaredFields()) {
			fieldNames.add(field.getName());
		}
		if (!fieldNames.contains(fieldName)) {
			throw new NotFindFieldException();
		}
		if (TextUtils.isEmpty(value)) {
			throw new QueryValueNullException();
		}

		int flag;
		// 参数定位数据有可能出现多个数据,因而需要使用事务处理
		try {
			// 批量保存采用事务处理,提高效率
			db.beginTransaction();
			flag = db.delete(clazz.getSimpleName(), fieldName + " = ?",
					new String[] { String.valueOf(value) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return flag;
	}

	/**
	 * 更新数据
	 * @param <T>
	 * 
	 * @param data
	 *            更新的数据对象
	 * @return true-更新成功
	 * @throws NotFindKeyFieldException
	 *             该类中未找到主键参数
	 */
	public <T> boolean updateData(T data) throws NotFindKeyFieldException {
		boolean success = false;
		Field keyField = FieldUtils.getKeyField(data.getClass());
		if(keyField == null) {
			throw new NotFindKeyFieldException();
		}
		Class<?> clazz = data.getClass();
		ContentValues values = new ContentValues();
		try {
			for (Field field : clazz.getDeclaredFields()) {
				field.setAccessible(true);
				values.put(field.getName(), field.get(data).toString());
			}
			int id = db.update(data.getClass().getSimpleName(), values,
					keyField.getName() + " = ?",
					new String[] { String.valueOf(keyField.get(data)) });
			success = id > 0;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 利用_id查询某条数据
	 * @param <T>
	 * 
	 * @param clazz
	 *            需要查询的类型
	 * @param queryId
	 *            需要查询的id
	 *            
	 * @return 查询到的数据,查不到时返回null
	 * 
	 * @throws NotFindKeyFieldException 
	 * 				该类中未找到主键参数
	 */
	public <T> T queryById(Class<T> clazz, String queryId) throws NotFindKeyFieldException {
		List<T> datas;
		T data = null;
		
		Field keyField = FieldUtils.getKeyField(clazz);
		if(keyField == null) {
			throw new NotFindKeyFieldException();
		}
			
		datas = queryByField(clazz, keyField.getName(), queryId);
		
		if(datas != null && datas.size() > 0) {
			data = datas.get(0);
		}
		return data;
	}

	/**
	 * 利用参数查询数据,可能是多条
	 * @param <T>
	 * 
	 * @param clazz
	 *            需要查询的类型
	 * @param data
	 *            需要查询的id
	 * @return 查询到的数据,查不到时返回null
	 */
	public <T> List<T> queryByField(Class<T> clazz,
			String fieldName, String value) {
		TableUtils.initTables(db, clazz);
		
		List<T> dataList = null;
		T data = null;
		Cursor cursor = null;

		try {
			cursor = db.query(
					clazz.getSimpleName(), 
					null,
					TextUtils.isEmpty(fieldName) ? null : fieldName + " = ?",
					TextUtils.isEmpty(fieldName) ? null : new String[] { value },
					null, null, null);
			if (cursor.moveToFirst()) {
				dataList = new ArrayList<T>();
				do {
					data = clazz.newInstance();
					for (Field field : clazz.getDeclaredFields()) {
						field.setAccessible(true);
						setDBData2Bean(data, cursor, field);
					}
					dataList.add(data);
				} while (cursor.moveToNext());
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
		return dataList;
	}

	public <T> List<T> queryAll(Class<T> clazz) {
		return queryByField(clazz, null, null);
	}
	
	/**
	 * 将数据库查询到的数据设置到指定对象参数上
	 * <p>
	 * 自动判断数据类型并赋值
	 * @param <T>
	 * 
	 * @param data
	 *            需要设置值的数据
	 * @param cursor
	 * @param field
	 *            需要赋值对象的参数
	 * @throws IllegalAccessException
	 */
	private <T> void setDBData2Bean(T data, Cursor cursor, Field field)
			throws IllegalAccessException {
		field.setAccessible(true);
		int columnIndex = cursor.getColumnIndex(field.getName());
		Class<?> clazzType = field.getType();
		if (clazzType == int.class || clazzType == Integer.class) {
			field.set(data, cursor.getInt(columnIndex));
		} else if (clazzType == long.class
				|| clazzType == Long.class) {
			field.set(data, cursor.getLong(columnIndex));
		} else if (clazzType == float.class
				|| clazzType == Float.class) {
			field.set(data, cursor.getFloat(columnIndex));
		} else if (clazzType == double.class
				|| clazzType == Double.class) {
			field.set(data, cursor.getDouble(columnIndex));
		} else if (clazzType == char.class
				|| clazzType == Character.class
				|| clazzType == String.class) {
			field.set(data, cursor.getString(columnIndex));
		} else if (clazzType == boolean.class
				|| clazzType == Boolean.class) {
			field.set(data, Boolean.parseBoolean(cursor
					.getString(columnIndex)));
		} else if(clazzType == java.util.Date.class 
				|| clazzType == java.sql.Date.class) {
			field.set(data, DateUtils.String2Date(
					cursor.getString(columnIndex)));
		}
	}

}

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
	private static final String DATABASE_NAME = "meowmomentData";// ���ݿ������
	private static final int DATABASE_VERSION = 1;// ���ݿ�İ汾

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
	 * �ú������ڵ�һ�δ������ݿ��ʱ��ִ�У�ʵ�����ǵ�һ�εõ�SQLiteDatabase�����ʱ��Ż�����������
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i(DBConstants.TAG, "onCreaDB)");
	}

	/**
	 * ����������ݿ�汾version�����仯ʱ���ͻ�����������--�÷�����Ҫ���ڿ�������Ҫ�����ݿ������Ż�ʱ���Ż��õ�
	 * ��������ԶԱ�����޸ģ����磺�����ֶΣ��޸��ֶΣ������ֶε����Եȵȡ�����
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i(DBConstants.TAG, "����onUpgrade() oldVersion = " + oldVersion
				+ "/ newVersion = " + newVersion);
		/*
		 * db.execSQL("drop table sms");//�����Ҫɾ���˱�����½����˱�ʱ�����������ᶪʧ��ǰ������---����
		 * onCreate(db);//���½���
		 */
		// ��������ԶԱ�����޸ģ����磺�����ֶΣ��޸��ֶΣ������ֶε����Եȵȡ�����
		db.execSQL("alter table sms add count integer");// �����ټ�һ��
	}



	/**
	 * �������
	 * @param <T>
	 * 
	 * @param data
	 * @return true-����ɹ�
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
	 * ��������
	 * @param <T>
	 * 
	 * @param dataList
	 * @return ����ʧ�ܵ������б�
	 */
	public <T> List<T> saveAll(List<T> dataList) {
		List<T> failAddDataList = new ArrayList<T>();
		try {
			// �����������������,���Ч��
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
	 * ɾ��ָ������,ʵ�ʻ������ö���_idɾ��
	 * @param <T>
	 * 
	 * @param data
	 *            ��Ҫɾ��������
	 * @return true-ɾ���ɹ�
	 * @throws NotFindKeyFieldException 
	 * 			������δ�ҵ���������
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
	 * ����_idɾ��ĳ������
	 * 
	 * @param clazz
	 *            ��Ҫɾ������������
	 * @param delId
	 *            Ŀ�����ݵ�idֵ
	 *            
	 * @return true-ɾ���ɹ�
	 * 
	 * @throws NotFindKeyFieldException 
	 * 				������δ�ҵ���������
	 * @throws NotFindFieldException
	 *             ������δ�ҵ���Ӧ����
	 * @throws QueryValueNullException
	 *             ��ѯֵΪ��
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
	 * ���ò���ɾ������,���ܶ���
	 * 
	 * @param clazz
	 *            ��Ҫɾ������������
	 * @param fieldName
	 *            �����Ĳ�����
	 * @param value
	 *            ��Ҫ����������Ӧ��ֵ
	 * @return 0-ɾ��ʧ��; >0ɾ���ɹ�������
	 * @throws NotFindFieldException
	 *             ������δ�ҵ���Ӧ����
	 * @throws QueryValueNullException
	 *             ��ѯֵΪ��
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
		// ������λ�����п��ܳ��ֶ������,�����Ҫʹ��������
		try {
			// �����������������,���Ч��
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
	 * ��������
	 * @param <T>
	 * 
	 * @param data
	 *            ���µ����ݶ���
	 * @return true-���³ɹ�
	 * @throws NotFindKeyFieldException
	 *             ������δ�ҵ���������
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
	 * ����_id��ѯĳ������
	 * @param <T>
	 * 
	 * @param clazz
	 *            ��Ҫ��ѯ������
	 * @param queryId
	 *            ��Ҫ��ѯ��id
	 *            
	 * @return ��ѯ��������,�鲻��ʱ����null
	 * 
	 * @throws NotFindKeyFieldException 
	 * 				������δ�ҵ���������
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
	 * ���ò�����ѯ����,�����Ƕ���
	 * @param <T>
	 * 
	 * @param clazz
	 *            ��Ҫ��ѯ������
	 * @param data
	 *            ��Ҫ��ѯ��id
	 * @return ��ѯ��������,�鲻��ʱ����null
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
	 * �����ݿ��ѯ�����������õ�ָ�����������
	 * <p>
	 * �Զ��ж��������Ͳ���ֵ
	 * @param <T>
	 * 
	 * @param data
	 *            ��Ҫ����ֵ������
	 * @param cursor
	 * @param field
	 *            ��Ҫ��ֵ����Ĳ���
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

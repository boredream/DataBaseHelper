package com.boredream.dbhelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.boredream.dbhelper.exception.NotFindFieldException;
import com.boredream.dbhelper.exception.NullIDException;
import com.boredream.dbhelper.exception.QueryValueNullException;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = "DBHelper";
	
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
		Log.i(TAG, "DatabaseHelper construct");
	}

	/**
	 * �ú������ڵ�һ�δ������ݿ��ʱ��ִ�У�ʵ�����ǵ�һ�εõ�SQLiteDatabase�����ʱ��Ż�����������
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreate()");
	}

	public void createTable(Class<? extends BaseData> clazz) {
		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(clazz.getSimpleName() + "(");
		sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT");

		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(HelperUtils.isDBableType(field)) {
				String type = parseDBPriType(field.getType());
				String fieldName = field.getName();
				
				if(fieldName.equals(BaseColumns._ID)) {
					continue;
				}
				sb.append(", " + fieldName + " " + type);
			}
		}
		sb.append(");");
		Log.i(TAG, "first create table, sql = \n" + sb.toString());
		db.execSQL(sb.toString());
	}

	/**
	 * ����������ݿ�汾version�����仯ʱ���ͻ�����������--�÷�����Ҫ���ڿ�������Ҫ�����ݿ������Ż�ʱ���Ż��õ�
	 * ��������ԶԱ�����޸ģ����磺�����ֶΣ��޸��ֶΣ������ֶε����Եȵȡ�����
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i(TAG, "����onUpgrade() oldVersion = " + oldVersion
				+ "/ newVersion = " + newVersion);
		/*
		 * db.execSQL("drop table sms");//�����Ҫɾ���˱�����½����˱�ʱ�����������ᶪʧ��ǰ������---����
		 * onCreate(db);//���½���
		 */
		// ��������ԶԱ�����޸ģ����磺�����ֶΣ��޸��ֶΣ������ֶε����Եȵȡ�����
		db.execSQL("alter table sms add count integer");// �����ټ�һ��
	}

	/**
	 * ��ʼ�����ݿ��еı�,��û�����Ӧ�ı�ʱ�½�һ��
	 */
	private void initTables(Class<? extends BaseData> clazz) {
		Cursor cursor = null;
		try {
			boolean isTableExit;
			cursor = db.rawQuery(
					"SELECT * FROM sqlite_master WHERE TYPE = ? AND name = ?",
					new String[] { "table", clazz.getSimpleName() });
			isTableExit = cursor.moveToFirst();
			if (!isTableExit) {
				createTable(clazz);
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * �������
	 * 
	 * @param data
	 * @return true-����ɹ�
	 * @throws Exception
	 */
	public boolean addData(BaseData data) {
		initTables(data.getClass());

		Class<? extends BaseData> clazz = data.getClass();
		ContentValues values = new ContentValues();

		Field[] fields = clazz.getDeclaredFields();
		try {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if(!HelperUtils.isDBableType(field)) {
					continue;
				}
				
				field.setAccessible(true);
				if (field.getName().equals(BaseColumns._ID) || field.get(data) == null) {
					continue;
				}
				if(field.getType() == java.util.Date.class
						|| field.getType() == java.sql.Date.class) {
					values.put(field.getName(), HelperUtils.Date2String(field.get(data)));
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
	 * 
	 * @param dataList
	 * @return ����ʧ�ܵ������б�
	 */
	public List<BaseData> addDataList(List<BaseData> dataList) {
		List<BaseData> failAddDataList = new ArrayList<BaseData>();
		try {
			// �����������������,���Ч��
			db.beginTransaction();
			BaseData data;
			boolean isAddSuccess;
			for (int i = 0; i < dataList.size(); i++) {
				data = dataList.get(i);
				isAddSuccess = addData(data);
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
	 * 
	 * @param data
	 *            ��Ҫɾ��������
	 * @return true-ɾ���ɹ�
	 */
	public boolean deleteData(BaseData data) {
		Class<? extends BaseData> clazz = data.getClass();
		return deleteDataById(clazz, data._id);
	}

	/**
	 * ����_idɾ��ĳ������
	 * 
	 * @param clazz
	 *            ��Ҫɾ������������
	 * @param delId
	 *            Ŀ�����ݵ�idֵ
	 * @return true-ɾ���ɹ�
	 * @throws NotFindFieldException
	 *             ������δ�ҵ���Ӧ�����쳣
	 * @throws QueryValueNullException
	 *             ��ѯֵΪ���쳣
	 */
	public boolean deleteDataById(Class<? extends BaseData> clazz, long delId) {
		boolean isSuccess;
		try {
			isSuccess = deleteDataByField(clazz, BaseColumns._ID, String.valueOf(delId)) == 1;
		} catch (NotFindFieldException e) {
			e.printStackTrace();
			isSuccess = false;
		} catch (QueryValueNullException e) {
			e.printStackTrace();
			isSuccess = false;
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
	 *             ������δ�ҵ���Ӧ�����쳣
	 * @throws QueryValueNullException
	 *             ��ѯֵΪ���쳣
	 */
	public int deleteDataByField(Class<? extends BaseData> clazz,
			String fieldName, String value) throws NotFindFieldException,
			QueryValueNullException {
		initTables(clazz);
		
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
	 * 
	 * @param data
	 *            ���µ����ݶ���
	 * @return true-���³ɹ�
	 * @throws NullIDException
	 *             �������ݶ���idΪ��(�޷���λ�����ݿ�����,����ʧ��)
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public boolean updateData(BaseData data) throws NullIDException,
			IllegalArgumentException, IllegalAccessException {
		if (data._id == 0) {
			throw new NullIDException();
		}
		Class<? extends BaseData> clazz = data.getClass();
		ContentValues values = new ContentValues();
		for (Field field : clazz.getDeclaredFields()) {
			values.put(field.getName(), field.get(data).toString());
		}
		int id = db.update(data.getClass().getSimpleName(), values,
				BaseColumns._ID + " = ?",
				new String[] { String.valueOf(data._id) });
		return id <= 0;
	}

	/**
	 * ����_id��ѯĳ������
	 * 
	 * @param clazz
	 *            ��Ҫ��ѯ������
	 * @param queryId
	 *            ��Ҫ��ѯ��id
	 * @return ��ѯ��������,�鲻��ʱ����null
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	public BaseData queryDataById(Class<? extends BaseData> clazz, long queryId)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {
		List<BaseData> dataList = queryDataByField(clazz, BaseColumns._ID,
				String.valueOf(queryId));
		return dataList == null ? null : dataList.get(0);
	}

	/**
	 * ���ò�����ѯ����,�����Ƕ���
	 * 
	 * @param clazz
	 *            ��Ҫ��ѯ������
	 * @param data
	 *            ��Ҫ��ѯ��id
	 * @return ��ѯ��������,�鲻��ʱ����null
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	public List<BaseData> queryDataByField(Class<? extends BaseData> clazz,
			String fieldName, String value)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {
		initTables(clazz);
		
		List<BaseData> dataList = null;
		BaseData data = null;
		Cursor cursor = null;

		try {
			cursor = db.query(
					clazz.getSimpleName(), 
					null,
					TextUtils.isEmpty(fieldName) ? null : fieldName + " = ?",
					TextUtils.isEmpty(fieldName) ? null : new String[] { value },
					null, null, null);
			if (cursor.moveToFirst()) {
				dataList = new ArrayList<BaseData>();
				do {
					data = clazz.newInstance();
					for (Field field : clazz.getDeclaredFields()) {
						setDBData2Bean(data, cursor, field);
					}
					dataList.add(data);
				} while (cursor.moveToNext());
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
		return dataList;
	}

	public List<? extends BaseData> queryAllData(Class<? extends BaseData> clazz)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {
		return queryDataByField(clazz, null, null);
	}

	private String parseDBPriType(Class<?> clazzType) {
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
//			���
		}
		return type;
	}
	
	/**
	 * �����ݿ��ѯ�����������õ�ָ�����������
	 * <p>
	 * �Զ��ж��������Ͳ���ֵ
	 * 
	 * @param data
	 *            ��Ҫ����ֵ������
	 * @param cursor
	 * @param field
	 *            ��Ҫ��ֵ����Ĳ���
	 * @throws IllegalAccessException
	 */
	private void setDBData2Bean(BaseData data, Cursor cursor, Field field)
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
			field.set(data, HelperUtils.String2Date(
					cursor.getString(columnIndex)));
		}
	}

}

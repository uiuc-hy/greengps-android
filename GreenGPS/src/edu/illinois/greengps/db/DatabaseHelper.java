package edu.illinois.greengps.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper implements DBConstants{
	
	private static final String TAG = "DatabaseHelper";
	private static boolean isDbOpen = false;
	private static Object dbLock = new Object();
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		 Log.i(TAG, "Creating database " + DATABASE_NAME);
		 db.execSQL("CREATE TABLE " + FS_TABLE + " ("
                 + FS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                 + FS_DATA + " TEXT"
                 + ");");
		 
		 db.execSQL("CREATE TABLE " + STATE_TABLE + " (" 
				 + STATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				 + STATE_NAME + " TEXT,"
				 + STATE_STATUS + " INTEGER"
				 + ");");
	}
	
	public void setupState(String stateName, long valueIfNull) {
		SQLiteDatabase db = openDb();
		// check if the state already exists
		Cursor cursor = db.query(STATE_TABLE, null, STATE_NAME + "='" + stateName + "'", null, null, null, null);
		if (!cursor.moveToFirst()) {
			Log.i(TAG, "Resetting state " + stateName + " to " + valueIfNull);
			ContentValues values = new ContentValues();
			values.put(STATE_NAME, stateName);
			values.put(STATE_STATUS, valueIfNull);
			db.insert(STATE_TABLE, null, values);
		}
		cursor.close();
		closeDb(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + FS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STATE_TABLE);
        onCreate(db);
	}
	
	public long insert(long sampleId, String data) {
		SQLiteDatabase db = openDb();
		ContentValues values = new ContentValues();
		values.put(FS_DATA, data);
		
		long rowId = db.insert(FS_TABLE, null, values);

		closeDb(db);
		return rowId;
	}
	
	public boolean delete(long sampleId) {
		SQLiteDatabase db = openDb();
		if (db == null) {
			closeDb(db);
			return false;
		}

		int count = db.delete(FS_TABLE, FS_ID + "=" + sampleId, null);
		closeDb(db);
		return count > 0;
	}
	
	public boolean deleteRange(long sampleId) {
		SQLiteDatabase db = openDb();
		if (db == null) {
			closeDb(db);
			return false;
		}

		int count = db.delete(FS_TABLE, FS_ID + "<=" + sampleId, null);
		closeDb(db);
		return count > 0;
	}
	
	public String getData(long id) {
		SQLiteDatabase db = openDb();
		Cursor cursor = db.query(FS_TABLE, null, FS_ID + "=" + id, null, null, null, null);

		String data = null;
		if (cursor.moveToFirst()) {
			data = cursor.getString(cursor.getColumnIndex(FS_DATA));
		}
		
		cursor.close();
		closeDb(db);
		return data;
	}
	
	public Entry<Long, String> tuple(Long key, String value) {
	    Map<Long, String> map = new HashMap<Long, String>();
	    map.put(key, value);
	    return map.entrySet().iterator().next();
	}
	
	/**
	 * Return numData rows with the greatest IDs (most recently inserted)
	 * @param numData
	 * @return
	 */
	public ArrayList <Entry<Long, String>> getMostRecentData(int numData) {
		ArrayList <Entry<Long, String>> data = new ArrayList <Entry<Long, String>>();
		SQLiteDatabase db = openDb();
		
		Cursor cursor = db.rawQuery("SELECT * FROM " + FS_TABLE 
				+ " ORDER BY " + FS_ID + " DESC "
				+ " LIMIT " + numData, null);
		
		cursor.moveToFirst();
		for (int i=0; i<cursor.getCount(); i++) {
			data.add(tuple(cursor.getLong(cursor.getColumnIndex(FS_ID)) ,
					cursor.getString(cursor.getColumnIndex(FS_DATA))));
			cursor.moveToNext();
		}
		
		cursor.close();
		closeDb(db);
		return data;
	}
	
	/**
	 * Return numData rows that have an ID greater than id
	 * @param id
	 * @param numData
	 * @return
	 */
	public ArrayList <Entry<Long, String>> getData(long id, int numData) {
		ArrayList <Entry<Long, String>> data = new ArrayList <Entry<Long, String>>();
		SQLiteDatabase db = openDb();

		Cursor cursor = db.rawQuery("SELECT * FROM " + FS_TABLE 
				+ " WHERE " + FS_ID + ">" + id 
				+ " ORDER BY " + FS_ID
				+ " LIMIT " + numData, null);
		
		cursor.moveToFirst();
		for (int i=0; i<cursor.getCount(); i++) {
			data.add(tuple(cursor.getLong(cursor.getColumnIndex(FS_ID)) ,
					cursor.getString(cursor.getColumnIndex(FS_DATA))));
			cursor.moveToNext();
		}
		
		cursor.close();
		closeDb(db);
		return data;
	}
	
	public long getStateStatus(String stateName) {
		Log.i(TAG, "inside getStateStatus");
		
		SQLiteDatabase db = openDb();
		Cursor cursor = db.query(STATE_TABLE, null, STATE_NAME + "='" + stateName+"'", null, null, null, null);
		
		long status = -2;
		if (cursor.moveToFirst()) {
			status = cursor.getLong(cursor.getColumnIndex(STATE_STATUS));
		}
		
		cursor.close();
		closeDb(db);
		
		Log.i(TAG, "Status returning: " + status);
		return status;
	}
	
	public void updateStateStatus(String stateName, long status) {
		SQLiteDatabase db = openDb();
		db.execSQL("UPDATE " + STATE_TABLE + " SET " + STATE_STATUS + "=" + status
				+ " WHERE " + STATE_NAME + "='" + stateName+"'");
		closeDb(db);
	}
	
	public void clearSamples() {
		SQLiteDatabase db = openDb();
		Log.w(TAG, "Resetting database...erasing samples table");
        db.execSQL("DELETE FROM " + FS_TABLE);
        closeDb(db);
	}
	
	public void resetDb() {
		SQLiteDatabase db = openDb();
		Log.w(TAG, "Resetting database...erasing all old data");
        db.execSQL("DROP TABLE IF EXISTS " + FS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STATE_TABLE);
        onCreate(db);
        closeDb(db);
	}
	
	/**
	 * Borrowed from pdc-android code
	 * @return
	 */
	private SQLiteDatabase openDb() {
		synchronized (dbLock) {
			while (isDbOpen) {
				try {
					dbLock.wait();
				} catch (InterruptedException e) {
				}
			}
			isDbOpen = true;
			try {
				return getWritableDatabase();
			} catch (SQLiteException e) {
				Log.e(TAG, "Error opening database: " + DATABASE_NAME);
				isDbOpen = false;
				return null;
			}
		}
	}
	
	/**
	 * Borrowed from pdc-android code
	 * @param db
	 */
	private void closeDb(SQLiteDatabase db) {
		synchronized (dbLock) {
			if (db != null)
				db.close();
			isDbOpen = false;
			dbLock.notify();
		}
	}
}

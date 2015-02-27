package com.example.photofunpro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ImagesTable {
	//Database Table
	public static final String DATABASE_FUNCAM = "funcam.db";
	public static final String TABLE_IMAGES = "images";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_GPS_LAT = "lat";
	public static final String COLUMN_GPS_LON = "lon";
	public static final String COLUMN_IMG_PATH = "path";
	public static final String COLUMN_IMG_DATE = "date";
	public static final String COLUMN_UPLOADED = "uploaded";
	
	//Logging tag
	private static final String TAG = ImagesTable.class.getSimpleName();
	
	//Database Creation SQL
	public static final String DATABASE_CREATE = "" +
			"CREATE TABLE " + TABLE_IMAGES + " (" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_TITLE + " TEXT NULL, " + 
			COLUMN_GPS_LAT + " TEXT NOT NULL, " + 
			COLUMN_GPS_LON + " TEXT NOT NULL, " + 
			COLUMN_IMG_PATH + " TEXT NOT NULL, " +
			COLUMN_IMG_DATE + " TEXT NOT NULL, " +
			COLUMN_UPLOADED + " TEXT DEFAULT '0');";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		Log.d(TAG, TABLE_IMAGES + " table has been created");
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
		Log.d(TAG, "Upgrading database from ver " + oldVer + " to ver " + newVer);
		database.execSQL("DROP IF EXISTS TABLE " + TABLE_IMAGES);
		onCreate(database);
	}
}
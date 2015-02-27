package com.example.photofunpro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class VideosTable {
	//Database Table
	public static final String DATABASE_FUNCAM = "funcam.db";
	public static final String TABLE_VIDEOS = "videos";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	
	//Logging tag
	private static final String TAG = VideosTable.class.getSimpleName();
	
	//Database Creation SQL
	public static final String DATABASE_CREATE = "" +
			"CREATE TABLE " + TABLE_VIDEOS + " (" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_PATH + " TEXT NOT NULL, " + 
			COLUMN_TITLE + " TEXT NOT NULL, " + 
			COLUMN_DESCRIPTION + " TEXT NOT NULL);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		Log.d(TAG, TABLE_VIDEOS + " table has been created");
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
		Log.d(TAG, "Upgrading database from ver " + oldVer + " to ver " + newVer);
		database.execSQL("DROP IF EXISTS TABLE " + TABLE_VIDEOS);
		onCreate(database);
	}

}

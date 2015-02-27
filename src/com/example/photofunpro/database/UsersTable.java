package com.example.photofunpro.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UsersTable {
	//Database Table
	public static final String DATABASE_FUNCAM = "funcam.db";
	public static final String TABLE_USERS = "users";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_EMAIL = "email";
	public static final String COLUMN_PASSWORD = "password";
	public static final String COLUMN_IMG_PATH = "path";	
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESIGNATION = "designation";
	public static final String COLUMN_BIO = "bio";
	
	//Logging tag
	private static final String TAG = UsersTable.class.getSimpleName();
	
	//Database Creation SQL
	public static final String DATABASE_CREATE = "" +
			"CREATE TABLE " + TABLE_USERS + " (" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_EMAIL + " TEXT NOT NULL, " + 
			COLUMN_PASSWORD + " TEXT NOT NULL, " + 
			COLUMN_IMG_PATH + " TEXT NULL, " +
			COLUMN_NAME + " TEXT NULL, " + 
			COLUMN_DESIGNATION + " TEXT NULL, " + 
			COLUMN_BIO + " TEXT NULL);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		Log.d(TAG, TABLE_USERS + " table has been created");
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
		Log.d(TAG, "Upgrading database from ver " + oldVer + " to ver " + newVer);
		database.execSQL("DROP IF EXISTS TABLE " + TABLE_USERS);
		onCreate(database);
	}
}
package com.example.photofunpro.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotoFunProDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "funcam.db";
	private static final int DATABASE_VERSION = 1;

	public PhotoFunProDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase db) {
		ImagesTable.onCreate(db);
		VideosTable.onCreate(db);
		UsersTable.onCreate(db);
	}

	//called during the upgrade of the database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ImagesTable.onUpgrade(db, oldVersion, newVersion);
		VideosTable.onUpgrade(db, oldVersion, newVersion);
		UsersTable.onUpgrade(db, oldVersion, newVersion);
	}

}
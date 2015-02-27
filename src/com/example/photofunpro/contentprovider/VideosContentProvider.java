package com.example.photofunpro.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.example.photofunpro.database.PhotoFunProDatabaseHelper;
import com.example.photofunpro.database.VideosTable;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class VideosContentProvider extends ContentProvider {

	//the database to be used
	private PhotoFunProDatabaseHelper database;
	
	//used for the URI match
	private static final int VIDEOS = 10;
	private static final int VIDEO_ID = 20;
	private static final String AUTHORITY = "com.example.photofunpro.contentprovider";
	private static final String BASE_PATH = "videos";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/videos";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/video";
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, VIDEOS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", VIDEO_ID);
	}

	@Override
	public boolean onCreate() {
		database = new PhotoFunProDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// Check if the caller has requested a column which does not exists
		checkColumns(projection);
		// Set the table
		queryBuilder.setTables(VideosTable.TABLE_VIDEOS);
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case VIDEOS:
			break;
		case VIDEO_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(VideosTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case VIDEOS:
			id = sqlDB.insert(VideosTable.TABLE_VIDEOS, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case VIDEOS:
			rowsDeleted = sqlDB.delete(VideosTable.TABLE_VIDEOS, selection,
					selectionArgs);
			break;
		case VIDEO_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(VideosTable.TABLE_VIDEOS,
						VideosTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(VideosTable.TABLE_VIDEOS,
						VideosTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case VIDEOS:
			rowsUpdated = sqlDB.update(VideosTable.TABLE_VIDEOS, values, selection,
					selectionArgs);
			break;
		case VIDEO_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(VideosTable.TABLE_VIDEOS, values,
						VideosTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(VideosTable.TABLE_VIDEOS, values,
						VideosTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { VideosTable.COLUMN_TITLE, VideosTable.COLUMN_DESCRIPTION,
				VideosTable.COLUMN_PATH, VideosTable.COLUMN_ID };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
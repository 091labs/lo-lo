package com.codeskraps.lolo.home;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase {
	private static final String TAG = DataBase.class.getSimpleName();

	public static final String DB_TABLE_TWITTER = "twitter";
	public static final String DB_TABLE_TABS = "tabs";

	public static final String C_TWEET_ID = "tweet_id";
	public static final String C_TWEET_CREATED = "tweet_created";
	public static final String C_TWEET_IMAGE = "tweet_image";
	public static final String C_TWEET_NAME = "tweet_name";
	public static final String C_TWEET_SCREEN = "tweet_screen";
	public static final String C_TWEET_TEXT = "tweet_text";

	private Context context;
	private DbHelper dbHelper;

	public DataBase(Context context) {
		this.context = context;
		dbHelper = new DbHelper();
	}

	public void close() {
		dbHelper.close();
	}

	public void insert(String table, TweetItem tweetItem) {

		ContentValues values = new ContentValues();
		values.put(C_TWEET_ID, tweetItem.getId());
		values.put(C_TWEET_CREATED, tweetItem.getCreated());
		values.put(C_TWEET_IMAGE, tweetItem.getImgUser());
		values.put(C_TWEET_NAME, tweetItem.getTxtUser());
		values.put(C_TWEET_SCREEN, tweetItem.getTxtScreenName());
		values.put(C_TWEET_TEXT, tweetItem.getText());

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		db.close();
	}

	public Cursor query(String table) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		return db.query(table, null, null, null, null, null, C_TWEET_CREATED + " DESC", "500");
	}

	public void deleteTable(String table) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(table, null, null);
		db.close();
	}

	private class DbHelper extends SQLiteOpenHelper {
		public static final String DB_NAME = "sBrowserDB.db";
		public static final int DB_VERSION = 2;

		public DbHelper() {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String
					.format("create table %s (%s VARCHAR(18) primary key, %s text, %s text, %s text, %s text, %s text)",
							DB_TABLE_TWITTER, C_TWEET_ID, C_TWEET_CREATED, C_TWEET_IMAGE,
							C_TWEET_NAME, C_TWEET_SCREEN, C_TWEET_TEXT);
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists " + DB_TABLE_TWITTER);
			Log.d(TAG, "onUpdate dropped tables " + DB_TABLE_TWITTER);

			this.onCreate(db);
		}
	}

}

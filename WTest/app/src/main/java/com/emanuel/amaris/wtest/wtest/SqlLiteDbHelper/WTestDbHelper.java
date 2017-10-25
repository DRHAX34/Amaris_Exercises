package com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by emanuel on 25-10-2017.
 */

public class WTestDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WTestCacheDb.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WTestDbContract.WTestDbEntry.TABLE_NAME + " (" +
                    WTestDbContract.WTestDbEntry._ID + " INTEGER PRIMARY KEY," +
                    WTestDbContract.WTestDbEntry.COLUMN_LOCAL + " TEXT COLLATE NOCASE," +
                    WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII + " TEXT COLLATE NOCASE," +
                    WTestDbContract.WTestDbEntry.COLUMN_VALUE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + WTestDbContract.WTestDbEntry.TABLE_NAME;

    public WTestDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so the upgrade policy should be simply to start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Same policy as when we upgrade it
        onUpgrade(db, oldVersion, newVersion);
    }
}

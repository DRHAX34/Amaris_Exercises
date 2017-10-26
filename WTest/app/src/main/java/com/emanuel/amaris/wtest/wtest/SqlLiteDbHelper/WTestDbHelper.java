package com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by emanuel on 25-10-2017.
 */

/**
 * This class is necessary in order to interact with the database and to be able to create it
 * All the method and variable names are self-explanatory
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

    //This method is executed when there is no database created at all
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    //This method is executed if there is a database created, but we have a newer version
    //Since there may have been changes, here is the place to work on them
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so the upgrade policy should be simply to start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    //I see no purpose for this method, since the old app won't know our recent changes on the new app
    //This is only if we need to make a forced downgrade from a failed new app to a stable old app
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Same policy as when we upgrade it
        onUpgrade(db, oldVersion, newVersion);
    }
}

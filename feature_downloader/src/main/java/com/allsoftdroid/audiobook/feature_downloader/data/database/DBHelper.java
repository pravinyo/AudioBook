package com.allsoftdroid.audiobook.feature_downloader.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract.downloadEntry;

public class DBHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME="audiobook_download.db";
    private final static int DATABASE_VERSION=2;

    private static final String SQL_CREATE_ENTERIES_DOWNLOAD="CREATE TABLE "+ downloadEntry.TABLE_NAME+"("+
            downloadEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
            downloadEntry.COLUMN_DOWNLOAD_ID + " TEXT NOT NULL,"+
            downloadEntry.COLUMN_DOWNLOAD_URL + " TEXT NOT NULL,"+
            downloadEntry.COLUMN_DOWNLOAD_NAME + " TEXT NOT NULL)";




    DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTERIES_DOWNLOAD);
    }

    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+ downloadEntry.TABLE_NAME);

        onCreate(db);
    }
}

package com.teinproductions.tein.gameoflife.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DefaultPatternDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "patterns";

    public DefaultPatternDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String entry = "CREATE TABLE " + DefaultPatternContract.DefaultPatternEntry.TABLE_NAME + " (" +
                DefaultPatternContract.DefaultPatternEntry._ID + " INTEGER PRIMARY KEY," +
                DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL + " TEXT NOT NULL," +
                DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_NAME + " TEXT," +
                DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_CONTENT + " TEXT)";
        db.execSQL(entry);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: 31-1-2016 Update table instead of disposing it
        db.execSQL("DROP TABLE IF EXISTS " + DefaultPatternContract.DefaultPatternEntry.TABLE_NAME);
        onCreate(db);
    }
}

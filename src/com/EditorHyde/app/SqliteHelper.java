package com.EditorHyde.app;

/**
 * Created by xrdawson on 8/19/13.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteHelper extends SQLiteOpenHelper {

    public static final String TABLE_SCRATCHES = "scratches";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_CONTENT = "content";

    private static final String DATABASE_NAME = "commments.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_SCRATCHES + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_COMMENT
            + " text not null,"
            + COLUMN_CONTENT
            + " text not null"
            + ");";

    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SqliteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCRATCHES );
        onCreate(db);
    }

}
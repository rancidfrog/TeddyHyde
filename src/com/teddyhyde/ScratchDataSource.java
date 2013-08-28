package com.teddyhyde;

/**
 * Created by xrdawson on 8/19/13.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ScratchDataSource {

    // Database fields
    private SQLiteDatabase database;
    private SqliteHelper dbHelper;
    private String[] allColumns = { SqliteHelper.COLUMN_ID,
            SqliteHelper.COLUMN_CONTENT };

    public ScratchDataSource(Context context) {
        dbHelper = new SqliteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Scratch createScratch(String Scratch) {
        ContentValues values = new ContentValues();
        values.put(SqliteHelper.COLUMN_CONTENT, Scratch);
        long insertId = database.insert(SqliteHelper.TABLE_SCRATCHES, null,
                values);
        Cursor cursor = database.query(SqliteHelper.TABLE_SCRATCHES,
                allColumns, SqliteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Scratch newScratch = cursorToScratch(cursor);
        cursor.close();
        return newScratch;
    }

    public void deleteScratch(Scratch scratch) {
        long id = scratch.getId();
        System.out.println("Scratch deleted with id: " + id);
        database.delete(SqliteHelper.TABLE_SCRATCHES, SqliteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void updateScratch(String id, String scratch ) {
        ContentValues args = new ContentValues();
        args.put(SqliteHelper.COLUMN_CONTENT, scratch);
        System.out.println("Scratch updated with id: " + id);
        database.update(SqliteHelper.TABLE_SCRATCHES, args, SqliteHelper.COLUMN_ID + "=" + id, null);
    }

    public List<Scratch> getAllScratches() {
        List<Scratch> Scratches = new ArrayList<Scratch>();

        Cursor cursor = database.query(SqliteHelper.TABLE_SCRATCHES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Scratch Scratch = cursorToScratch(cursor);
            Scratches.add(Scratch);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return Scratches;
    }

    private Scratch cursorToScratch(Cursor cursor) {
        Scratch Scratch = new Scratch();
        Scratch.setId(cursor.getLong(0));
        Scratch.setScratch(cursor.getString(1));
        return Scratch;
    }
}

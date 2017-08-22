package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.apex.icrf.Const;

public class BaseDbAdapter {
    protected static SQLiteDatabase mDb;

    public void beginTransaction() {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Begin Transaction");
        mDb.beginTransaction();
    }

    public void setTransactionSuccessful() {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Transaction Successful");
        mDb.setTransactionSuccessful();
    }

    public void endTransaction() {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "End Transaction");
        mDb.endTransaction();
    }

    protected void insertRow(String table, ContentValues initialValues) {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Row inserted into table " + table);
        mDb.insert(table, null, initialValues);
    }

    protected int updateRow(String table, ContentValues initialValues,
                            String whereClause) {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Row in table " + table + " updated "
                    + whereClause);
        return mDb.update(table, initialValues, whereClause, null);
    }

    protected void deleteRow(String table, String whereClause, String[] whereArgs) {

        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Row in table " + table + " deleted "
                    + whereClause);
       mDb.delete(table, whereClause, whereArgs);
    }

    public Cursor query(String query) {
        Cursor c = mDb.rawQuery(query, null);
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Query '" + query + "' returned " + c.getCount()
                    + " rows");
        return c;
    }

    protected void clearTable(String table) {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "Table " + table + " cleared");
        mDb.delete(table, null, null);
    }

    protected static void setWritableDatabase(SQLiteDatabase db) {
        mDb = db;
    }

    protected void clearFeedFromTable(String where, String[] whereArgs) {
        if (Const.DEBUGGING_DB)
            Log.d(Const.DEBUG, "clearFeedFromTable: where:" + where);
        mDb.delete(DatabaseHelper.PETITIONS_TABLE, where, null);
    }

}
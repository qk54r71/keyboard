package com.example.user.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.user.myapplication.Common.CommonJava;

/**
 * Created by USER on 2016-07-14.
 */
public class DBManageMent {

    public static final String TEXT_KEY = "text_key";
    public static final String TEXT_CONTENT = "text_content";
    public static final String TEXT_ID = "_id";
    private static final String TAG = "DBManageMent";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */

    private static final String DATABASE_CREATE = "create table notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "text_key TEXT NOT NULL, text_content TEXT);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;
    private final Context mContext;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            CommonJava.Loging.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public DBManageMent(Context context) {
        this.mContext = context;
    }

    public DBManageMent open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createNote(String text_key, String text_content) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(TEXT_KEY, text_key);
        initialValues.put(TEXT_CONTENT, text_content);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteNote(long rowId) {
        CommonJava.Loging.i("Delete called", "value__" + rowId);
        return mDb.delete(DATABASE_TABLE, TEXT_ID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllNotes() {
        return mDb.query(DATABASE_TABLE, new String[]{TEXT_ID, TEXT_KEY, TEXT_CONTENT}, null, null, null, null, null);
    }

    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{TEXT_ID, TEXT_KEY, TEXT_CONTENT}, TEXT_ID
                + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(TEXT_KEY, title);
        args.put(TEXT_CONTENT, body);
        return mDb.update(DATABASE_TABLE, args, TEXT_ID + "=" + rowId, null) > 0;
    }

    public String[] serchKey(String text_key) {

        String sqlSelete = "SELECT text_content FROM " + DATABASE_TABLE + " WHERE text_key='" + text_key + "';";
        String[] resultStringArray = null;
        String resultString = null;
        Cursor cursorSelect = mDb.rawQuery(sqlSelete, null);

        cursorSelect.moveToLast();
        resultString = cursorSelect.getString(0);

        resultStringArray = resultString.split(";");

        cursorSelect.close();

        return resultStringArray;
    }

}

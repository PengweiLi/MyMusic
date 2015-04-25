
package com.lipengwei.music;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * @author author-pengwei.li@tcl.com
 * @version version-v1.0 The ContentProvider is used to store and get User
 *          information from the database
 */
public class MyContentProvider extends ContentProvider {

    public static final String DBNAME = "mydb";// database name
    public static final String TABLENAME = "account";// table name
    public static final String _ID = "_id";// user id
    public static final String UERNAME = "uername";// user name
    public static final String EMAIL = "email";// user email
    public static final String PASSWORD = "password";// user password
    // database authority
    public static final String AUTOHORITY = "com.lipengwei.music.provider";
    public static final Uri CONTENT_URI = Uri.
            parse("content://" + AUTOHORITY + "/account");// database uri

    private MainDatabaseHelper mOpenHelper;
    private SQLiteDatabase mDB;

    /**
     * create database if not exist
     * 
     * @return create success
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MainDatabaseHelper(getContext());
        return true;
    }

    /**
     * insert new row for user account
     * 
     * @param uri-The content:// URI of the insertion request
     * @param values-A set of column_name/value pairs to add to the database
     * @return The URI for the newly inserted item
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        mDB = mOpenHelper.getWritableDatabase();
        // get insert number
        long rowId = mDB.insert(TABLENAME, _ID, values);
        Log.i("MUSIC", "rowId=" + rowId);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        } else {
            return null;
        }
    }

    /**
     * query data from database
     * 
     * @param uri-The URI to query
     * @param projection-The list of columns to put into the cursor
     * @param selection-A selection criteria to apply when filtering rows
     * @param selectionArgs-The values will be bound as Strings
     * @param sortOrder-How the rows in the cursor should be sorted
     * @return a Cursor or null
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        qb.setTables(TABLENAME);
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        return c;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public String getType(Uri arg0) {
        return null;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }

    // use to create table account
    private static final String SQL_CREATE_MAIN = "CREATE TABLE " +
            "account " +
            "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            UERNAME + " TEXT, " +
            PASSWORD + " TEXT, " +
            EMAIL + " TEXT )";

    /**
     * the class define SqliteOpenHelper to create database is not exist
     */
    protected static final class MainDatabaseHelper extends
            SQLiteOpenHelper {

        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_MAIN);
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        }
    }
}

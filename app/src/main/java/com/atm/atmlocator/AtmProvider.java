package com.atm.atmlocator;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Tahmidul on 8/8/2016.
 */

public class AtmProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.atmlocator.Bank";
    static final String URL = "content://" + PROVIDER_NAME + "/atms";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String BANK = "bank";
    static final String ATM_NAME = "atm_name";
    static final String LAT = "lat";
    static final String LONGI = "longi";
    static final String ADDRESS = "address";
    static final String CITY = "city";
    static final String STATE = "state";
    static final String COUNTRY = "country";
    static final String IDS = "ids";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int ATM = 1;
    static final int ATM_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "atms", ATM);
        uriMatcher.addURI(PROVIDER_NAME, "atms/#", ATM_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "Bank";
    static final String BANK_TABLE_NAME = "atms";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + BANK_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " bank TEXT NOT NULL, " +
                    " atm_name TEXT NOT NULL, " +
                    " lat TEXT NOT NULL, " +
                    " longi TEXT NOT NULL, " +
                    " address TEXT NOT NULL, " +
                    " city TEXT NOT NULL, " +
                    " state TEXT NOT NULL, " +
                    " country TEXT NOT NULL, "+
                    " ids TEXT NOT NULL);";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            Log.d("SHAKIL", "yap creating table");
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  BANK_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Log.d("SHAKIL", "yap onCreate of Content Provider");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(	BANK_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         * throw  add a new student record and information that is to be passed to the uppermost often the question remains as like t
         * this like the most one hundred and eighty one and content and also for the most performance based solution)")
         */

        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(BANK_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ATM:
                Log.d("SHAKIL", "4m onQuery with base url which will return all row of the table");
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case ATM_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                Log.d("SHAKIL", "pathSegments = "+ uri.getPathSegments().toString());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == "") {
            /**
             * By default sort on student names
             */
            sortOrder = BANK;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs, null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ATM:
                count = db.delete(BANK_TABLE_NAME, selection, selectionArgs);
                break;

            case ATM_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( BANK_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case ATM:
                count = db.update(BANK_TABLE_NAME, values, selection, selectionArgs);
                break;

            case ATM_ID:
                count = db.update(BANK_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all student records
             */
            case ATM:
                return "vnd.android.cursor.dir/vnd.atmlocator.Bank";

            /**
             * Get a particular student
             */
            case ATM_ID:
                return "vnd.android.cursor.item/vnd.atmlocator.Bank";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}

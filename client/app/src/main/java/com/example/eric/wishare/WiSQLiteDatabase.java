package com.example.eric.wishare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.util.ArrayList;

public class WiSQLiteDatabase extends SQLiteOpenHelper {

    interface OnDBReadyListener {
        void onDBReady(SQLiteDatabase theDB);
    }

    private static final int mDATABASE_VERSION = 1;
    private static final String mDATABASE_NAME = "wishare.db";
    private static WiSQLiteDatabase mDB;


    //Table with variables
    //Common variable types include INT, FLOAT, DATE, TIME, varchar([max characters])    <-- string, BIT <-- boolean with 0 | 1
    private static final String mSQL_CREATE_CONTACTS =
            "CREATE TABLE SynchronizedContacts (" +
                    "phone varchar(255) NOT NULL PRIMARY KEY," +
                    "name varchar(255)," +
                    "token varchar(255))";
    private static final String mSQL_CREATE_CONFIGUREDNETWORKS =
            "CREATE TABLE ConfiguredNetworks (" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "name varchar(255)," +
                    "SSID varchar(255)," +
                    "passwordHash varchar(255))";
    private static final String mSQL_CREATE_PERMITTEDCONTACTS =
            "CREATE TABLE PermittedContacts (" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "name varchar(255)," +
                    "SSID varchar(255)," +
                    "passwordHash varchar(255))";

    //Not sure if this will really be used
    private static final String mSQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS SynchronizedContacts";

    private WiSQLiteDatabase(Context context) {
        super(context.getApplicationContext(),mDATABASE_NAME,null,mDATABASE_VERSION);
    }

    public static synchronized WiSQLiteDatabase getInstance(Context context) {
        if (mDB == null) {
            mDB = new WiSQLiteDatabase(context.getApplicationContext());
        }
        return mDB;
    }

    interface OnContactListReadyListener{
        void onContactListReady(ArrayList<WiContact> contacts);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mSQL_CREATE_CONTACTS);
        db.execSQL(mSQL_CREATE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_CREATE_PERMITTEDCONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(mSQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void getWritableDatabase(OnDBReadyListener listener) {
        new OpenDbAsyncTask().execute(listener);
    }

    private static class OpenDbAsyncTask extends AsyncTask<OnDBReadyListener,Void,SQLiteDatabase> {
        OnDBReadyListener listener;

        @Override
        protected SQLiteDatabase doInBackground(OnDBReadyListener... params){
            listener = params[0];
            return WiSQLiteDatabase.mDB.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db) {
            listener.onDBReady(db);
        }
    }
}
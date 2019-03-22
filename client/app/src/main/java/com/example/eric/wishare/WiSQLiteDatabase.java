package com.example.eric.wishare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.example.eric.wishare.model.WiContact;

import java.util.ArrayList;

public class WiSQLiteDatabase extends SQLiteOpenHelper {

    public interface OnDBReadyListener {
        void onDBReady(SQLiteDatabase theDB);
    }

    private static final int mDATABASE_VERSION = 1;
    private static final String mDATABASE_NAME = "wishare.db";
    private static WiSQLiteDatabase mDB;


    //Table with variables
    //Common variable types include INT, FLOAT, DATE, TIME, varchar([max characters])    <-- string, BIT <-- boolean with 0 | 1
    private static final String mSQL_CREATE_SYNCHRONIZEDCONTACTS =
            "CREATE TABLE SynchronizedContacts (" +
                    "contact_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "phone varchar(255) UNIQUE," +
                    "name varchar(255))";
    private static final String mSQL_CREATE_CONFIGUREDNETWORKS =
            "CREATE TABLE WifiConfiguration (" +
                    "network_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "SSID varchar(255)," +
                    "password varchar(255))";
    private static final String mSQL_CREATE_PERMITTEDCONTACTS =
            "CREATE TABLE PermittedContacts (" +
                    "network_id INTEGER NOT NULL," +
                    "contact_id INTEGER NOT NULL," +
                    "data_limit varchar(255)," +
                    "PRIMARY KEY(network_id, contact_id))";

    private static final String mSQL_DELETE_SYNCHRONIZEDCONTACTS =
            "DROP TABLE IF EXISTS SynchronizedContacts";
    private static final String mSQL_DELETE_CONFIGUREDNETWORKS =
            "DROP TABLE IF EXISTS WifiConfiguration";
    private static final String mSQL_DELETE_PERMITTEDCONTACTS =
            "DROP TABLE IF EXISTS PermittedContacts";

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
        db.execSQL(mSQL_CREATE_SYNCHRONIZEDCONTACTS);
        db.execSQL(mSQL_CREATE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_CREATE_PERMITTEDCONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(mSQL_DELETE_SYNCHRONIZEDCONTACTS);
        db.execSQL(mSQL_DELETE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_DELETE_PERMITTEDCONTACTS);
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
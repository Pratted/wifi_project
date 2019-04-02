package com.example.eric.wishare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import java.util.ArrayList;
import java.util.HashMap;

public class WiSQLiteDatabase extends SQLiteOpenHelper {




    public interface OnDBReadyListener {
        void onDBReady(SQLiteDatabase theDB);
    }

    private static final int mDATABASE_VERSION = 1;
    private static final String mDATABASE_NAME = "wishare.db";
    private static WiSQLiteDatabase sInstance;
    private static final String TAG = "WiSQLiteDatabase";

    public static class TABLE_CONTACTS {
        public static final String COL_CONTACT_ID = "contact_id";
        public static final String COL_NAME = "name";
        public static final String COL_PHONE = "phone";
        public static final String TABLE_NAME = "SynchronizedContacts";
    }

    public static class TABLE_CONFIGURED_NETWORKS {
        public static final String COL_NETWORK_ID = "network_id";
        public static final String COL_SSID = "ssid";
        public static final String COL_PASSWORD = "password";
        public static final String TABLE_NAME = "ConfiguredNetworks";
    }

    public static class TABLE_PERMITTED_CONTACTS {
        public static final String COL_NETWORK_ID = "network_id";
        public static final String COL_CONTACT_ID = "contact_id";
        public static final String COL_SSID = "ssid";
        public static final String COL_PHONE = "phone";
        public static final String COL_DATA_LIMIT = "data_limit";
        public static final String TABLE_NAME = "PermittedContacts";
    }

    public static class TABLE_INVITATIONS {
        public static final String COL_INVITATION_ID = "invitation_id";
        public static final String COL_SSID = "ssid";
        public static final String COL_SENDER = "sender";
        public static final String COL_EXPIRES = "expires";
        public static final String COL_DATA_LIMIT = "data_limit";
        public static final String TABLE_NAME = "Invitations";
    }

    //Table with variables
    //Common variable types include INT, FLOAT, DATE, TIME, varchar([max characters])    <-- string, BIT <-- boolean with 0 | 1
    private static final String mSQL_CREATE_SYNCHRONIZEDCONTACTS =
            "CREATE TABLE " + TABLE_CONTACTS.TABLE_NAME + " (" +
                    TABLE_CONTACTS.COL_CONTACT_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    TABLE_CONTACTS.COL_PHONE + " varchar(255) UNIQUE," +
                    TABLE_CONTACTS.COL_NAME + " varchar(255))";
    private static final String mSQL_CREATE_CONFIGUREDNETWORKS =
            "CREATE TABLE " + TABLE_CONFIGURED_NETWORKS.TABLE_NAME + " (" +
                     TABLE_CONFIGURED_NETWORKS.COL_NETWORK_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                     TABLE_CONFIGURED_NETWORKS.COL_SSID + " varchar(255)," +
                     TABLE_CONFIGURED_NETWORKS.COL_PASSWORD + " varchar(255))";
    private static final String mSQL_CREATE_PERMITTEDCONTACTS =
            "CREATE TABLE " + TABLE_PERMITTED_CONTACTS.TABLE_NAME + " (" +
                    TABLE_PERMITTED_CONTACTS.COL_NETWORK_ID + " INTEGER NOT NULL," +
                    TABLE_PERMITTED_CONTACTS.COL_CONTACT_ID + " INTEGER NOT NULL," +
                    TABLE_PERMITTED_CONTACTS.COL_SSID + " varchar(255)," +
                    TABLE_PERMITTED_CONTACTS.COL_DATA_LIMIT + " varchar(255)," +
                    TABLE_PERMITTED_CONTACTS.COL_PHONE + " varchar(255)," +
                    "PRIMARY KEY(" + TABLE_PERMITTED_CONTACTS.COL_NETWORK_ID + "," + TABLE_PERMITTED_CONTACTS.COL_CONTACT_ID + "))";

    private static final String mSQL_CREATE_INVITATION =
            "CREATE TABLE " + TABLE_INVITATIONS.TABLE_NAME + " (" +
                    TABLE_INVITATIONS.COL_INVITATION_ID + " Integer not null primary key autoincrement," +
                    TABLE_INVITATIONS.COL_SSID + " varchar(255)," +
                    TABLE_INVITATIONS.COL_SENDER + " varchar(255)," +
                    TABLE_INVITATIONS.COL_EXPIRES + " varchar(255)," +
                    TABLE_INVITATIONS.COL_DATA_LIMIT + " Integer)";

    private static final String mSQL_DELETE_SYNCHRONIZEDCONTACTS =
            "DROP TABLE IF EXISTS " + TABLE_CONTACTS.TABLE_NAME;
    private static final String mSQL_DELETE_CONFIGUREDNETWORKS =
            "DROP TABLE IF EXISTS "+ TABLE_CONFIGURED_NETWORKS.TABLE_NAME;
    private static final String mSQL_DELETE_PERMITTEDCONTACTS =
            "DROP TABLE IF EXISTS " + TABLE_PERMITTED_CONTACTS.TABLE_NAME;
    private static final String mSQL_DELETE_INVITATION =
            "DROP TABLE IF EXISTS " + TABLE_INVITATIONS.TABLE_NAME;

    private WiSQLiteDatabase(Context context) {
        super(context.getApplicationContext(),mDATABASE_NAME,null,mDATABASE_VERSION);
    }

    public synchronized HashMap<String, WiContact> loadContacts(){
        HashMap<String, WiContact> contacts = new HashMap<>();

        SQLiteDatabase db = sInstance.getReadableDatabase();
        db.query(TABLE_CONTACTS.TABLE_NAME, null, null, null, null, null, "name");

        Cursor cur = db.query(TABLE_CONTACTS.TABLE_NAME, null, null, null, null, null, "name");

        if (cur != null && cur.moveToFirst()) {
            do {
                WiContact contact = new WiContact(
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_NAME)),
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_PHONE)),
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_CONTACT_ID)));

                contacts.put(contact.getPhone(), contact);
            } while (cur.moveToNext());
        }

        cur.close();

        /*
        cur = db.rawQuery("select phone, ssid from SynchronizedContacts sc join PermittedNetworks pn on sc.ssid = pn.ssid", null);

        if (cur != null && cur.moveToFirst()) {
            do {
                WiContact contact = new WiContact(
                        cur.getString(cur.getColumnIndex("name")),
                        cur.getString(cur.getColumnIndex("phone")),
                        cur.getString(cur.getColumnIndex("contact_id")));

                contacts.put(contact.getPhone(), contact);
            } while (cur.moveToNext());
        }


        cur.close();
        */

        return contacts;
    }

    public ArrayList<WiConfiguration> loadNetworks(){
        ArrayList<WiConfiguration> networks = new ArrayList<>();
        Cursor cur = sInstance.getReadableDatabase().rawQuery("select * from " + TABLE_CONFIGURED_NETWORKS.TABLE_NAME, null);

        if (cur != null && cur.moveToFirst()) {
            do {
                WiConfiguration wiConfiguration = new WiConfiguration(
                        cur.getString(cur.getColumnIndex(TABLE_CONFIGURED_NETWORKS.COL_SSID)),
                        cur.getString(cur.getColumnIndex(TABLE_CONFIGURED_NETWORKS.COL_PASSWORD)),
                        cur.getString(cur.getColumnIndex(TABLE_CONFIGURED_NETWORKS.COL_NETWORK_ID)));
                networks.add(wiConfiguration);
            } while (cur.moveToNext());
        }
        cur.close();
        return networks;
    }

    public synchronized void insert(final WiInvitation invitation){
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "Adding invitation to database");
                ContentValues vals = invitation.toContentValues();

                theDB.insert(TABLE_INVITATIONS.TABLE_NAME, null, vals);
                Log.d(TAG, "Inserted invitation to database");
            }
        });
    }

    public synchronized void insert(final WiContact contact){
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "inserting contact to database");
                theDB.insert(TABLE_CONTACTS.TABLE_NAME, null, contact.toContentValues());
                Log.d(TAG, "Inserted contact to database");
            }
        });
    }

    public synchronized void insert(final WiConfiguration config) {
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "inserting network into database");
                theDB.insert(TABLE_CONFIGURED_NETWORKS.TABLE_NAME, null, config.toContentValues());
                Log.d(TAG, "inserted network into database");
            }
        });
    }

    public synchronized void delete(final WiConfiguration config){
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "removing network from database");
                theDB.delete(TABLE_CONFIGURED_NETWORKS.TABLE_NAME,
                        TABLE_CONFIGURED_NETWORKS.COL_SSID + "=?",
                        new String[]{config.SSID});

                Log.d(TAG, "removed network from database");
            }
        });
    }

    public void delete(final WiInvitation mInvitation) {
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "removing invitation from database");
                theDB.delete(TABLE_INVITATIONS.TABLE_NAME,
                        TABLE_INVITATIONS.COL_SSID + "=?",
                        new String[]{mInvitation.networkName});

                Log.d(TAG, "removed invitation from database");
            }
        });
    }


    public ArrayList<WiInvitation> loadAllInvitations(){
        ArrayList<WiInvitation> invitations = new ArrayList<>();
        Cursor cur = sInstance.getReadableDatabase().rawQuery("select count(*) from Invitations;", null);

        if(cur != null && cur.moveToFirst()){
            do {
                Log.d(TAG, "Records found " + cur.getInt(0));
            } while(cur.moveToNext());
        }

        cur = sInstance.getReadableDatabase().rawQuery("select * from Invitations;", null);

        if(cur != null && cur.moveToFirst()){
            do {
                Log.d(TAG, "Loading invitation");
                invitations.add(WiInvitation.fromCursor(cur));
            } while(cur.moveToNext());
        }
        return invitations;
    }

    public static synchronized WiSQLiteDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WiSQLiteDatabase(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mSQL_CREATE_SYNCHRONIZEDCONTACTS);
        db.execSQL(mSQL_CREATE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_CREATE_PERMITTEDCONTACTS);
        db.execSQL(mSQL_CREATE_INVITATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(mSQL_DELETE_SYNCHRONIZEDCONTACTS);
        db.execSQL(mSQL_DELETE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_DELETE_PERMITTEDCONTACTS);
        db.execSQL(mSQL_DELETE_INVITATION);
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
            return WiSQLiteDatabase.sInstance.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db) {
            listener.onDBReady(db);
        }
    }
}
package com.example.eric.wishare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WiSQLiteDatabase extends SQLiteOpenHelper {

    public interface OnDBReadyListener {
        void onDBReady(SQLiteDatabase theDB);
    }

    private static final int mDATABASE_VERSION = 1;
    private static final String mDATABASE_NAME = "wishare.db";
    private static WiSQLiteDatabase sInstance;
    private static final String TAG = "WiSQLiteDatabase";

    public static class TABLE_CONTACTS {
        public static final String COL_NAME = "name";
        public static final String COL_PHONE = "phone";
        public static final String TABLE_NAME = "SynchronizedContacts";
    }

    public static class TABLE_CONFIGURED_NETWORKS {
        public static final String COL_SSID = "ssid";
        public static final String COL_PASSWORD = "password";
        public static final String COL_DATE_CREATED = "date_created";
        public static final String TABLE_NAME = "ConfiguredNetworks";
    }

    public static class TABLE_PERMITTED_CONTACTS {
        public static final String COL_SSID = "ssid";
        public static final String COL_PHONE = "phone";
        public static final String COL_DATA_LIMIT = "data_limit";
        public static final String COL_EXPIRES = "expires";
        public static final String COL_DATE_CREATED = "date_created";
        public static final String TABLE_NAME = "PermittedContacts";
    }

    public static class TABLE_INVITATIONS {
        public static final String COL_SSID = "ssid";
        public static final String COL_SENDER = "sender";
        public static final String COL_EXPIRES = "expires";
        public static final String COL_DATA_LIMIT = "data_limit";
        public static final String COL_DATE_CREATED = "date_created";
        public static final String TABLE_NAME = "Invitations";
    }

    private static class TABLE_PENDING_INVITATIONS {
        public static final String COL_SSID = "ssid";
        public static final String COL_PHONE = "phone";
        public static final String COL_EXPIRES = "expires";
        public static final String COL_DATA_LIMIT = "data_limit";
        public static final String COL_DATE_CREATED = "date_created";
        public static final String TABLE_NAME = "PendingInvitations";
    }

    //Table with variables
    //Common variable types include INT, FLOAT, DATE, TIME, varchar([max characters])    <-- string, BIT <-- boolean with 0 | 1
    private static final String mSQL_CREATE_SYNCHRONIZEDCONTACTS =
            "CREATE TABLE " + TABLE_CONTACTS.TABLE_NAME + " (" +
                    TABLE_CONTACTS.COL_PHONE + " varchar(255) PRIMARY KEY ," +
                    TABLE_CONTACTS.COL_NAME + " varchar(255))";

    private static final String mSQL_CREATE_CONFIGUREDNETWORKS =
            "CREATE TABLE " + TABLE_CONFIGURED_NETWORKS.TABLE_NAME + " (" +
                     TABLE_CONFIGURED_NETWORKS.COL_SSID + " varchar(255) PRIMARY KEY," +
                     TABLE_CONFIGURED_NETWORKS.COL_PASSWORD + " varchar(255))";

    private static final String mSQL_CREATE_PERMITTEDCONTACTS =
            "CREATE TABLE " + TABLE_PERMITTED_CONTACTS.TABLE_NAME + " (" +
                    TABLE_PERMITTED_CONTACTS.COL_SSID + " varchar(255)," +
                    TABLE_PERMITTED_CONTACTS.COL_PHONE + " varchar(255)," +
                    TABLE_PERMITTED_CONTACTS.COL_EXPIRES + " varchar(255)," +
                    TABLE_PERMITTED_CONTACTS.COL_DATA_LIMIT + " varchar(255)," +
                    TABLE_PERMITTED_CONTACTS.COL_DATE_CREATED + " varchar(255)," +
                    "PRIMARY KEY(" + TABLE_PERMITTED_CONTACTS.COL_SSID + "," + TABLE_PERMITTED_CONTACTS.COL_PHONE + "))";

    private static final String mSQL_CREATE_INVITATION =
            "CREATE TABLE " + TABLE_INVITATIONS.TABLE_NAME + " (" +
                    TABLE_INVITATIONS.COL_SSID + " varchar(255)," +
                    TABLE_INVITATIONS.COL_SENDER + " varchar(255)," +
                    TABLE_INVITATIONS.COL_EXPIRES + " varchar(255)," +
                    TABLE_INVITATIONS.COL_DATA_LIMIT + " varchar(255)," +
                    TABLE_INVITATIONS.COL_DATE_CREATED + " varchar(255)," +
                    "PRIMARY KEY(" + TABLE_INVITATIONS.COL_SSID + "," + TABLE_INVITATIONS.COL_SENDER + "))";

    private static final String mSQL_CREATE_PENDING_INVITATION =
            "CREATE TABLE " + TABLE_PENDING_INVITATIONS.TABLE_NAME + " (" +
                    TABLE_PENDING_INVITATIONS.COL_SSID + " varchar(255)," +
                    TABLE_PENDING_INVITATIONS.COL_PHONE + " varchar(255)," +
                    TABLE_PENDING_INVITATIONS.COL_EXPIRES + " varchar(255)," +
                    TABLE_PENDING_INVITATIONS.COL_DATA_LIMIT + " varchar(255)," +
                    TABLE_PENDING_INVITATIONS.COL_DATE_CREATED + " varchar(255)," +
                    "PRIMARY KEY(" + TABLE_PENDING_INVITATIONS.COL_SSID + "," + TABLE_PENDING_INVITATIONS.COL_PHONE + "))";


    private static final String mSQL_DELETE_SYNCHRONIZEDCONTACTS =
            "DROP TABLE IF EXISTS " + TABLE_CONTACTS.TABLE_NAME;
    private static final String mSQL_DELETE_CONFIGUREDNETWORKS =
            "DROP TABLE IF EXISTS "+ TABLE_CONFIGURED_NETWORKS.TABLE_NAME;
    private static final String mSQL_DELETE_PERMITTEDCONTACTS =
            "DROP TABLE IF EXISTS " + TABLE_PERMITTED_CONTACTS.TABLE_NAME;
    private static final String mSQL_DELETE_INVITATION =
            "DROP TABLE IF EXISTS " + TABLE_INVITATIONS.TABLE_NAME;
    private static final String mSQL_DELETE_PENDING_INVITATION =
            "DROP TABLE IF EXISTS " + TABLE_PENDING_INVITATIONS.TABLE_NAME;

    private WiSQLiteDatabase(Context context) {
        super(context.getApplicationContext(),mDATABASE_NAME,null,mDATABASE_VERSION);
    }

    public synchronized HashMap<String, WiContact> loadContacts(){
        HashMap<String, WiContact> contacts = new HashMap<>();

        // load all the contacts from the contacts table...
        Cursor cur = getReadableDatabase().query(TABLE_CONTACTS.TABLE_NAME, null, null, null, null, null, null);

        if (cur != null && cur.moveToFirst()) {
            do {
                WiContact contact = new WiContact(
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_NAME)),
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_PHONE)));
                contacts.put(contact.getPhone(), contact);
            } while (cur.moveToNext());
        }

        // load all pending invitations and apply to each contact...
        cur = getReadableDatabase().query(TABLE_PENDING_INVITATIONS.TABLE_NAME, null, null, null, null, null, null);

        if(cur.moveToFirst()){
            do {
                String recipientPhone = cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_PHONE));

                WiInvitation invitation = new WiInvitation(
                        cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_SSID)),
                        WiUtils.getDevicePhone(),
                        cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_EXPIRES)),
                        "",
                        cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_DATA_LIMIT))
                );

                contacts.get(recipientPhone).invite(invitation);
            } while(cur.moveToNext());
        }

        // load all permitted networks and apply to each contact
        cur = getReadableDatabase().query(TABLE_PERMITTED_CONTACTS.TABLE_NAME, null, null, null, null, null, null);

        if(cur.moveToFirst()){
            do {
                String ssid = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_SSID));
                String recipientPhone = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_PHONE));
                //String expires = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_EXPIRES));
                //String dataLimit = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_DATA_LIMIT));

                WiConfiguration config = new WiConfiguration(ssid, "");
                contacts.get(recipientPhone).grantAccess(config);
            } while(cur.moveToNext());
        }

        return contacts;
    }

    public ArrayList<WiConfiguration> loadNetworks(){
        ArrayList<WiConfiguration> networks = new ArrayList<>();
        Cursor cur = sInstance.getReadableDatabase().query(TABLE_CONFIGURED_NETWORKS.TABLE_NAME, null, null, null, null, null, null);

        if (cur != null && cur.moveToFirst()) {
            do {
                WiConfiguration wiConfiguration = new WiConfiguration(
                        cur.getString(cur.getColumnIndex(TABLE_CONFIGURED_NETWORKS.COL_SSID)),
                        cur.getString(cur.getColumnIndex(TABLE_CONFIGURED_NETWORKS.COL_PASSWORD)),
                        "");
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

    private List<WiConfiguration> loadPermittedNetworks(WiContact contact){
        ArrayList<WiConfiguration> permittedNetworks = new ArrayList<>();
        Cursor cur = getReadableDatabase().query(TABLE_PERMITTED_CONTACTS.TABLE_NAME, new String[]{TABLE_PERMITTED_CONTACTS.COL_SSID},
                "phone=?", new String[]{contact.getPhone()}, null, null, null);

        if(cur.moveToFirst()){
            do {
                permittedNetworks.add(new WiConfiguration(cur.getString(0),""));
            } while(cur.moveToNext());
        }

        return permittedNetworks;
    }

    private List<WiInvitation> loadPendingInvitations(WiContact contact){
        ArrayList<WiInvitation> pendingInvitations = new ArrayList<>();
        Cursor cur = getReadableDatabase().query(TABLE_PENDING_INVITATIONS.TABLE_NAME, new String[]{TABLE_PENDING_INVITATIONS.COL_SSID},
                "phone=?", new String[]{contact.getPhone()}, null, null, null);

        if(cur.moveToFirst()){
            do {
                WiInvitation invitation = new WiInvitation(
                    cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_SSID)),
                    WiUtils.getDevicePhone(),
                    cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_EXPIRES)),
                    "",
                    cur.getString(cur.getColumnIndex(TABLE_PENDING_INVITATIONS.COL_DATA_LIMIT))
                );

                pendingInvitations.add(invitation);
            } while(cur.moveToNext());
        }

        return pendingInvitations;
    }


    public synchronized void insertPendingInvitation(final WiInvitation invitation, final WiContact recipient){
        Log.d(TAG, "ssid=" + invitation.getWiConfiguration().SSID);
        Log.d(TAG, "phone=" + recipient.getPhone());

        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "Adding pending invitation to database");
                ContentValues vals = new ContentValues();
                vals.put(TABLE_PENDING_INVITATIONS.COL_PHONE, recipient.getPhone());
                vals.put(TABLE_PENDING_INVITATIONS.COL_SSID, invitation.getWiConfiguration().SSID);
                vals.put(TABLE_PENDING_INVITATIONS.COL_DATE_CREATED, WiUtils.getDateTime());
                theDB.insert(TABLE_PENDING_INVITATIONS.TABLE_NAME, null, vals);
                Log.d(TAG, "Inserted pending invitation to database");
            }
        });
    }

    public synchronized void insertPermittedContact(final WiContact contact, final WiConfiguration config){
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "Adding permitted contact to database");
                ContentValues vals = new ContentValues();
                vals.put(TABLE_PERMITTED_CONTACTS.COL_SSID, config.SSID);
                vals.put(TABLE_PERMITTED_CONTACTS.COL_PHONE, contact.getPhone());
                vals.put(TABLE_PERMITTED_CONTACTS.COL_EXPIRES, contact.getExpiresIn());
                vals.put(TABLE_PERMITTED_CONTACTS.COL_DATA_LIMIT, "None");
                vals.put(TABLE_PERMITTED_CONTACTS.COL_DATE_CREATED, WiUtils.getDateTime());
                vals.put(TABLE_PERMITTED_CONTACTS.COL_SSID, config.getSSID());

                theDB.insert(TABLE_PERMITTED_CONTACTS.TABLE_NAME, null, vals);
                Log.d(TAG, "Added permitted contact to database");
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

    /*
    public synchronized void insert(final WiContact contact, final WiConfiguration config) {
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                ContentValues vals = new ContentValues();
                vals.put(TABLE_PERMITTED_CONTACTS.COL_CONTACT_ID, contact.getContactID());
                vals.put(TABLE_PERMITTED_CONTACTS.COL_NETWORK_ID, config.getNetworkID());
                vals.put(TABLE_PERMITTED_CONTACTS.COL_DATA_LIMIT, "None");
                vals.put(TABLE_PERMITTED_CONTACTS.COL_SSID, config.getSSID());

                theDB.insert(TABLE_PERMITTED_CONTACTS.TABLE_NAME, null, vals);
            }
        });
    }
    */

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

    public synchronized void delete(final WiInvitation mInvitation) {
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

    public synchronized void delete(final WiConfiguration config, final String phone) {
        getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "removing permitted contact from database");
                theDB.delete(TABLE_PERMITTED_CONTACTS.TABLE_NAME,
                        TABLE_PERMITTED_CONTACTS.COL_SSID + "=?," +
                                TABLE_PERMITTED_CONTACTS.COL_PHONE + "=?",
                        new String[]{config.SSID, phone});

                Log.d(TAG, "removed permitted contact from database");
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

    public synchronized void reset(){
        SQLiteDatabase db = getWritableDatabase();

        deleteAllTables(db);
        createAllTables(db);
    }

    public synchronized void createAllTables(SQLiteDatabase db){
        db.execSQL(mSQL_CREATE_SYNCHRONIZEDCONTACTS);
        db.execSQL(mSQL_CREATE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_CREATE_PERMITTEDCONTACTS);
        db.execSQL(mSQL_CREATE_INVITATION);
        db.execSQL(mSQL_CREATE_PENDING_INVITATION);
    }

    public synchronized void deleteAllTables(SQLiteDatabase db){
        db.execSQL(mSQL_DELETE_SYNCHRONIZEDCONTACTS);
        db.execSQL(mSQL_DELETE_CONFIGUREDNETWORKS);
        db.execSQL(mSQL_DELETE_PERMITTEDCONTACTS);
        db.execSQL(mSQL_DELETE_INVITATION);
        db.execSQL(mSQL_DELETE_PENDING_INVITATION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteAllTables(db);
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
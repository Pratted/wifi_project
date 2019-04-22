package com.example.eric.wishare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.util.Log;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Log.d(TAG, "Initializing cache...");
        mPermittedNetworksCache = new HashMap<>();
        mPendingInvitationsCache = new HashMap<>();
    }

    private HashMap<String, HashSet<String>> mPermittedNetworksCache;
    private HashMap<String, HashSet<String>> mPendingInvitationsCache;

    public synchronized HashMap<String, WiContact> loadContacts(){
        HashMap<String, WiContact> contacts = new HashMap<>();

        // load all the contacts from the contacts table...
        Cursor cur = sInstance.getReadableDatabase().query(TABLE_CONTACTS.TABLE_NAME, null, null, null, null, null, null);

        if (cur != null && cur.moveToFirst()) {
            do {
                WiContact contact = new WiContact(
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_NAME)),
                        cur.getString(cur.getColumnIndex(TABLE_CONTACTS.COL_PHONE)));
                contacts.put(contact.getPhone(), contact);
            } while (cur.moveToNext());
        }

        // load all pending invitations and apply to each contact...
        cur = sInstance.getReadableDatabase().query(TABLE_PENDING_INVITATIONS.TABLE_NAME, null, null, null, null, null, null);

        Log.d(TAG, "BEGIN load pending invitations...");
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

                if(!mPendingInvitationsCache.containsKey(recipientPhone)) {
                    mPendingInvitationsCache.put(recipientPhone, new HashSet<String>());
                }

                mPendingInvitationsCache.get(recipientPhone).add(invitation.networkName);

                Log.d(TAG, "Reciepient phone = " + recipientPhone);
                contacts.get(recipientPhone).invite(invitation);
            } while(cur.moveToNext());
        }

        // load all permitted networks and apply to each contact
        cur = sInstance.getReadableDatabase().query(TABLE_PERMITTED_CONTACTS.TABLE_NAME, null, null, null, null, null, null);

        Log.d(TAG, "BEGIN load permitted networks...");
        if(cur.moveToFirst()){
            do {
                String ssid = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_SSID));
                String recipientPhone = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_PHONE));
                //String expires = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_EXPIRES));
                //String dataLimit = cur.getString(cur.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_DATA_LIMIT));

                if(!mPermittedNetworksCache.containsKey(recipientPhone)) {
                    Log.d(TAG, "Adding " + recipientPhone + " to permittedNetworkCache");
                    mPermittedNetworksCache.put(recipientPhone, new HashSet<String>());
                }

                mPermittedNetworksCache.get(recipientPhone).add(ssid);

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
        sInstance.getWritableDatabase(new OnDBReadyListener() {
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
        sInstance.getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "inserting contact to database");
                theDB.insert(TABLE_CONTACTS.TABLE_NAME, null, contact.toContentValues());

                // update our cache
                mPermittedNetworksCache.put(contact.getPhone(), new HashSet<String>());
                mPendingInvitationsCache.put(contact.getPhone(), new HashSet<String>());
                Log.d(TAG, "Inserted contact to database");
            }
        });
    }

    public synchronized void insert(final WiConfiguration config) {
        sInstance.getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "inserting network into database");
                theDB.insert(TABLE_CONFIGURED_NETWORKS.TABLE_NAME, null, config.toContentValues());
                Log.d(TAG, "inserted network into database");
            }
        });
    }

    private void saveContactPermittedNetworks(final WiContact contact, SQLiteDatabase db){
        List<WiConfiguration> temp = contact.getPermittedNetworks();
        Set<String> permittedNetworks = new HashSet<>();

        if(!mPermittedNetworksCache.containsKey(contact.getPhone())){
            mPermittedNetworksCache.put(contact.getPhone(), new HashSet<String>());
        }

        final Set<String> cache = mPermittedNetworksCache.get(contact.getPhone());

        for(WiConfiguration config: temp){
            permittedNetworks.add(config.SSID);
        }

        for(String ssid: permittedNetworks){

            // this network is not in cache. It was recently added so it must be saved in DB.
            if(!cache.contains(ssid)){
                cache.add(ssid);
            }
        }

        final ArrayList<String> toBeDeleted = new ArrayList<>();
        for(String ssid: cache){
            // the contact no longer has access to this network. It needs to be deleted from db
            if(!permittedNetworks.contains(ssid)){
                Log.d(TAG, "Need to delete: " + ssid);
                toBeDeleted.add(ssid);
            }
        }

        for(String bad: toBeDeleted){
            cache.remove(bad);
        }

        try {
            for(String ssid: toBeDeleted){
                db.delete(TABLE_PERMITTED_CONTACTS.TABLE_NAME, "ssid=?", new String[]{ssid});
            }

            for(String ssid: cache){
                ContentValues cv = new ContentValues();
                cv.put(TABLE_PERMITTED_CONTACTS.COL_SSID, ssid);
                cv.put(TABLE_PERMITTED_CONTACTS.COL_PHONE, contact.getPhone());
                cv.put(TABLE_PERMITTED_CONTACTS.COL_EXPIRES, contact.getExpiresIn());

                db.replace(TABLE_PERMITTED_CONTACTS.TABLE_NAME, null, cv);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void saveContactPendingInvitations(final WiContact contact, SQLiteDatabase db){
        List<WiInvitation> temp = contact.getPendingInvitations();
        Set<String> pendingInvitations = new HashSet<>();

        if(!mPendingInvitationsCache.containsKey(contact.getPhone())){
            mPendingInvitationsCache.put(contact.getPhone(), new HashSet<String>());
        }

        final Set<String> cache = mPendingInvitationsCache.get(contact.getPhone());

        for(WiInvitation inv: temp){
            pendingInvitations.add(inv.networkName);
        }
        int x = 0;
        for(String ssid: pendingInvitations){

            // this invitation is not in cache. It was recently added so it must be saved in DB.
            if(!cache.contains(ssid)){
                cache.add(ssid);
            }
        }

        final ArrayList<String> toBeDeleted = new ArrayList<>();
        for(String ssid: cache){

            // the contact no longer this pending invitation. It needs to be deleted from db.
            if(!pendingInvitations.contains(ssid)){
                toBeDeleted.add(ssid);
            }
        }

        for(String bad: toBeDeleted){
            cache.remove(bad);
        }

        try {
            for(String ssid: toBeDeleted){
                db.delete(TABLE_PENDING_INVITATIONS.TABLE_NAME, "ssid=? and phone=?", new String[]{ssid, contact.getPhone()});
            }

            for(String ssid: cache){
                ContentValues cv = new ContentValues();
                cv.put(TABLE_PENDING_INVITATIONS.COL_SSID, ssid);
                cv.put(TABLE_PENDING_INVITATIONS.COL_PHONE, contact.getPhone());
                cv.put(TABLE_PENDING_INVITATIONS.COL_EXPIRES, contact.getExpiresIn());

                db.replace(TABLE_PENDING_INVITATIONS.TABLE_NAME, null, cv);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void save(final WiContact contact){
        sInstance.getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                saveContactPermittedNetworks(contact, theDB);
                saveContactPendingInvitations(contact, theDB);
                Log.d(TAG, "Saved contact");

            }
        });
    }

    public synchronized void save(final List<WiContact> contacts){
        sInstance.getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                for(WiContact contact: contacts){
                    saveContactPermittedNetworks(contact, theDB);
                    saveContactPendingInvitations(contact, theDB);
                }

                Log.d(TAG, "Saved " + contacts.size() + "contacts");
            }
        });
    }

    public synchronized void delete(final WiConfiguration config){
        sInstance.getWritableDatabase(new OnDBReadyListener() {
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
        sInstance.getWritableDatabase(new OnDBReadyListener() {
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
        sInstance.getWritableDatabase(new OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase theDB) {
                Log.d(TAG, "removing permitted contact from database");
                theDB.delete(TABLE_PERMITTED_CONTACTS.TABLE_NAME,
                        TABLE_PERMITTED_CONTACTS.COL_SSID + "=? and " +
                                TABLE_PERMITTED_CONTACTS.COL_PHONE + "=?",
                        new String[]{config.SSID, phone});

                Log.d(TAG, "removed permitted contact from database");
            }
        });
    }

    public synchronized ArrayList<String> getContactNetworks(WiContact contact){
        ArrayList<String> ssidList = new ArrayList<>();
        Log.d(TAG, "Getting all contact's network from database");
        Cursor cursor = sInstance.getReadableDatabase().query(TABLE_PERMITTED_CONTACTS.TABLE_NAME, new String[]{TABLE_PERMITTED_CONTACTS.COL_SSID},
                TABLE_PERMITTED_CONTACTS.COL_PHONE + "=?", new String[]{contact.getPhone()}, null, null, null);
        while(cursor != null && cursor.moveToNext()) {
            ssidList.add(cursor.getString(cursor.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_SSID)));
        }
        return ssidList;
    }

    public synchronized ArrayList<String> getNetworksContacts(final WifiConfiguration config){
        final ArrayList<String> phoneList = new ArrayList<String>();
            Log.d(TAG, "getting ALL permitted contacts for the selected network");
            Cursor cursor = sInstance.getReadableDatabase().query(TABLE_PERMITTED_CONTACTS.TABLE_NAME, new String[]{TABLE_PERMITTED_CONTACTS.COL_PHONE}, TABLE_PERMITTED_CONTACTS.COL_SSID + "=?", new String[]{config.SSID}, null, null, null);
            while(cursor != null && cursor.moveToNext()) {
                phoneList.add(cursor.getString(cursor.getColumnIndex(TABLE_PERMITTED_CONTACTS.COL_PHONE)));
            }
        return phoneList;
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

    public synchronized void getWritableDatabase(OnDBReadyListener listener) {
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
            // exit early and do nothing
            if(!WiUtils.isDatabaseEnabled()){
                Log.d(TAG, "The database is not enabled.");
                return;
            }

            db.beginTransaction();

            // if a DB write fails, don't crash the entire app
            try {
                listener.onDBReady(db);
            } catch (SQLException e){
                e.printStackTrace();
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
}
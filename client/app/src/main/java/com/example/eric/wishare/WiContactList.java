package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiDataMessage;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class WiContactList {
    private static final String TAG = "WiContactList";

    private static HashMap<String, WiContact> mContacts;
    private WeakReference<Context> mContext;
    private static WiContactList sInstance;
    private SQLiteDatabase mDatabase;

    private OnContactListReadyListener mContactListReadyListener;
    private AsyncTask<Void,Void,Void> mSynchronizeContactsTask;

    // boolean flag to keep make sure synchronizeContacts only runs once
    private static boolean synchronizing = false;

    private WiDataMessage msg;
    private WiDataMessageController mDataMessageController;

    @SuppressLint("StaticFieldLeak")
    private WiContactList(Context context) {
        mContacts = new HashMap<>();

        mContext = new WeakReference<>(context.getApplicationContext());
        mDataMessageController = WiDataMessageController.getInstance(context);
        mSynchronizeContactsTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "Begin Synchronize Contacts!");
                final HashMap<String, WiContact> deviceContacts = loadDeviceContacts();
                Log.d(TAG, "Loaded " + deviceContacts.size() + " contacts from phone");

                // mContacts is initialized with contacts from DB
                mContacts = WiSQLiteDatabase.getInstance(mContext.get()).loadContacts();

                // this data message contains the device's contact list as an array of phone numbers
                // the server responds with a subset of them (the contacts with wishare installed)
                msg = new WiDataMessage(WiDataMessage.MSG_CONTACT_LIST, deviceContacts.values());

                msg.setOnResponseListener(new WiDataMessage.OnResponseListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response == null){
                            Log.e(TAG, "Shit. Server did not respond");
                            return;
                        }

                        Log.d(TAG, "Received Response from server");
                        Log.d(TAG, response.toString());

                        final HashSet<String> contactsWithWiShare = new HashSet<>();

                        try{
                            for(int i = 0; i < response.getJSONArray("phones").length(); i++){
                                contactsWithWiShare.add(response.getJSONArray("phones").get(i).toString());
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        Log.d(TAG, "There are " + mContacts.size() + " contacts in the database");

                        for(WiContact contact: mContacts.values()){
                            Log.d(TAG, contact.getPhone() + " ->" + contact.getName());
                        }

                        Log.d(TAG, "End display contacts in DB");

                        WiSQLiteDatabase.getInstance(mContext.get()).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
                            @Override
                            public void onDBReady(SQLiteDatabase theDB) {
                                Log.d(TAG, "Begin adding contacts to database!");

                                for(String phone: contactsWithWiShare){
                                    WiContact contact = mContacts.get(phone);

                                    Log.d(TAG, "Checking if " + phone + " is in the DB...");
                                    if(contact == null){
                                        Log.d(TAG, "It is not the DB. Lets add it lol");
                                        theDB.insert(WiSQLiteDatabase.TABLE_CONTACTS.TABLE_NAME, null, deviceContacts.get(phone).toContentValues());
                                    }
                                }

                                if(mContactListReadyListener != null){
                                    mContactListReadyListener.onContactListReady(mContacts);
                                }

                                theDB.close();
                            }
                        });

                    }
                });

                mDataMessageController.send(msg);

                Log.d(TAG, "End Synchronize Contacts!");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
    }

    public Map<String, WiContact> getWiContacts(){
        return mContacts;
    }

    public interface OnContactListReadyListener{
        void onContactListReady(HashMap<String, WiContact> contacts);
    }

    public void setOnContactListReadyListener(OnContactListReadyListener listener){
        mContactListReadyListener = listener;
    }

    private HashMap<String, WiContact> loadDeviceContacts(){
        HashMap<String, WiContact>  contacts = new HashMap<>();
        ContentResolver resolver = mContext.get().getContentResolver();

        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while(cursor != null && cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = WiUtils.formatPhoneNumber(phone);

            if(!phone.isEmpty()){
                Log.d(TAG, "Loaded {" + name + "} {" + phone + "}");

                WiContact contact = new WiContact(name, phone);
                contacts.put(contact.getPhone(), contact);
            }
        }

        return contacts;
    }

    private HashMap<String, WiContact> loadDbContacts() {
        HashMap<String, WiContact> contacts  = WiSQLiteDatabase.getInstance(mContext.get()).loadContacts();
        //HashMap<String, WiContact> contacts = new HashMap<>();

        /*
        SQLiteDatabase db = WiSQLiteDatabase.getInstance(mContext.get()).getReadableDatabase();
        Cursor cur = db.rawQuery("select * from SynchronizedContacts order by name asc", null);

        if (cur != null && cur.moveToFirst()) {
            do {
                WiContact contact = new WiContact(
                        cur.getString(cur.getColumnIndex("name")),
                        cur.getString(cur.getColumnIndex("phone")),
                        cur.getString(cur.getColumnIndex("contact_id")));

                contacts.put(contact.getPhone(), contact);
            } while (cur.moveToNext());
        }

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

    private synchronized ArrayList<WiContact> getPermittedContacts(final String networkSSID){
        final ArrayList<WiContact> permittedContacts = new ArrayList<>();
        WiSQLiteDatabase.getInstance(mContext.get()).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                mDatabase = db;
                Cursor c = mDatabase.query("PermittedContacts", null, "SSID=?", new String[]{networkSSID}, null, null,"name asc");
                if (c.moveToFirst()) {
                    WiContact contact = new WiContact(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("phone")));
                    permittedContacts.add(contact);
                    while(c.moveToNext()) {
                        contact = new WiContact(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("phone")));
                        permittedContacts.add(contact);
                    }
                }
                c.close();
                db.close();
            }
        });
        return permittedContacts;
    }

    public synchronized void synchronizeContacts(){
        if(!synchronizing){
            synchronizing = true;
            mSynchronizeContactsTask.execute();
        }
    }

    public static synchronized WiContactList getInstance(Context context){
        if (sInstance == null){
            sInstance = new WiContactList(context.getApplicationContext());
        }

        return sInstance;
    }

    public WiContact getContactByPhone(String phone){
        return mContacts.get(phone);
    }
}

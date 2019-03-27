package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiDataMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WiContactList {
    private static final String TAG = "WiContactList";

    private static ArrayList<WiContact> mContactListArray;
    private static HashMap<String, WiContact> mContactListMap;
    private WeakReference<Context> mContext;
    private static WiContactList mCL;
    private SQLiteDatabase mDatabase;

    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEnteredListener;
    private OnContactListReadyListener mContactListReadyListener;

    private HashMap<String, WiContact> mDeviceContacts;
    private HashMap<String, WiContact> mDbContacts;

    private AsyncTask<Void,Void,Void> mSynchronizeContactsTask;

    // boolean flag to keep make sure synchronizeContacts only runs once
    private static boolean synchronizing = false;

    private WiDataMessage msg;
    private WiDataMessageController mDataMessageController;
    private ArrayList<WiContact> mBuffer;

    @SuppressLint("StaticFieldLeak")
    private WiContactList(Context context) {
        mContactListArray = new ArrayList<>();
        mContactListMap = new HashMap<>();
        mContext = new WeakReference<>(context.getApplicationContext());
        mDeviceContacts = new HashMap<>();
        mDbContacts = new HashMap<>();
        mDataMessageController = WiDataMessageController.getInstance(context);

        mSynchronizeContactsTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "Begin Synchronize Contacts!");
                mDeviceContacts = loadDeviceContacts();
                mDbContacts = loadDbContacts();

                msg = new WiDataMessage(WiDataMessage.MSG_CONTACT_LIST);
                Log.d(TAG, "Loaded " + mDeviceContacts.size() + " contacts from phone");

                JSONArray jsonPhones = new JSONArray();
                for(WiContact contact: mDeviceContacts.values()){
                    if(contact.getPhone() != null){
                        String phone = contact.getPhone();

                        if(!phone.isEmpty()){
                            jsonPhones.put(phone);
                        }
                    }
                }

                try {
                    msg.put("phones", jsonPhones);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                msg.setOnResponseListener(new WiDataMessage.OnResponseListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response == null){
                            Log.e(TAG, "Server did not respond");
                            return;
                        }

                        Log.d(TAG, "Received Response from server");
                        Log.d(TAG, response.toString());
                        mContactListArray = new ArrayList<>();

                        HashSet<String> contactsWithWiShare = new HashSet<>();

                        try{
                            for(int i = 0; i < response.getJSONArray("phones").length(); i++){
                                contactsWithWiShare.add(response.getJSONArray("phones").get(i).toString());
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }


                        mBuffer = new ArrayList<>();

                        Log.d(TAG, "There are " + mDbContacts.size() + " contacts in the database");

                        for(String phone: contactsWithWiShare){
                            Log.d(TAG, "Checking if " + phone + " is in the DB...");
                            if(!mDbContacts.keySet().contains(phone)){
                                Log.d(TAG, "It is not the DB. Lets add it lol");
                                mBuffer.add(mDeviceContacts.get(phone));
                            }
                        }

                        WiSQLiteDatabase.getInstance(mContext.get()).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
                            @Override
                            public void onDBReady(SQLiteDatabase theDB) {
                                Log.d(TAG, "Begin adding contacts to database!");
                                for(WiContact contact: mBuffer){
                                    Log.d(TAG, "Adding record to database! " + contact.toString());
                                    contact.setPhone(WiUtils.formatPhoneNumber(contact.getPhone()));
                                    theDB.insert("SynchronizedContacts", null, contact.toContentValues());
                                }
                                Cursor cur = theDB.rawQuery("SELECT * FROM SynchronizedContacts", null);
                                if (cur != null && cur.moveToFirst()) {
                                    do {
                                        WiContact contact = new WiContact(
                                                cur.getString(cur.getColumnIndex("name")),
                                                cur.getString(cur.getColumnIndex("phone")),
                                                cur.getString(cur.getColumnIndex("contact_id")));

/*                                        //testing purposes
                                        if (contact.getName().contains("Eric")) {
                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put("network_id", "1");
                                            contentValues.put("contact_id", contact.getContactID());
                                            contentValues.put("SSID", "KHQ");
                                            theDB.insert("PermittedContacts", null, contentValues);
                                        }*/

                                        mContactListArray.add(contact);
                                        mContactListMap.put(contact.getPhone(), contact);
                                    } while (cur.moveToNext());
                                }
                                mBuffer.clear();
                                synchronizing = false;
                                mContactListReadyListener.onContactListReady(mContactListArray);
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

    public ArrayList<WiContact> getWiContacts(){
        return mContactListArray;
    }

    public interface OnContactListReadyListener{
        void onContactListReady(ArrayList<WiContact> contacts);
    }

    public void setOnContactListReadyListener(OnContactListReadyListener listener){
        mContactListReadyListener = listener;
    }

    private HashMap<String, WiContact> loadDeviceContacts(){
        HashMap<String, WiContact>  contacts = new HashMap<>();
        ContentResolver resolver = mContext.get().getContentResolver();

        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while(cursor != null && cursor.moveToNext()) {
            WiContact contact = new WiContact(
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

            contacts.put(contact.getPhone(), contact);
        }

        return contacts;
    }

    private HashMap<String, WiContact> loadDbContacts() {
        HashMap<String, WiContact> contacts = new HashMap<>();
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
        cur.close();

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
        if (mCL == null){
            mCL = new WiContactList(context.getApplicationContext());
        }

        return mCL;
    }

    public WiContact getContactByPhone(String phone){
        return mContactListMap.get(phone);
    }
}

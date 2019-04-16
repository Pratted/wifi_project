package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.messaging.WiDataMessage;
import com.example.eric.wishare.model.messaging.WiSynchronizeContactDataMessage;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WiContactList {
    private static final String TAG = "WiContactList";

    private static HashMap<String, WiContact> mContacts;
    private WeakReference<Context> mContext;
    private static WiContactList sInstance;
    private WiSQLiteDatabase mDatabase;

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
        mDatabase = WiSQLiteDatabase.getInstance(context.getApplicationContext());

        mSynchronizeContactsTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "Begin Synchronize Contacts!");
                final HashMap<String, WiContact> deviceContacts = loadDeviceContacts();
                Log.d(TAG, "Loaded " + deviceContacts.size() + " contacts from phone");

                // mContacts is initialized with contacts from DB
                mContacts = mDatabase.loadContacts();

                Log.d(TAG, "There are " + mContacts.size() + " contacts in the database");

                for(WiContact contact: mContacts.values()){
                    Log.d(TAG, contact.getPhone() + " ->" + contact.getName());
                }

                Log.d(TAG, "End display contacts in DB");

                // this data message contains the device's contact list as an array of phone numbers
                // the server responds with a subset of them (the contacts with wishare installed)
                msg = new WiSynchronizeContactDataMessage(new ArrayList<>(deviceContacts.values())){
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response == null){
                            Log.e(TAG, "Shit. Server did not respond");
                            return;
                        }

                        Log.d(TAG, "Received Response from server");
                        Log.d(TAG, response.toString());

                        for(String phone: this.getIncomingPhoneNumbers(response)){
                            if(!mContacts.containsKey(phone)){
                                // async write to DB
                                mDatabase.insert(deviceContacts.get(phone));

                                // don't wait on DB to add record to map. just do it now
                                mContacts.put(phone, deviceContacts.get(phone));
                            }
                            else{ // they've already been loaded from DB, so do nothing
                            }
                        }

                        if(mContactListReadyListener != null){
                            mContactListReadyListener.onContactListReady(mContacts);
                        }
                    }
                };

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

    public WiContact save(WiContact contact) {
        mContacts.put(contact.getPhone(), contact);
        return contact;
    }

    public void deleteNetworkFromAllContacts(String networkID) {
        for(WiContact contact : mContacts.values()) {
            contact.revokeAccess(networkID);
        }
    }
}

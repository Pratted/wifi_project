package com.example.eric.wishare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WiContactList {
    private static final String TAG = "WiContactList";

    private static ArrayList<WiContact> mContactListArray;
    private static HashMap<String, WiContact> mContactListMap;
    private WeakReference<Context> mContext;
    private static WiContactList mCL;
    private SQLiteDatabase mDatabase;

    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEnteredListener;
    private OnContactListReadyListener mContactListReadyListener;

    private ArrayList<WiContact> mDeviceContacts;
    private ArrayList<WiContact> mDbContacts;

    private AsyncTask<Void,Void,Void> mSynchronizeContactsTask;

    // boolean flag to keep make sure synchronizeContacts only runs once
    private static boolean synchronizing = false;

    private WiDataMessage msg;
    private WiDataMessageController mDataMessageController;

    @SuppressLint("StaticFieldLeak")
    private WiContactList(Context context) {
        mContactListArray = new ArrayList<>();
        mContactListMap = new HashMap<>();
        mContext = new WeakReference<>(context.getApplicationContext());
        mDeviceContacts = new ArrayList<>();
        mDbContacts = new ArrayList<>();
        mDataMessageController = WiDataMessageController.getInstance(context);

        mSynchronizeContactsTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "Begin Synchronize Contacts!");
                mDeviceContacts = loadDeviceContacts();
                mDbContacts = loadDbContacts();

                msg = new WiDataMessage();
                Log.d(TAG, "Loaded " + mDeviceContacts.size() + " contacts from phone");


                JSONArray jsonPhones = new JSONArray();
                for(WiContact contact: mDeviceContacts){
                    if(contact.getPhone() != null){
                        String phone = WiUtils.formatPhoneNumber(contact.getPhone());

                        if(!phone.isEmpty()){
                            jsonPhones.put(phone);
                        }
                    }
                }

                msg.put("phones", jsonPhones);

                msg.setOnResponseListener(new WiDataMessage.OnResponseListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Received Reponse from server");
                        synchronizing = false;
                        //mContactListReadyListener.onContactListReady();
                    }
                });

                mDataMessageController.send(msg);

                //msg.send();
                Log.d(TAG, "End Synchronize Contacts!");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
    }

    private WiDataMessage.OnResponseListener onServerContactResponse() {
        return new WiDataMessage.OnResponseListener(){
            @Override
            public void onResponse(JSONObject response) {

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

    private ArrayList<WiContact> loadDeviceContacts(){
        ArrayList<WiContact> contacts = new ArrayList<>();
        ContentResolver resolver = mContext.get().getContentResolver();

        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while(cursor != null && cursor.moveToNext()) {
            contacts.add(new WiContact(
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            ));
        }

        return contacts;
    }

    private ArrayList<WiContact> loadDbContacts() {
        ArrayList<WiContact> contacts = new ArrayList<>();
        SQLiteDatabase db = WiSQLiteDatabase.getInstance(mContext.get()).getReadableDatabase();
        Cursor cur = db.rawQuery("select * from SynchronizedContacts order by name asc", null);

        if (cur != null && cur.moveToFirst()) {
            do {
                contacts.add(new WiContact(
                        cur.getString(cur.getColumnIndex("name")),
                        cur.getString(cur.getColumnIndex("phone"))));
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

    public static boolean hasContactPermissions(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}

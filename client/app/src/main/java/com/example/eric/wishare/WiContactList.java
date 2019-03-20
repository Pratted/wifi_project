package com.example.eric.wishare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;

import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.model.WiContact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WiContactList {

    private static ArrayList<WiContact> mContactList;
    private static HashMap<String, WiContact> mPhoneToContact;
    private static WiContactListLoader mLoader;
    private WeakReference<Context> mContext;
    private static WiContactList mCL;
    private SQLiteDatabase mDatabase;

    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEnteredListener;
    private OnContactListReadyListener mContactListReadyListener;

    public WiContactList(Context context) {
        mContactList = new ArrayList<>();
        mPhoneToContact = new HashMap<>();
        mLoader = new WiContactListLoader();
        mContext = new WeakReference<Context>(context.getApplicationContext());

        refreshContext(context.getApplicationContext());
    }

    public void refreshContext(Context context){
        mContext = new WeakReference<Context>(context.getApplicationContext());
    }

    public void loadAsync(Context context){
        refreshContext(context.getApplicationContext());

        mLoader.execute();
    }


    public ArrayList<WiContact> getWiContacts(){
        return mContactList;
    }

    public int size() {
        return mContactList == null ? 0 : mContactList.size();
    }


    public interface OnContactListReadyListener{
        void onContactListReady(ArrayList<WiContact> contacts);
    }

    public void setOnContactListReadyListener(OnContactListReadyListener listener){
        mContactListReadyListener = listener;
    }

    public void load(){

        WiSQLiteDatabase.getInstance(mContext.get()).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                mDatabase = db;
                Cursor c = mDatabase.rawQuery("SELECT * FROM SynchronizedContacts ORDER BY name asc;", null);
                if (c.moveToFirst()) {
                    WiContact contact = new WiContact(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("phone")));
                    mContactList.add(contact);
                    mPhoneToContact.put(contact.getPhone(), contact);
                    while(c.moveToNext()) {
                        contact = new WiContact(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("phone")));
                        mContactList.add(contact);
                        mPhoneToContact.put(contact.getPhone(), contact);
                    }
                }
                c.close();
            }
        });
/*        ContentResolver resolver = mContext.get().getContentResolver();

        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while(cursor != null && cursor.moveToNext()) {
            WiContact contact = new WiContact(
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            );

            mContactList.add(contact);
            mPhoneToContact.put(contact.getPhone(), contact);
        }*/
    }

    private synchronized void loadDevice(){

    }

    public static synchronized WiContactList getInstance(Context context){
        if (mCL == null){
            mCL = new WiContactList(context.getApplicationContext());
        }
        return mCL;
    }

    private class WiContactListLoader extends AsyncTask<Void, Void, ArrayList<WiContact>> {

        @Override
        protected ArrayList<WiContact> doInBackground(Void... voids) {
            load(); // calling a synchronous function in an async function makes it async
            return mContactList;
        }

        @Override
        protected void onPostExecute(ArrayList<WiContact> contacts) {
            super.onPostExecute(contacts);
            mContactListReadyListener.onContactListReady(contacts);
        }
    }

    public static boolean hasContactPermissions(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}

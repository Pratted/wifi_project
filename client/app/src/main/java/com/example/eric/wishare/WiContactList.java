package com.example.eric.wishare;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WiContactList {

    private ArrayList<WiContact> mContactList;
    private HashMap<String, WiContact> mPhoneToContact;
    private WiContactListLoader mLoader;
    private WeakReference<Context> mContext;

    private OnContactListReadyListener mContactListReadyListener;

    public WiContactList(Context context) {
        mContactList = new ArrayList<>();
        mPhoneToContact = new HashMap<>();
        mLoader = new WiContactListLoader();

        refreshContext(context);
    }

    public void refreshContext(Context context){
        mContext = new WeakReference<>(context);
    }

    public void loadAsync(Context context){
        refreshContext(context);

        mLoader.execute();
    }

    public ArrayList<WiContact> getWiContacts(){
        return mContactList;
    }

    public int size() {
        return mContactList == null ? 0 : mContactList.size();
    }


    interface OnContactListReadyListener{
        void onContactListReady(ArrayList<WiContact> contacts);
    }

    public void setOnContactListReadyListener(OnContactListReadyListener listener){
        mContactListReadyListener = listener;
    }

    public void load(){
        ContentResolver resolver = mContext.get().getContentResolver();

        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while(cursor != null && cursor.moveToNext()) {
            WiContact contact = new WiContact(
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            );

            mContactList.add(contact);
            mPhoneToContact.put(contact.phone, contact);
        }
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

    public static void requestContactPermissions(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 87);
        }
    }
}

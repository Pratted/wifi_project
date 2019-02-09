package com.example.eric.wishare;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;

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


    interface OnContactListReadyListener{
        void onContactListReady(ArrayList<WiContact> contacts);
    }

    public void setOnContactListReadyListener(OnContactListReadyListener listener){
        mContactListReadyListener = listener;
    }

    private class WiContactListLoader extends AsyncTask<Void, Void, ArrayList<WiContact>> {

        @Override
        protected ArrayList<WiContact> doInBackground(Void... voids) {
            ContentResolver resolver = mContext.get().getContentResolver();
            Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            if((cursor != null ? cursor.getCount() : 0) > 0){
                while(cursor != null && cursor.moveToNext()){
                    WiContact contact = new WiContact(
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                        "phone #"
                    );

                    mContactList.add(contact);
                    mPhoneToContact.put(contact.phone, contact);
                }
            }

            return mContactList;
        }

        @Override
        protected void onPostExecute(ArrayList<WiContact> contacts) {
            super.onPostExecute(contacts);

            mContactListReadyListener.onContactListReady(contacts);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasContactPermissions(Context context){

        return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }
}

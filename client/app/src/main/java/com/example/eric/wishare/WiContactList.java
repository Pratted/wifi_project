package com.example.eric.wishare;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;

import com.example.eric.wishare.model.WiContact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WiContactList {

    private ArrayList<WiContact> mContactList;
    private HashMap<String, WiContact> mPhoneToContact;
    private WiContactListLoader mLoader;
    private Context mContext;

    private SQLiteDatabase mDatabase;

    private OnContactListReadyListener mContactListReadyListener;

    public WiContactList(Context context) {
        mContactList = new ArrayList<>();
        mPhoneToContact = new HashMap<>();
        mLoader = new WiContactListLoader();
        mContext = context;

        refreshContext(context);
    }

    public void refreshContext(Context context){
        mContext = context;
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


    public interface OnContactListReadyListener{
        void onContactListReady(ArrayList<WiContact> contacts);
    }

    public void setOnContactListReadyListener(OnContactListReadyListener listener){
        mContactListReadyListener = listener;
    }

    public void load(){

        WiSQLiteDatabase.getInstance(mContext).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                mDatabase = db;
                String[] columns = {"name", "phone", "token"};
                Cursor c = mDatabase.query("synchronizedContacts", columns, null, null, null, null, "name asc");
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
                db.close();
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

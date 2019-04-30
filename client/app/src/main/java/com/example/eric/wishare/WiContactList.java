package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.List;
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
                            Log.e(TAG, "Frick. Server did not respond");
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
        if (WiUtils.isDemoEnabled()){
            HashMap<String, WiContact> demoContacts = mContacts;
            ArrayList<WiContact> demoList = getDemoContacts();
            for (WiContact contact: demoList){
                demoContacts.put(contact.getPhone(), contact);
            }
            return demoContacts;
        }
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


    public boolean hasContact(String phone){
        return mContacts.containsKey(phone);
    }

    public WiContact save(WiContact contact) {
        mContacts.put(contact.getPhone(), contact);

        WiSQLiteDatabase.getInstance(mContext.get()).save(contact);
        return contact;
    }

    public void save(List<WiContact> contacts){
        for(WiContact contact: contacts){
            mContacts.put(contact.getPhone(), contact);
        }

        WiSQLiteDatabase.getInstance(mContext.get()).save(contacts);
    }

    public void deleteNetworkFromAllContacts(String networkID) {
        for(WiContact contact : mContacts.values()) {
            contact.revokeAccess(networkID);
        }
    }

    public ArrayList<WiContact> getDemoContacts(){
        ArrayList<WiContact> demoList = new ArrayList<>();
        demoList.add(new WiContact("Hashim Vasquez","827-529-4463"));
        demoList.add(new WiContact("Allistair Berry","676-283-3298"));
        demoList.add(new WiContact("Zeph Raymond","105-541-8758"));
        demoList.add(new WiContact("Quinn Soto","160-863-4458"));
        demoList.add(new WiContact("Caldwell Mcmahon","716-536-2159"));
        demoList.add(new WiContact("Buckminster Hicks","780-496-0843"));
        demoList.add(new WiContact("Bruno Gomez","882-242-3198"));
        demoList.add(new WiContact("Caleb Saunders","800-133-0614"));
        demoList.add(new WiContact("Kareem Dickerson","798-596-3476"));
        demoList.add(new WiContact("Garrison Spencer","742-408-9418"));
        demoList.add(new WiContact("Felix Cleveland","749-642-7061"));
        demoList.add(new WiContact("Henry Grimes","523-988-0290"));
        demoList.add(new WiContact("Burton Ball","725-415-2492"));
        demoList.add(new WiContact("Dennis Rush","249-812-5102"));
        demoList.add(new WiContact("John Bender","438-938-1520"));
        demoList.add(new WiContact("Colton Waters","291-897-8652"));
        demoList.add(new WiContact("Valentine Clements","927-297-1289"));
        demoList.add(new WiContact("Brennan Mcneil","943-486-5929"));
        demoList.add(new WiContact("Ian Koch","844-255-6842"));
        demoList.add(new WiContact("Connor Kidd","493-661-5981"));
        demoList.add(new WiContact("Jonah Ayala","615-232-2063"));
        demoList.add(new WiContact("Davis Dale","851-431-4378"));
        demoList.add(new WiContact("Gary Barnett","829-287-5596"));
        demoList.add(new WiContact("Damon Hayes","107-284-3406"));
        demoList.add(new WiContact("Macaulay Petersen","900-274-1352"));
        demoList.add(new WiContact("Abbot Gonzalez","227-958-9884"));
        demoList.add(new WiContact("Acton Matthews","167-417-7883"));
        demoList.add(new WiContact("Hu Barton","623-467-8051"));
        demoList.add(new WiContact("Xavier Stanton","845-267-0581"));
        demoList.add(new WiContact("Rogan Lara","706-867-5103"));
        demoList.add(new WiContact("Price Church","187-776-0558"));
        demoList.add(new WiContact("Gannon Zimmerman","143-995-1355"));
        demoList.add(new WiContact("Igor Townsend","624-670-2232"));
        demoList.add(new WiContact("Ishmael Levine","442-516-1339"));
        demoList.add(new WiContact("Holmes Charles","394-329-3224"));
        demoList.add(new WiContact("Ivor Hebert","394-883-5155"));
        demoList.add(new WiContact("Colt Quinn","712-348-2226"));
        demoList.add(new WiContact("Clinton Vinson","241-258-8223"));
        demoList.add(new WiContact("Chester Foster","925-183-5131"));
        demoList.add(new WiContact("Kadeem Marshall","882-656-1341"));
        demoList.add(new WiContact("Arthur Ryan","705-651-5516"));
        demoList.add(new WiContact("Xander Hawkins","498-343-6382"));
        demoList.add(new WiContact("Sawyer Preston","920-317-5325"));
        demoList.add(new WiContact("Ross Barnes","253-762-0542"));
        demoList.add(new WiContact("Thor Gardner","688-130-6346"));
        demoList.add(new WiContact("Dillon Carson","104-479-5724"));
        demoList.add(new WiContact("Edward Martinez","851-430-9899"));
        demoList.add(new WiContact("Uriel Saunders","737-328-4443"));
        demoList.add(new WiContact("Price Norton","151-781-7329"));
        demoList.add(new WiContact("Jermaine Hamilton","279-312-6500"));
        demoList.add(new WiContact("Rahim Chase","406-553-4884"));
        demoList.add(new WiContact("Lucius Sharpe","591-262-4540"));
        demoList.add(new WiContact("Thane Bass","569-160-1703"));
        demoList.add(new WiContact("Arsenio Ortega","502-806-3004"));
        demoList.add(new WiContact("Jared Leach","132-502-8435"));
        demoList.add(new WiContact("Brian Gill","502-638-5120"));
        demoList.add(new WiContact("Darius Dennis","289-529-4497"));
        demoList.add(new WiContact("Wing Gentry","820-727-0008"));
        demoList.add(new WiContact("Rahim Allen","471-136-2323"));
        demoList.add(new WiContact("Kadeem Martin","729-879-8342"));
        demoList.add(new WiContact("Philip Humphrey","497-224-2206"));
        demoList.add(new WiContact("Scott Shaffer","788-333-0581"));
        demoList.add(new WiContact("Ulric Barry","466-801-8187"));
        demoList.add(new WiContact("Ferdinand Cantrell","566-319-6219"));
        demoList.add(new WiContact("Tobias Sullivan","914-609-9744"));
        demoList.add(new WiContact("Todd Floyd","980-108-8074"));
        demoList.add(new WiContact("Stephen Baker","463-548-3675"));
        demoList.add(new WiContact("Austin Bryant","456-186-9477"));
        demoList.add(new WiContact("Kieran Price","226-409-0575"));
        demoList.add(new WiContact("Zeus Reeves","828-443-2902"));
        demoList.add(new WiContact("Tanek Long","235-883-4656"));
        demoList.add(new WiContact("Jakeem Whitley","927-743-1509"));
        demoList.add(new WiContact("Raymond Zamora","856-782-1765"));
        demoList.add(new WiContact("Elmo Mendez","370-773-1519"));
        demoList.add(new WiContact("Hasad Thornton","700-960-1016"));
        demoList.add(new WiContact("Conan Mendoza","998-546-6620"));
        demoList.add(new WiContact("Malik Baker","265-395-0794"));
        demoList.add(new WiContact("Oscar Lancaster","301-486-8501"));
        demoList.add(new WiContact("Ashton Contreras","926-181-4894"));
        demoList.add(new WiContact("Kaseem Bradford","735-768-3647"));
        demoList.add(new WiContact("Cedric Mason","995-976-7318"));
        demoList.add(new WiContact("Stewart Mullen","430-675-7825"));
        demoList.add(new WiContact("Reuben Chaney","449-158-0475"));
        demoList.add(new WiContact("Benedict Calhoun","520-611-8964"));
        demoList.add(new WiContact("Dustin Wise","977-661-2797"));
        demoList.add(new WiContact("Jermaine Lewis","372-169-3524"));
        demoList.add(new WiContact("Gregory Mcneil","597-642-9509"));
        demoList.add(new WiContact("Joshua Stanley","891-442-6243"));
        demoList.add(new WiContact("Neville Graves","475-777-2009"));
        demoList.add(new WiContact("Price Anthony","605-257-3904"));
        demoList.add(new WiContact("Garrett Conway","483-299-4928"));
        demoList.add(new WiContact("Philip Mccarthy","314-709-6011"));
        demoList.add(new WiContact("Austin Rojas","161-466-4002"));
        demoList.add(new WiContact("Hashim Salas","928-173-8584"));
        demoList.add(new WiContact("Theodore Dodson","561-446-0914"));
        demoList.add(new WiContact("Kirk Kirkland","707-961-1876"));
        demoList.add(new WiContact("Ishmael Mccarty","793-997-9242"));
        demoList.add(new WiContact("Holmes Floyd","452-932-0518"));
        demoList.add(new WiContact("Abbot Marks","205-809-6560"));
        demoList.add(new WiContact("Ulysses Ware","344-442-2026"));
        return demoList;
    }
}

package com.example.eric.wishare;

import android.Manifest;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.dialog.WiInvitationListDialog;
import com.example.eric.wishare.dialog.WiManageContactsDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.view.WiConfiguredNetworkListView;
import com.example.eric.wishare.view.WiMyInvitationsButton;
import com.example.eric.wishare.dialog.*;
import com.example.eric.wishare.model.*;
import com.example.eric.wishare.view.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkList;

    private Button btnShowNotification;

    private WiMyInvitationsButton btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiManageContactsDialog mContactListDialog;
    private WiNetworkManager mNetworkManager;

    private SQLiteDatabase mDatabase;
    private BroadcastReceiver mWifiConnectedReceiver;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);




        // contact permission accepted..
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            mContactListDialog = new WiManageContactsDialog(MainActivity.this, btnManageContacts);


            mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
                @Override
                public void onContactSelected(WiContact contact) {
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
            });
            addContacts(this);

            //need the contact list loaded before showing the dialog. do this SYNCHRONOUSLY
            mContactListDialog.loadContacts();
            mContactListDialog.refresh(this);
            //mContactListDialog.show();

            mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
            mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String phone = formatPhoneNumber("1231231234");
        String phone2 = formatPhoneNumber("(123) 123-1234");
        String phone3 = formatPhoneNumber("123-123-1234");
        String phone4 = formatPhoneNumber("+11231231234");

        System.out.println("Called oncreate...");

        //plzFirebase();

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        FirebaseApp.initializeApp(this);

        btnShowNotification = findViewById(R.id.btn_show_notification);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        mConfiguredNetworkList = findViewById(R.id.configured_network_list);


        mInvitationListDialog = new WiInvitationListDialog(this, btnMyInvitations);
        WiContact contact1 = new WiContact("Eric Pratt", "1");
        WiContact contact2 = new WiContact("Eric Pratt", "2");
        WiContact contact3 = new WiContact("Eric Pratt", "3");
        WiContact contact4 = new WiContact("Eric Pratt", "+12223334444");
        mInvitationListDialog.add(new WiInvitation("belkin-622", contact1, "Never", "127 hours", "10GB"));
        mInvitationListDialog.add(new WiInvitation("belkin-048", contact2, "2/28/2019", "36 hours", "5GB"));
        mInvitationListDialog.add(new WiInvitation("home-255", contact3, "3/15/2019", "Never", "None"));
        mInvitationListDialog.add(new WiInvitation("home-200", contact4, "3/15/2019", "24 hours", "3GB"));


        /**
         need contact permission to build the ContactListDialog
         if contact permission is not granted, the user will be prompted on Manage Contacts button click
         if the user grants permission, the callback onPermissionResult() will construct the WiContactListDialog
         **/
        if(WiContactList.hasContactPermissions(this)){
            //addContacts(MainActivity.this);
            mContactListDialog = new WiManageContactsDialog(this, btnManageContacts);
            mContactListDialog.loadContactsAsync(); // start loading the contacts asynchronously.

            mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
                @Override
                public void onContactSelected(WiContact contact){
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
            });

            mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
            mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());
        }
        else{
            // if there are no permissions, make onClick for the button request permissions...
            btnManageContacts.setOnClickListener(requestContactPermissions());
            btnAddNetwork.setOnClickListener(requestContactPermissions());

        }

        mNetworkManager = WiNetworkManager.getInstance(this);

        final WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"305\"";
        config.preSharedKey = "\"eightautumn\"";

        mNetworkManager.addNetwork(config);
        btnShowNotification.setOnClickListener(sendNotification());

    }

    private View.OnClickListener sendNotification(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Thread thread = new Thread(new Runnable(){
                    public void run() {
                        WiDataMessage.send();
                }});

                thread.start();

                // Wifi
                mNetworkManager.testConnection("305");

                final MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this).progress(true, 100).content("Testing connection...").show();

                mNetworkManager.setOnTestConnectionCompleteListener(new WiNetworkManager.OnTestConnectionCompleteListener() {
                    @Override
                    public void onTestConnectionComplete(boolean success) {
                        dialog.dismiss();
                        new MaterialDialog.Builder(MainActivity.this).title("Connection successful!").positiveText("Ok").show();
                    }
                });

                WiNotificationInviteReceived notification = new WiNotificationInviteReceived(MainActivity.this, "Test Notification", "This is test description");
                notification.show();
            }
        };
    }

    private View.OnClickListener requestContactPermissions(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 87);
                }
            }
        };
    }

    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEntered(){
        return new WiAddNetworkDialog.OnPasswordEnteredListener() {
            @Override
            public void OnPasswordEntered(WiConfiguration config) {
                mConfiguredNetworkList.addView(config);
            }
        };
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

//        mAddNetworkDialog.refresh(this);
        mInvitationListDialog.refresh(this);

        if(mContactListDialog != null) {
            mContactListDialog.refresh(this);
        }


        if(getIntent().getStringExtra("inviteNetwork") != null){
            Intent intent = getIntent();

            String networkName = intent.getStringExtra("network_name");

            String dataLimit = intent.getStringExtra("data_limit");
            String expires = intent.getStringExtra("expires");

            String temp = intent.getStringExtra("owner");
            String name = "";
            String phone = "";
            String other = "";

            if(temp != null){
                try {
                    JSONObject t2 = new JSONObject(temp);
                    name = t2.getString("name");
                    phone = t2.getString("phone");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                networkName = "Sample Network";
                name = "Joe Schmoe";
                phone = "12345";
                expires = "Never";
                other = "";
                dataLimit = "5 Gb";
            }


            intent.removeExtra("inviteNetwork");

            WiInvitation inv = new WiInvitation(networkName, new WiContact(name, phone), expires, other, dataLimit);

            /*
            WiInvitation invitation = null;

            for (WiInvitation invite: mInvitationListDialog.getInvitations()){
                if (invite.getNetworkName().equals(networkName))
                    invitation = invite;
            }
            */


            if (inv != null){
                WiInvitationAcceptDeclineDialog mAcceptDeclineDialog = new WiInvitationAcceptDeclineDialog(this, inv);
                mAcceptDeclineDialog.show();
            }
            else{
                Toast.makeText(this, "Error: Invitation expired or does not exist", Toast.LENGTH_LONG).show();
            }
        }


        plzFirebase();
    }

    private void plzFirebase(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        Log.d(TAG, "The token is: " + token);
                        System.out.println(token);
                //Toast.makeText(MainActivity.this, "The token is: " + token , Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void addContacts(final Context context){
        WiSQLiteDatabase.getInstance(context).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);


                mDatabase = db;

                while(cursor != null && cursor.moveToNext()) {

                    ContentValues values = new ContentValues();
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = formatPhoneNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    values.put("name", name);
                    values.put("phone", phone);
                    values.put("token", "iAmAToken");
                    if (phone.contains("+")){
                        System.out.println("foo");
                    }
                    mDatabase.insert("SynchronizedContacts", null, values);

                }

                cursor.close();
            }
        });
    }
    public static String formatPhoneNumber(String phone){
        String revised = "";

        /***********************************************************************************
         Source - https://stackoverflow.com/a/16702965
         ************************************************************************************/
        Pattern regex = Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
        Matcher matcher = regex.matcher(phone);

        if(matcher.matches()){
            revised = matcher.group(2) + "-" + matcher.group(3) + "-" + matcher.group(4);
        }
        return revised;
    }

}

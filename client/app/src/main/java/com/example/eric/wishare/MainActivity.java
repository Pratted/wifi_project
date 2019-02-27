package com.example.eric.wishare;

import android.Manifest;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // contact permission accepted..
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mContactListDialog = new WiManageContactsDialog(this, btnManageContacts);

            mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
                @Override
                public void onContactSelected(WiContact contact) {
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
            });

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


        System.out.println("Called oncreate...");

        //plzFirebase();

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if(savedInstanceState != null){
            int x = 0;
            x++;
        }



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


        btnShowNotification.setOnClickListener(sendNotification());

    }
    private View.OnClickListener sendNotification(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){
                WiNotificationInviteReceived notification = new WiNotificationInviteReceived(MainActivity.this, "Title", "Description");
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


        int x = 0;
        x++;

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

            try {
                JSONObject t2 = new JSONObject(temp);
                name = t2.getString("name");
                phone = t2.getString("phone");

            } catch (JSONException e) {
                e.printStackTrace();
            }


            WiInvitation inv = new WiInvitation(networkName, new WiContact(name, phone), expires, "", dataLimit);
            
            int x = 0;
            x++;

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
}

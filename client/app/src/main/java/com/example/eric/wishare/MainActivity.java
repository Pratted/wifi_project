package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.dialog.WiInvitationListDialog;
import com.example.eric.wishare.dialog.WiManageContactsDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.view.WiConfiguredNetworkListView;
import com.example.eric.wishare.view.WiMyInvitationsButton;
import com.google.firebase.FirebaseApp;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkListView;
    private WiNetworkManager mConfiguredNetworks;

    private Button btnShowNotification;

    private WiMyInvitationsButton btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiManageContactsDialog mContactListDialog;


    @SuppressLint("ApplySharedPref")
    private void registerDevice(){
        if(!WiUtils.isDeviceRegistered()){
            WiMessagingService.registerDevice(
                    WiUtils.getDeviceToken(),
                    WiUtils.getDevicePhone()
            );

            WiSharedPreferences.putBoolean("registered", true);
            WiSharedPreferences.save();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        WiSharedPreferences.initialize(this);

        registerDevice();

        WiContactList.getInstance(this).synchronizeContacts(); // async...

        System.out.println("DEVICE TOKEN IS: ");
        System.out.println(WiUtils.getDeviceToken());

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        btnShowNotification = findViewById(R.id.btn_show_notification);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        mConfiguredNetworkListView = findViewById(R.id.configured_network_list);
        mConfiguredNetworks = new WiNetworkManager(this);
        for (WiConfiguration configuredNetwork : mConfiguredNetworks.getConfiguredNetworks(this)){
            mConfiguredNetworkListView.addView(configuredNetwork);
        }

        mInvitationListDialog = new WiInvitationListDialog(this, btnMyInvitations);

        /*
        WiContact contact1 = new WiContact("Eric Pratt", "1");
        WiContact contact2 = new WiContact("Eric Pratt", "2");
        WiContact contact3 = new WiContact("Eric Pratt", "3");
        WiContact contact4 = new WiContact("Eric Pratt", "+12223334444");
        mInvitationListDialog.add(new WiInvitation("belkin-622", contact1, "Never", "127 hours", "10GB"));
        mInvitationListDialog.add(new WiInvitation("belkin-048", contact2, "2/28/2019", "36 hours", "5GB"));
        mInvitationListDialog.add(new WiInvitation("home-255", contact3, "3/15/2019", "Never", "None"));
        mInvitationListDialog.add(new WiInvitation("home-200", contact4, "3/15/2019", "24 hours", "3GB"));
        */

        mContactListDialog = new WiManageContactsDialog(this, btnManageContacts);

        mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
            @Override
            public void onContactSelected(WiContact contact){
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                intent.putExtra("contact", contact);
                System.out.println("STARTING CONTACT ACTIVITY");
                startActivity(intent);
            }
        });

        mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
        mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());

        btnShowNotification.setOnClickListener(sendNotification());
    }

    private View.OnClickListener sendNotification(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){

                /*
                // Wifi
                WiNetworkManager mNetworkManager = WiNetworkManager.getInstance(MainActivity.this);
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
                */
            }
        };
    }


    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEntered(){
        return new WiAddNetworkDialog.OnPasswordEnteredListener() {
            @Override
            public void OnPasswordEntered(WiConfiguration config) {
                mConfiguredNetworkListView.addView(config);
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

            /*
            WiInvitation inv = new WiInvitation(networkName, new WiContact(name, phone), expires, other, dataLimit);


            if (inv != null){
                WiInvitationAcceptDeclineDialog mAcceptDeclineDialog = new WiInvitationAcceptDeclineDialog(this, inv);
                mAcceptDeclineDialog.show();
            }
            else{
                Toast.makeText(this, "Error: Invitation expired or does not exist", Toast.LENGTH_LONG).show();
            }
            */
        }
    }
}

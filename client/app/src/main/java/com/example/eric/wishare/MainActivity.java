package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.example.eric.wishare.model.WiInvitationNotification;
import com.example.eric.wishare.model.WiNotification;
import com.example.eric.wishare.view.WiConfiguredNetworkListView;
import com.example.eric.wishare.view.WiMyInvitationsButton;
import com.google.firebase.FirebaseApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("key");
            //tvStatus.setText(message);
             Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(WiUtils.ACTIVITY_MAIN));

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


        mInvitationListDialog = new WiInvitationListDialog(this, btnMyInvitations);

        ArrayList<WiInvitation> invitations = WiSQLiteDatabase.getInstance(this).loadAllInvitations();
        for(WiInvitation inv: invitations){
            mInvitationListDialog.add(inv);
        }

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
            public void onClick(View v) {

                WiInvitationNotification notification = new WiInvitationNotification(MainActivity.this,
                        new WiInvitation("YOYOMA", "AddyK", "tomorrow", "5", "500"),
                        WiNotification.SILENT_NOTIFICATION);
                notification.show();


            }
        };
    }


    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEntered(){
        return new WiAddNetworkDialog.OnPasswordEnteredListener() {
            @Override
            public void OnPasswordEntered(WiConfiguration config) {
                mConfiguredNetworkListView.addConfiguredNetwork(config);
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

        Intent intent = getIntent();

        if(intent != null){
            if(intent.hasExtra("invitation")){
                Log.d(TAG, "PREPARNG INVITATION");

                WiInvitation invitation = intent.getParcelableExtra("invitation");
                mInvitationListDialog.add(invitation);
                new WiInvitationAcceptDeclineDialog(this, invitation).show();
                intent.removeExtra("invitation");
            }
        }

//        mAddNetworkDialog.refresh(this);
        mInvitationListDialog.refresh(this);

        if(mContactListDialog != null) {
            mContactListDialog.refresh(this);
        }
    }
}

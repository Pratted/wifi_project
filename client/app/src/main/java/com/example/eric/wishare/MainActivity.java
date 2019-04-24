package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.dialog.WiDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.dialog.WiInvitationListDialog;
import com.example.eric.wishare.dialog.WiManageContactsDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.messaging.TestConnectionMessage;
import com.example.eric.wishare.view.WiConfiguredNetworkListView;
import com.example.eric.wishare.view.WiMyInvitationsButton;
import com.google.firebase.FirebaseApp;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkListView;

    private WiMyInvitationsButton btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiManageContactsDialog mManageContactsDialog;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d(TAG, "MAIN ACTIVITY RECEIVED NEW BROADCAST MESSAGE");

            if(intent.hasExtra("invitation_receieved")){
                Log.d(TAG, "Invitation received, updadating My Invitations button...");
                btnMyInvitations.setInvitationCount(WiInvitationList.getInstance(MainActivity.this).size());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_MAIN);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Log.d(TAG, "Creating Main Activity");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(WiUtils.ACTIVITY_MAIN));

        FirebaseApp.initializeApp(this);
        WiSharedPreferences.initialize(this);

        WiContactList.getInstance(this).synchronizeContacts(); // async...

        Log.d(TAG, "DEVICE TOKEN IS: ");
        Log.d(TAG, WiUtils.getDeviceToken());

        mConfiguredNetworkListView = findViewById(R.id.configured_network_list);

        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        btnMyInvitations.setInvitationCount(0); // initialize to 0 to prevent the red circle from showing with no invites

        mAddNetworkDialog = new WiAddNetworkDialog(this);
        mManageContactsDialog = new WiManageContactsDialog(this);
        mInvitationListDialog = new WiInvitationListDialog(this);

        mAddNetworkDialog.setOnNetworkReadyListener(onNetworkReady());
        mManageContactsDialog.setOnContactSelectedListener(startContactActivity());
        //mInvitationListDialog.setOnInvitationsUpdatedListener(refreshMyInvitationsButtonCounter());

        btnAddNetwork.setOnClickListener(showWiDialog(mAddNetworkDialog));
        btnManageContacts.setOnClickListener(showWiDialog(mManageContactsDialog));
        btnMyInvitations.setOnClickListener(showWiDialog(mInvitationListDialog));
    }

    private View.OnClickListener showWiDialog(final WiDialog dialog){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        };
    }

    private WiAddNetworkDialog.OnNetworkReadyListener onNetworkReady(){
        return new WiAddNetworkDialog.OnNetworkReadyListener() {
            @Override
            public void onNetworkReady(WiConfiguration configuration) {
                WiSQLiteDatabase.getInstance(MainActivity.this).insert(configuration);

                // call configureNetwork here because the host is configuring it.
                WiNetworkManager.getInstance(MainActivity.this).configureNetwork(configuration);

                mConfiguredNetworkListView.addConfiguredNetwork(configuration);
            }
        };
    }

    private WiManageContactsDialog.OnContactSelectedListener startContactActivity(){
        return new WiManageContactsDialog.OnContactSelectedListener() {
            @Override
            public void onContactSelected(WiContact contact) {
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                intent.putExtra("contact", contact);
                System.out.println("STARTING CONTACT ACTIVITY");
                startActivity(intent);
            }
        };
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_MAIN);
    }

    private CountDownTimer mTimer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            Toast.makeText(getApplicationContext(), "Failed to connect to server.", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        final TestConnectionMessage msg = new TestConnectionMessage(getApplicationContext()){
            @Override
            public void onResponse(JSONObject response) {
                mTimer.cancel();
            }

        };
        mTimer.start();
        WiDataMessageController.getInstance(this).send(msg);

        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_MAIN);

        Intent intent = getIntent();

        if(intent != null){
            if(intent.hasExtra("invitation")){
                displayInvitation(intent);
            }
        }

        btnMyInvitations.setInvitationCount(WiInvitationList.getInstance(this).size());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "HOLY FUCK");

        if(intent != null){
            if(intent.hasExtra("invitation")){
                displayInvitation(intent);
            }
        }
    }

    private void displayInvitation(Intent intent){
        Log.d(TAG, "Preparing to display invitation");

        String ssid = intent.getStringExtra("invitation");
        WiInvitation invitation = WiInvitationList.getInstance(this).getInvitation(ssid);

        new WiInvitationAcceptDeclineDialog(this, invitation).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}

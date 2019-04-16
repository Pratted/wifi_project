package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.example.eric.wishare.view.WiConfiguredNetworkListView;
import com.example.eric.wishare.view.WiMyInvitationsButton;
import com.google.firebase.FirebaseApp;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkListView;

    private Button btnShowNotification;

    private WiMyInvitationsButton btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiManageContactsDialog mManageContactsDialog;

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

    private Toolbar myToolbar;

    private WiTimerService mTimerService;
    private Intent mTimerServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Log.d(TAG, "Creating Main Activity");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(WiUtils.ACTIVITY_MAIN));

        FirebaseApp.initializeApp(this);
        WiSharedPreferences.initialize(this);

        mTimerService = new WiTimerService(getApplicationContext());
        mTimerServiceIntent = new Intent(getApplicationContext(), mTimerService.getClass());

        if (!isMyServiceRunning(mTimerService.getClass())) {
            startService(mTimerServiceIntent);
        }

        registerDevice();

        WiContactList.getInstance(this).synchronizeContacts(); // async...

        Log.d(TAG, "DEVICE TOKEN IS: ");
        Log.d(TAG, WiUtils.getDeviceToken());

        mConfiguredNetworkListView = findViewById(R.id.configured_network_list);

        btnShowNotification = findViewById(R.id.btn_show_notification);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        btnMyInvitations.setInvitationCount(0); // initialize to 0 to prevent the red circle from showing with no invites

        mAddNetworkDialog = new WiAddNetworkDialog(this);
        mManageContactsDialog = new WiManageContactsDialog(this);
        mInvitationListDialog = new WiInvitationListDialog(this);

        mAddNetworkDialog.setOnNetworkReadyListener(onNetworkReady());
        mManageContactsDialog.setOnContactSelectedListener(startContactActivity());
        mInvitationListDialog.setOnInvitationsUpdatedListener(refreshMyInvitationsButtonCounter());

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

    private WiInvitationListDialog.OnInvitationsUpdatedListener refreshMyInvitationsButtonCounter(){
        return new WiInvitationListDialog.OnInvitationsUpdatedListener() {
            @Override
            public void onInvitationsUpdated(List<WiInvitation> invitations) {
                if(invitations != null){
                    btnMyInvitations.setInvitationCount(invitations.size());
                }
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

        WiSharedPreferences.putString("current_activity", WiUtils.ACTIVITY_MAIN);
        WiSharedPreferences.save();

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
    }

    @Override
    protected void onPause() {
        super.onPause();

        WiSharedPreferences.putString("current_activity", "");
        WiSharedPreferences.save();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "HOLY FUCK");

        if(intent != null){
            if(intent.hasExtra("invitation")){
                Log.d(TAG, "PREPARNG INVITATION");

                WiInvitation invitation = intent.getParcelableExtra("invitation");
                mInvitationListDialog.add(invitation);
                new WiInvitationAcceptDeclineDialog(this, invitation).show();
                intent.removeExtra("invitation");
            }
        }
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

    @Override
    protected void onDestroy() {
        stopService(mTimerServiceIntent);
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}

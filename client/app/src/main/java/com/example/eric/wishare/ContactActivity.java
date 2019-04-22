package com.example.eric.wishare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.dialog.WiInviteContactToNetworkDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.messaging.WiRevokeAccessDataMessage;
import com.example.eric.wishare.view.WiContactSharedNetworkListView;

public class ContactActivity extends AppCompatActivity {
    private LinearLayout mHiddenLayout;
    private String TAG = "ContactActivity";

    private WiContact mContact;

    private Button btnInviteContactToNetwork;
    private Button btnRevokeSelectiveAccess;
    private Button btnHideCheckBoxes;

    private WiInviteContactToNetworkDialog mInviteToNetwork;

    private WiContactSharedNetworkListView mContactSharedNetworkList;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_CONTACT);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mConfigReceiver, new IntentFilter(WiUtils.ACTIVITY_CONTACT));

        mContact = getIntent().getExtras().getParcelable("contact");

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(mContact.getName());
        setSupportActionBar(mToolbar);

        mContactSharedNetworkList = findViewById(R.id.contactNetworkList);
        mHiddenLayout = findViewById(R.id.ll_hidden_btn_layout);

        btnRevokeSelectiveAccess = findViewById(R.id.btn_revoke_selective_access);
        btnHideCheckBoxes = findViewById(R.id.btn_hide_checkboxes);
        mContactSharedNetworkList.setOnCheckBoxVisibleListener(onCheckBoxVisible());
        mContactSharedNetworkList.populateNetworks(this, mContact);

        btnInviteContactToNetwork = findViewById(R.id.btn_invite_contact_to_network);

        findViewById(R.id.btn_revoke_all_access).setOnClickListener(revokeAllAccess());
        mInviteToNetwork = new WiInviteContactToNetworkDialog(this, mContact, btnInviteContactToNetwork);
    }

    private BroadcastReceiver mConfigReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            WiConfiguration config = intent.getParcelableExtra("config");
            //tvStatus.setText(message);
            mInviteToNetwork.setOnInviteAcceptListener(onInviteAccept());
            mInviteToNetwork.inviteIsAccepted(config);
            String out = "BR: " + config.getSSIDNoQuotes();
            Toast.makeText(ContactActivity.this, out, Toast.LENGTH_LONG).show();
            reload();
        }
    };

    private WiInviteContactToNetworkDialog.OnInviteAcceptListener onInviteAccept() {
        return new WiInviteContactToNetworkDialog.OnInviteAcceptListener() {
            @Override
            public void onInviteAccept(WiConfiguration network) {
                mContactSharedNetworkList.addSharedNetwork(network);
            }
        };
    }

    private void reload() {
        for(WiConfiguration config : mContact.getPermittedNetworks()) {
            if(!mContactSharedNetworkList.contains(config)) {
                mContactSharedNetworkList.addSharedNetwork(config);
            }
        }
        Toast.makeText(getApplicationContext(), "Networks refreshed.", Toast.LENGTH_SHORT).show();
    }

    public MaterialDialog.SingleButtonCallback removeAllNetworks(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                mContactSharedNetworkList.hideAllNetworks();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    for (WiConfiguration config: mContact.getPermittedNetworks()){
                        WiRevokeAccessDataMessage msg = new WiRevokeAccessDataMessage(config, mContact.getPhone());
                        WiDataMessageController.getInstance(getApplicationContext()).send(msg);
                        WiSQLiteDatabase.getInstance(getApplicationContext()).delete(config, mContact.getPhone());
                    }
                }
            }
        };
    }

    private View.OnClickListener revokeAllAccess() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(ContactActivity.this)
                        .title("Revoke All Network Accesses")
                        .content("Are you sure you want to revoke " + mContact.getName() +
                                "'s access to all shared networks?" +
                                " This action is permanent.")
                        .negativeText("Cancel")
                        .positiveText("Yes")
                        .onPositive(removeAllNetworks())
                        .show();
            }
        };
    }

    private WiContactSharedNetworkListView.OnCheckBoxVisibleListener onCheckBoxVisible() {
        return new WiContactSharedNetworkListView.OnCheckBoxVisibleListener() {

            @Override
            public void onCheckBoxVisible() {
                mHiddenLayout.setVisibility(View.VISIBLE);
                btnHideCheckBoxes.setVisibility(View.VISIBLE);
                btnHideCheckBoxes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContactSharedNetworkList.hideAllCheckBoxes();
                        btnHideCheckBoxes.setVisibility(View.GONE);
                        btnRevokeSelectiveAccess.setVisibility(View.GONE);
                        mHiddenLayout.setVisibility(View.GONE);
                    }
                });

                btnRevokeSelectiveAccess.setVisibility(View.VISIBLE);
                btnRevokeSelectiveAccess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove selected networks
//                        new MaterialDialog.Builder(getContext())
//                                .title("Revoke Network Access")
//                                .content("Are you sure you want to revoke " + networks + " networks?" +
//                                        " This action is permanent.")
//                                .negativeText("Cancel")
//                                .positiveText("Yes")
//                                .onPositive(mContactSharedNetworkList.hideSelectedNetworks())
//                                .show();

                        mContactSharedNetworkList.hideSelectedNetworks();
                        mContactSharedNetworkList.hideAllCheckBoxes();
                        btnHideCheckBoxes.setVisibility(View.GONE);
                        btnRevokeSelectiveAccess.setVisibility(View.GONE);
                        mHiddenLayout.setVisibility(View.GONE);
                    }
                });
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                startActivity(new Intent(ContactActivity.this, SettingsActivity.class));
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

/*    private WiContactListDialog.OnContactSelectedListener onContactSelected(){
        return new WiContactListDialog.OnContactSelectedListener() {
            @Override
            public void onContactSelected(WiContact contact) {
                ((TextView)findViewById(R.id.tv_contact_name)).setText(contact.getName());
                ((TextView)findViewById(R.id.tv_contact_number)).setText(contact.getPhone());
                Toast.makeText(ContactActivity.this, "Hello", Toast.LENGTH_LONG).show();
            }
        };
    }*/

    @Override
    public void onResume(){
        super.onResume();
        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_CONTACT);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "Received a new intent!");

        if(intent != null){
            if(intent.hasExtra("invitation")){
                Log.d(TAG, "Preparing invitation dialog...");
                WiInvitation invitation = intent.getParcelableExtra("invitation");
                new WiInvitationAcceptDeclineDialog(this, invitation).show();
            }
        }
    }
}

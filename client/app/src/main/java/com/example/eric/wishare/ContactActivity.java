package com.example.eric.wishare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.dialog.WiInviteContactToNetworkDialog;
import com.example.eric.wishare.dialog.WiRevokeAccessDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.messaging.WiRevokeAccessDataMessage;
import com.example.eric.wishare.view.WiContactSharedNetworkListView;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {
    private ScrollView mNetworkScrollView;
    private LinearLayout mHiddenLayout;

    private WiContact mContact;

    private Button btnInviteContactToNetwork;
    private Button btnRevokeSelectiveAccess;
    private Button btnHideCheckBoxes;

    private ArrayList<WifiConfiguration> mNetworks;

    private WiInviteContactToNetworkDialog mInviteToNetwork;
    private WiRevokeAccessDialog mRevokeAccessDialog;

    private WiContactSharedNetworkListView mContactSharedNetworkList;
    private WiNetworkManager mNetworkManager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

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

        for(WiConfiguration config: mContact.getPermittedNetworks()) {
            mContactSharedNetworkList.addSharedNetwork(config);
        }

        btnInviteContactToNetwork = findViewById(R.id.btn_invite_contact_to_network);

        findViewById(R.id.btn_revoke_all_access).setOnClickListener(revokeAllAccess());
//        findViewById(R.id.contactNetworkList).setOnClickListener(reload());
        mInviteToNetwork = new WiInviteContactToNetworkDialog(this, mContact, btnInviteContactToNetwork);
        mInviteToNetwork.setOnInviteClickListener(onInviteClick());
    }

    private BroadcastReceiver mConfigReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            WiConfiguration config = intent.getParcelableExtra("config");
            //tvStatus.setText(message);
            reload();
        }
    };

    private WiInviteContactToNetworkDialog.OnInviteClickListener onInviteClick() {
        return new WiInviteContactToNetworkDialog.OnInviteClickListener() {
            @Override
            public void onInviteClick(WiConfiguration network) {
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

    }
}

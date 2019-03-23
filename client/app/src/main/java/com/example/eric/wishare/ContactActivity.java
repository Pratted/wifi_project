package com.example.eric.wishare;

import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.dialog.WiInviteContactToNetworkDialog;
import com.example.eric.wishare.dialog.WiRevokeAccessDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.view.WiContactSharedNetworkListView;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {
    private ScrollView mNetworkScrollView;
    private LinearLayout mHiddenLayout;

    private WiContact mContact;

    private Button btnRevokeAllAccess;
    private Button btnInviteContactToNetwork;
    private Button btnRevokeSelectiveAccess;
    private Button btnHideCheckBoxes;

    private ArrayList<WifiConfiguration> mNetworks;

    private WiInviteContactToNetworkDialog mInviteToNetwork;
    private WiRevokeAccessDialog mRevokeAccessDialog;

    private WiContactSharedNetworkListView mContactSharedNetworkList;

    private WiNetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mContact = getIntent().getExtras().getParcelable("contact");

        try {
            String title = "";
            title += mContact.getName();
            if(!mContact.getPhone().isEmpty()) {
                title += ": " + mContact.getPhone();
            }
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            System.out.println("SET TITLE NULL POINTER IN CONTACT ACTIVITY");
        }

//        ((TextView) findViewById(R.id.tv_contact_number)).setText(contact.getPhone());
//        ((TextView) findViewById(R.id.tv_permitted_networks)).setText("Networks " + contact.getName() + " has access to:");

//        mNetworkManager = WiNetworkManager.getInstance();

        mContactSharedNetworkList = findViewById(R.id.contactNetworkList);
        mHiddenLayout = findViewById(R.id.ll_hidden_btn_layout);

        btnRevokeSelectiveAccess = findViewById(R.id.btn_revoke_selective_access);
        btnHideCheckBoxes = findViewById(R.id.btn_hide_checkboxes);
        mContactSharedNetworkList.setOnCheckBoxVisibleListener(onCheckBoxVisible());

        mNetworks = WiNetworkManager.getConfiguredNetworks(this);

        ArrayList<WiConfiguration> networkList = new ArrayList<>();

        for (WifiConfiguration configuration : mNetworks) {
            networkList.add(new WiConfiguration(configuration, ""));
        }

//        for(WiConfiguration config : networkList) {
//            mContactSharedNetworkList.addSharedNetwork(config);
//        }

        btnRevokeAllAccess = findViewById(R.id.btn_revoke_all_access);
        btnInviteContactToNetwork = findViewById(R.id.btn_invite_contact_to_network);

//        mRevokeAccessDialog = new WiRevokeAccessDialog(this, btnRevokeAllAccess);
        btnRevokeAllAccess.setOnClickListener(revokeAllAccess());
        mInviteToNetwork = new WiInviteContactToNetworkDialog(this, mContact, btnInviteContactToNetwork);
        mInviteToNetwork.setOnInviteClickListener(onInviteClick());
    }

    private WiInviteContactToNetworkDialog.OnInviteClickListener onInviteClick() {
        return new WiInviteContactToNetworkDialog.OnInviteClickListener() {
            @Override
            public void onInviteClick(List<WiConfiguration> networks) {
                for(WiConfiguration config : networks) {
                    mContactSharedNetworkList.addSharedNetwork(config);
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
                        .onPositive(mContactSharedNetworkList.hideAllNetworks())
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
}

package com.example.eric.wishare;

import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {
    private ScrollView mNetworkScrollView;
    private LinearLayout mHiddenLayout;

    private Button btnRevokeAllAccess;
    private Button btnAddContactToNetwork;
    private Button btnRevokeSelectiveAccess;
    private Button btnHideCheckBoxes;

    private ArrayList<WifiConfiguration> mNetworks;

    private WiAddContactToNetworkDialog mAddToNetwork;
    private WiRevokeAccessDialog mRevokeAccessDialog;

    private WiContactSharedNetworkListView mContactSharedNetworkList;

    private WiNetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        WiContact contact = getIntent().getExtras().getParcelable("contact");

        try {
            String title = "";
            title += contact.getName();
            if(!contact.getPhone().isEmpty()) {
                title += ": " + contact.getPhone();
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

//        mLayout = findViewById(R.id.center_view);
//        mLayout.removeAllViews();
//        mNetworkScrollView = findViewById(R.id.scroll_network_list);
//
//        LayoutInflater inflater = getLayoutInflater();
//
//        for (int i = 0; i < networkList.size(); i++) {
//            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_contact_shared_network_list_item, null);
////            ((ImageView) layout.findViewById(R.id.iv_wifi_icon_spot)).setImageResource(R.drawable.ic_wifi_black_24dp);
//            ((TextView) layout.findViewById(R.id.tv_network_name)).setText(mNetworks.get(i).SSID);
//            ((TextView) layout.findViewById(R.id.tv_connection_status)).setText(i + " Active users");
//            if(mNetworks.get(i).networkId % 2 == 0) {
//                ((ImageView) layout.findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_check_green_24dp);
//            } else {
//                ((ImageView) layout.findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_warning_orange_24dp);
//            }
//            if(mLayout.getParent() != null) {
//                ((ViewGroup)mLayout.getParent()).removeView(mLayout);
//            }
//            mLayout.addView(layout);
//        }
//
//        mNetworkScrollView.addView(mLayout);

        for(WiConfiguration config : networkList) {
            mContactSharedNetworkList.addSharedNetwork(config);
        }

        btnRevokeAllAccess = findViewById(R.id.btn_revoke_all_access);
        btnAddContactToNetwork = findViewById(R.id.btn_add_contact_to_network);

        mRevokeAccessDialog = new WiRevokeAccessDialog(this, btnRevokeAllAccess);
        mAddToNetwork = new WiAddContactToNetworkDialog(this, btnAddContactToNetwork);
    }

    private WiContactSharedNetworkListView.OnCheckBoxVisibleListener onCheckBoxVisible() {
        return new WiContactSharedNetworkListView.OnCheckBoxVisibleListener() {

            @Override
            public void onCheckBoxVisible() {
                btnHideCheckBoxes.setVisibility(View.VISIBLE);
                btnHideCheckBoxes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContactSharedNetworkList.hideAllCheckBoxes();
                        btnHideCheckBoxes.setVisibility(View.GONE);
                        btnRevokeSelectiveAccess.setVisibility(View.GONE);
                    }
                });

                btnRevokeSelectiveAccess.setVisibility(View.VISIBLE);
                btnRevokeSelectiveAccess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove selected networks


                        mContactSharedNetworkList.hideAllCheckBoxes();
                        btnHideCheckBoxes.setVisibility(View.GONE);
                        btnRevokeSelectiveAccess.setVisibility(View.GONE);
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

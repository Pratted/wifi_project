package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {
    private ScrollView mNetworkScrollView;
    private LinearLayout mLayout;

    private Button btnRevokeAccess;
    private Button btnAddContactToNetwork;

    private ArrayList<WifiConfiguration> mNetworks;

    private WiAddContactToNetworkDialog mAddToNetwork;
    private WiRevokeAccessDialog mRevokeAccessDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mNetworks = WiNetworkManager.getConfiguredNetworks(this);

        ArrayList<String> networkList = new ArrayList<>();

        for (WifiConfiguration configuration : mNetworks) {
            networkList.add(configuration.SSID);
        }

        WiContact contact = getIntent().getExtras().getParcelable("contact");

        try {
            getSupportActionBar().setTitle(contact.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            System.out.println("SET TITLE NULL POINTER IN CONTACT ACTIVITY");
        }

        ((TextView) findViewById(R.id.tv_contact_number)).setText(contact.phone);

        ((TextView) findViewById(R.id.tv_permitted_networks)).setText("Networks " + contact.name + " has access to:");

        mLayout = findViewById(R.id.center_view);
        mNetworkScrollView = findViewById(R.id.scroll_network_list);

        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < networkList.size(); i++) {
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_test, null);
            ((TextView) layout.findViewById(R.id.tv_network_name)).setText(mNetworks.get(i).SSID);
            ((TextView) layout.findViewById(R.id.tv_connection_status)).setText(i + " Active users");
//            if(mNetworks.get(i).networkId % 2 == 0) {
//                ((ImageView) findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_check_green_24dp);
//            }
            if(mLayout.getParent() != null) {
                ((ViewGroup)mLayout.getParent()).removeView(mLayout);
            }
            mLayout.addView(layout);
        }
        mNetworkScrollView.addView(mLayout);


        btnRevokeAccess = findViewById(R.id.btn_revoke_access);
        btnAddContactToNetwork = findViewById(R.id.btn_add_contact_to_network);

        mRevokeAccessDialog = new WiRevokeAccessDialog(this, btnRevokeAccess);
        mAddToNetwork = new WiAddContactToNetworkDialog(this, btnAddContactToNetwork);



/*
        RecyclerView rvNetworkList = ((RecyclerView)findViewById(R.id.rv_network_list));
*/
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

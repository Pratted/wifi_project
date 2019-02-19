package com.example.eric.wishare;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
    private LinearLayout mScrollView;

    private Button btnRevokeAccess;
    private Button btnAddContactToNetwork;


    private WiAddContactToNetworkDialog mAddToNetwork;
    private WiRevokeAccessDialog mRevokeAccessDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        WiContact contact = getIntent().getExtras().getParcelable("contact");

        //Set name and phone number
        ((TextView)findViewById(R.id.tv_contact_name)).setText(contact.getName());
        ((TextView)findViewById(R.id.tv_contact_number)).setText(contact.getPhone());

        //Set up scroll view
        mNetworkScrollView = findViewById(R.id.scroll_network_list);
        mScrollView = new LinearLayout(this);
        mScrollView.setOrientation(LinearLayout.VERTICAL);
        mNetworkScrollView.addView(mScrollView);
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_permitted_network_list_item, null);


        //Using hard coded placeholders
        ((TextView) layout.findViewById(R.id.tv_network_name)).setText("Port Sarim");
        ((TextView) layout.findViewById(R.id.tv_connection_status)).setText("Invitation sent");
        mScrollView.addView(layout);
        layout = (LinearLayout) inflater.inflate(R.layout.layout_permitted_network_list_item, null);
        ((TextView) layout.findViewById(R.id.tv_network_name)).setText("Varrock");
        ((TextView) layout.findViewById(R.id.tv_connection_status)).setText("Connected");
        mScrollView.addView(layout);
        layout = (LinearLayout) inflater.inflate(R.layout.layout_permitted_network_list_item, null);
        ((TextView) layout.findViewById(R.id.tv_network_name)).setText("Lumbridge");
        ((TextView) layout.findViewById(R.id.tv_connection_status)).setText("Data limit reached");
        mScrollView.addView(layout);
        layout = (LinearLayout) inflater.inflate(R.layout.layout_permitted_network_list_item, null);
        ((TextView) layout.findViewById(R.id.tv_network_name)).setText("Edgeville");
        ((TextView) layout.findViewById(R.id.tv_connection_status)).setText("Not connected");
        mScrollView.addView(layout);
        layout = (LinearLayout) inflater.inflate(R.layout.layout_permitted_network_list_item, null);
        ((TextView) layout.findViewById(R.id.tv_network_name)).setText("Falador");
        ((TextView) layout.findViewById(R.id.tv_connection_status)).setText("Invitation expired");
        mScrollView.addView(layout);


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

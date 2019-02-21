package com.example.eric.wishare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class NetworkActivity extends AppCompatActivity {

    private WiConfiguration mConfig;
    private TextView mTvNetworkName;
    private WiInviteContactsDialog mInviteContactsDialog;
    private WiEditNetworkDialog mEditNetworkDialog;

    private WiContactList mContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        if(getIntent().hasExtra("NetworkInfo")) {
            System.out.println("IT HAS THE INTENT");
        }

        mConfig = getIntent().getParcelableExtra("NetworkInfo");

        mInviteContactsDialog = new WiInviteContactsDialog(this);
        mEditNetworkDialog = new WiEditNetworkDialog(this, mConfig);

        mContactList = new WiContactList(this);
        mContactList.load();

        for(WiContact contact: mContactList.getWiContacts()){
            mInviteContactsDialog.addContact(contact);
        }

        findViewById(R.id.btn_invite_contacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInviteContactsDialog.show();
            }
        });

        findViewById(R.id.btn_edit_network).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditNetworkDialog.show();
            }
        });

        try {
            getSupportActionBar().setTitle(mConfig.getSSID());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            System.out.println("SET TITLE NULL POINTER IN NETWORK ACTIVITY");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

package com.example.eric.wishare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NetworkActivity extends AppCompatActivity {

    private WiConfiguration mConfig;
    private TextView mTvNetworkName;
    private WiInviteContactsDialog mInviteContactsDialog;

    private WiContactList mContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        if(getIntent().hasExtra("NetworkInfo")) {
            System.out.println("IT HAS THE INTENT");
        }


        mInviteContactsDialog = new WiInviteContactsDialog(this);


        mContactList = new WiContactList(this);
        mContactList.load();

        for(WiContact contact: mContactList.getWiContacts()){
            mInviteContactsDialog.addContact(contact);
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInviteContactsDialog.show();
            }
        });

        String networkName = getIntent().getStringExtra("NetworkInfo");

        if(networkName != null) {
            ((TextView)findViewById(R.id.tv_network_name)).setText(networkName);

        } else {
            ((TextView)findViewById(R.id.tv_network_name)).setText("WHAT");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}

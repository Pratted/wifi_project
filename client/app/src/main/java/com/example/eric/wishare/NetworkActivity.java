package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class NetworkActivity extends AppCompatActivity {

    private WiConfiguration mConfig;
    private WiInviteContactsDialog mInviteContactsDialog;
    private WiTabbedScrollView mTabbedScrollView;

    private WiContactList mContactList;

    @SuppressLint("ClickableViewAccessibility")
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

        mConfig = getIntent().getParcelableExtra("NetworkInfo");

        try {
            getSupportActionBar().setTitle(mConfig.getSSID());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            System.out.println("SET TITLE NULL POINTER IN NETWORK ACTIVITY");
        }


        mTabbedScrollView = findViewById(R.id.tabbed_scroll_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

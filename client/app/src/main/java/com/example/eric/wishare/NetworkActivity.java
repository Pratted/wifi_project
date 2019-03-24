package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


import java.util.ArrayList;
import com.example.eric.wishare.dialog.WiEditNetworkDialog;
import com.example.eric.wishare.dialog.WiInviteContactsDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.view.WiTabbedScrollView;

public class NetworkActivity extends AppCompatActivity {

    private WiConfiguration mConfig;

    private WiInviteContactsDialog mInviteContactsDialog;
    private WiEditNetworkDialog mEditNetworkDialog;

    private WiTabbedScrollView mTabbedScrollView;

    private EditText searchBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        mConfig = getIntent().getParcelableExtra("NetworkInfo");

        mInviteContactsDialog = new WiInviteContactsDialog(this);
        mEditNetworkDialog = new WiEditNetworkDialog(this, mConfig);

        searchBar = findViewById(R.id.edit_text_search_bar);
        searchBar.addTextChangedListener(search());

        findViewById(R.id.btn_edit_network).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditNetworkDialog.show();
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

    private WiContactList.OnContactListReadyListener onContactListReady(){
        return new WiContactList.OnContactListReadyListener(){
            @Override
            public void onContactListReady(ArrayList<WiContact> contacts) {
                //Build Tabbed List

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private ArrayList<WiContact> getPermittedContacts(){
        SQLiteDatabase db = WiSQLiteDatabase.getInstance(this).getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM PermittedContacts WHERE network_id=?", new String[]{mConfig.getNetworkID()});

        if (cur != null && cur.moveToFirst()) {
            do {
                WiConfiguration wiConfiguration = new WiConfiguration(
                        cur.getString(cur.getColumnIndex("SSID")),
                        cur.getString(cur.getColumnIndex("password")));
               // mConfiguredNetworks.add(wiConfiguration);
            } while (cur.moveToNext());
        }
        cur.close();
        return new ArrayList<WiContact>();
    }

    public TextWatcher search() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTabbedScrollView.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }
}

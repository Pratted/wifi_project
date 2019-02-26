package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;

public class NetworkActivity extends AppCompatActivity {

    private WiConfiguration mConfig;

    private WiInviteContactsDialog mInviteContactsDialog;
    private WiEditNetworkDialog mEditNetworkDialog;
    private WiRevokeAccessDialog mRevokeAccessDialog;

    private WiTabbedScrollView mTabbedScrollView;
    private WiContactList mContactList;

    private Button mButtonLhs;
    private Button mButtonRhs;

    private EditText searchBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        mConfig = getIntent().getParcelableExtra("NetworkInfo");

        mInviteContactsDialog = new WiInviteContactsDialog(this);
        mEditNetworkDialog = new WiEditNetworkDialog(this, mConfig);

        mContactList = new WiContactList(this);
        mContactList.load();

        searchBar = findViewById(R.id.edit_text_search_bar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTabbedScrollView.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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

        mConfig = getIntent().getParcelableExtra("NetworkInfo");

        try {
            getSupportActionBar().setTitle(mConfig.getSSID());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            System.out.println("SET TITLE NULL POINTER IN NETWORK ACTIVITY");
        }

        mTabbedScrollView = findViewById(R.id.tabbed_scroll_view);

        mButtonLhs = findViewById(R.id.btn_lhs);
        mButtonRhs = findViewById(R.id.btn_rhs);

        mButtonLhs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevokeAccessDialog.show();
            }
        });

        mButtonRhs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInviteContactsDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    This makes it so the (upper left) back button returns to MainActivity without calling onCreate()
    Smoother overall.
    */
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
}

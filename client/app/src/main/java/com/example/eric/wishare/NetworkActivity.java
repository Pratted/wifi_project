package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.eric.wishare.dialog.WiEditNetworkDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.view.WiInvitableContactsView;
import com.example.eric.wishare.view.WiPermittedContactsView;
import com.example.eric.wishare.view.WiTabbedScrollViewPager;

public class NetworkActivity extends AppCompatActivity {
    private final String TAG = "NetworkActivity";
    private WiConfiguration mConfig;

    private EditText searchBar;
    private Toolbar mToolbar;

    private WiTabbedScrollViewPager mViewPager;
    private WiPagerAdapter mPagerAdapter;
    private WiContactList mContactList;

    private WiPermittedContactsView mPermittedContactsView;
    private WiInvitableContactsView mInvitableContactsView;

    private Button mLhs;
    private Button mRhs;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_NETWORK);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(WiUtils.ACTIVITY_NETWORK));

        String ssid = getIntent().getStringExtra("ssid");
        mConfig = WiNetworkManager.getInstance(this).getConfiguredNetwork(ssid);

        mToolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.view_pager);
        searchBar = findViewById(R.id.edit_text_search_bar);

        mPagerAdapter = new WiPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mContactList = WiContactList.getInstance(this);

        mPermittedContactsView = new WiPermittedContactsView(this, mLhs, mRhs, mConfig);
        mInvitableContactsView = new WiInvitableContactsView(this, mLhs, mRhs, mConfig);

        for(WiContact contact: mContactList.getWiContacts().values()){
            if(contact.hasAccessTo(mConfig.SSID)){
                mPermittedContactsView.addPermittedContact(contact);
            }
            else{
                mInvitableContactsView.add(contact);
            }
        }

        //mInvitableContactsView.sortName(true);

        mPagerAdapter.addView(mPermittedContactsView);
        mPagerAdapter.notifyDataSetChanged();

        mPagerAdapter.addView(mInvitableContactsView);
        mPagerAdapter.notifyDataSetChanged();

        final TabLayout mTabs = findViewById(R.id.tab_layout);
        mTabs.setupWithViewPager(mViewPager);

        mTabs.getTabAt(0).setText("Permitted Contacts");
        mTabs.getTabAt(1).setText("Invite Contacts");

        mPermittedContactsView.sort(WiPermittedContactsView.COL_NAME); //descending order
        mPermittedContactsView.sort(WiPermittedContactsView.COL_NAME); //ascending order

        mPermittedContactsView.display();

        searchBar.addTextChangedListener(search());

        mToolbar.setTitle(mConfig.getSSIDNoQuotes());
        setSupportActionBar(mToolbar);
    }


    @Override
    protected void onStart() {
        super.onStart();
        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_NETWORK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WiUtils.setCurrentActivity(WiUtils.ACTIVITY_NETWORK);
    }

    public TextWatcher search() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPermittedContactsView.filter(s.toString());
                mInvitableContactsView.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("key");
            Log.d(TAG, "Received message: " + message);

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "Received a new intent!");

        if(intent != null){
            if(intent.hasExtra("invitation")){
                Log.d(TAG, "Prepaing invitation dialog...");
                WiInvitation invitation = intent.getParcelableExtra("invitation");
                new WiInvitationAcceptDeclineDialog(this, invitation).show();
            }
        }
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
                startActivity(new Intent(NetworkActivity.this, SettingsActivity.class));
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

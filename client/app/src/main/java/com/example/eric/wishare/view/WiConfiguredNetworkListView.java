package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.NetworkActivity;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.messaging.WiRevokeAccessDataMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class WiConfiguredNetworkListView extends LinearLayout {
    private String TAG = "WiConfiguredNetworkListView";

    private WiNetworkManager mNetworkManager;
    private WiSQLiteDatabase mDatabase;

    private Map<String, WiConfiguredNetworkListItem> mConfiguredNetworks;

    public WiConfiguredNetworkListView(Context context){
        super(context);
        init();
    }

    public WiConfiguredNetworkListView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public void init() {
        mConfiguredNetworks = new HashMap<>();
        mNetworkManager = WiNetworkManager.getInstance(getContext());
        mDatabase = WiSQLiteDatabase.getInstance(getContext());

        List<WiConfiguration> configuredNetworks  = mNetworkManager.getConfiguredNetworks();

        Log.d(TAG, "Found  " + configuredNetworks.size() + " configured networks");

        for (WiConfiguration config : mNetworkManager.getConfiguredNetworks()){
            addConfiguredNetwork(config);
        }
    }

    public void addConfiguredNetwork(WiConfiguration config) {
        Log.d(TAG, "Adding configured network: '" + config.SSID + "'");

        WiConfiguredNetworkListItem item = new WiConfiguredNetworkListItem(getContext(), config);
        mConfiguredNetworks.put(config.SSID, item);
        addView(item);
    }

    public void refresh(){
        List<WiConfiguration> configuredNetworks  = mNetworkManager.getConfiguredNetworks();
        int fuck = 0;

        for(WiConfiguredNetworkListItem item: mConfiguredNetworks.values()){
            int users = 0;

            Collection<WiContact> contacts = WiContactList.getInstance(getContext()).getWiContacts().values();
            int y = 0;

            for(WiContact contact: contacts){
                Log.d(TAG, "FUCK " + item.mConfig.SSID);
                if(item.mConfig.SSID.replace("\"", "").equals("HOME-0622")){
                    int x = 0;
                }
                if(contact.hasAccessTo(item.mConfig.SSID)){
                    users++;
                }
            }

            item.mSubtitle.setText(users + " Contact(s) have access");
        }
    }

    private class WiConfiguredNetworkListItem extends SwipeLayout {
        private WiConfiguration mConfig;
        private TextView mSubtitle;

        public WiConfiguredNetworkListItem(Context context, WiConfiguration config){
            super(context);
            mConfig = config;

            init();
        }

        public void init() {
            inflate(getContext(), R.layout.layout_configured_network_list_item, this);

            mSubtitle = findViewById(R.id.tv_permitted_users);

            ((TextView) findViewById(R.id.tv_network_name)).setText(mConfig.getSSIDNoQuotes());
            //((TextView) findViewById(R.id.tv_permitted_users)).setText(users + " Contact(s) have access");

            findViewById(R.id.middle_view).setOnClickListener(startNetworkActivity());
            findViewById(R.id.iv_trash).setOnClickListener(displayConfirmDeleteDialog());
        }

        private View.OnClickListener displayConfirmDeleteDialog(){
            return new OnClickListener() {
                boolean checked = false;
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getContext())
                            .title("Removing " + mConfig.getSSIDNoQuotes())
                            .content("Are you sure you want to remove " + mConfig.getSSIDNoQuotes() + "? This action cannot be undone.")
                            .checkBoxPrompt("Revoke access for all contacts?", false, new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    checked = isChecked;
                                }
                            })
                            .positiveText("Yes")
                            .negativeText("Cancel")

                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    WiConfiguredNetworkListView.this.removeView(WiConfiguredNetworkListItem.this);
                                    System.out.println("mConfig.getSSIDNoQuotes(): " + mConfig.getSSIDNoQuotes());

                                    if (checked){
                                        List<String> phoneList = WiSQLiteDatabase.getInstance(getContext()).getNetworksContacts(mConfig);
                                        for (String phone: phoneList) {
                                            WiContact contact = WiContactList.getInstance(getContext()).getContactByPhone(phone);
                                            contact.revokeAccess(mConfig.SSID);
                                            WiContactList.getInstance(getContext()).save(contact);
                                            WiRevokeAccessDataMessage msg = new WiRevokeAccessDataMessage(mConfig, contact.getPhone());
                                            WiDataMessageController.getInstance(getContext().getApplicationContext()).send(msg);
                                            WiSQLiteDatabase.getInstance(getContext()).delete(mConfig, phone);
                                        }
                                    }

                                    mNetworkManager.unConfigureNetwork(mConfig);
                                    WiContactList.getInstance(getContext()).deleteNetworkFromAllContacts(mConfig.SSID);
                                    mDatabase.delete(mConfig);
                                }
                            }).show();
                }
            };
        }

        private View.OnClickListener startNetworkActivity(){
            return new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NetworkActivity.class);
                    intent.putExtra("ssid", mConfig.SSID);
                    Log.d(TAG, "Starting network activity. Using ssid='" + mConfig.SSID +"'");
                    getContext().startActivity(intent);
                }
            };
        }
    }
}

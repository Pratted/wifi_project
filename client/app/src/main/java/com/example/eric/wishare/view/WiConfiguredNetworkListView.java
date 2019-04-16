package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.NetworkActivity;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.messaging.WiRevokeAccessDataMessage;

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
        WiConfiguredNetworkListItem item = new WiConfiguredNetworkListItem(getContext(), config);
        mConfiguredNetworks.put(config.SSID, item);
        addView(item);
    }

    private class WiConfiguredNetworkListItem extends SwipeLayout {
        private WiConfiguration mConfig;

        public WiConfiguredNetworkListItem(Context context, WiConfiguration config){
            super(context);
            mConfig = config;

            init();
        }

        public void init() {
            inflate(getContext(), R.layout.layout_configured_network_list_item, this);

            int users = mConfig.hashCode() % 5;
            if(users < 0) users *= -1;

            System.out.println(mConfig.getSSID());

            ((TextView) findViewById(R.id.tv_network_name)).setText(mConfig.getSSID());
            ((TextView) findViewById(R.id.tv_active_users)).setText(users + " active user(s)");

            if(users % 2 == 0) {
                ((ImageView) findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_check_green_24dp);
            }

            findViewById(R.id.middle_view).setOnClickListener(startNetworkActivity());
            findViewById(R.id.iv_trash).setOnClickListener(displayConfirmDeleteDialog());
        }

        private View.OnClickListener displayConfirmDeleteDialog(){
            return new OnClickListener() {
                boolean checked = false;
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getContext())
                            .title("Removing " + mConfig.getSSID())
                            .content("Are you sure you want to remove " + mConfig.getSSID() + "? This action cannot be undone.")
                            .checkBoxPrompt("Revoke access for all contacts?", false, new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked){
                                        checked = true;
                                    }
                                    else{
                                        checked = false;
                                    }
                                }
                            })
                            .positiveText("Yes")
                            .negativeText("Cancel")

                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    WiConfiguredNetworkListView.this.removeView(WiConfiguredNetworkListItem.this);
                                    System.out.println("mConfig.getSSID(): " + mConfig.getSSID());

                                    if (checked){
                                        List<String> phoneList = WiSQLiteDatabase.getInstance(getContext()).getNetworksContacts(mConfig);
                                        for (String phone: phoneList) {
                                            WiRevokeAccessDataMessage msg = new WiRevokeAccessDataMessage(mConfig, phone);
                                            WiDataMessageController.getInstance(getContext().getApplicationContext()).send(msg);
                                            WiSQLiteDatabase.getInstance(getContext()).delete(mConfig, phone);
                                        }
                                    }

                                    mNetworkManager.removeConfiguredNetwork(mConfig);
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
                    System.out.println("CLICKED");
                    Intent intent = new Intent(getContext(), NetworkActivity.class);
                    intent.putExtra("NetworkInfo", mConfig);
                    System.out.println("THIS IS THE SSID " + mConfig.getSSID());
                    System.out.println("\nSTARTING NETWORK ACTIVITY\n");
                    getContext().startActivity(intent);
                }
            };
        }
    }
}

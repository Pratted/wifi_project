package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.NetworkActivity;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.model.WiConfiguration;

import java.util.ArrayList;
import java.util.List;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class WiConfiguredNetworkListView extends LinearLayout {

    private List<WiConfiguration> configs;

    public WiConfiguredNetworkListView(Context context){
        super(context);
        init();
    }

    public WiConfiguredNetworkListView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public void init() {
        configs = new ArrayList<>();
    }

    public void addView(WiConfiguration config) {
        configs.add(config);
        this.addView(new WiConfiguredNetworkListItem(getContext(), config));
    }

    private class WiConfiguredNetworkListItem extends SwipeLayout {
        private WiConfiguration mConfig;
        private WiNetworkManager mNetworkManager;

        public WiConfiguredNetworkListItem(Context context, WiConfiguration config){
            super(context);
            mConfig = config;

            init();
        }

        public void init() {
            mNetworkManager = WiNetworkManager.getInstance(getContext().getApplicationContext());

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
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getContext())
                            .title("Removing " + mConfig.getSSID())
                            .content("Are you sure you want to remove " + mConfig.getSSID() + "? This action cannot be undone.")
                            .checkBoxPrompt("Revoke access for all contacts?", false, null)
                            .positiveText("Yes")
                            .negativeText("Cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    WiConfiguredNetworkListView.this.removeView(WiConfiguredNetworkListItem.this);
                                    System.out.println("mConfig.getSSID(): " + mConfig.getSSID()) ;
                                    mNetworkManager.removeConfiguredNetwork(mConfig);
                                    mNetworkManager.addNotConfiguredNetwork(mConfig);
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

package com.example.eric.wishare;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WiConfiguredNetworkListView extends LinearLayout {

    public WiConfiguredNetworkListView(Context context){
        super(context);
    }

    public WiConfiguredNetworkListView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public void addView(WiConfiguration config) {
        // calls LinearLayout.addView()
        // since WiConfiguredNetworkListItem is a LinearLayout, it can be passed into addView()
        this.addView(new WiConfiguredNetworkListViewItem(getContext(), config));
    }

    public class WiConfiguredNetworkListViewItem extends LinearLayout {
        private WiConfiguration mConfig;

        public WiConfiguredNetworkListViewItem(Context context, WiConfiguration config){
            super(context);
            mConfig = config;

            inflate(getContext(), R.layout.layout_configured_network_list_item, this);

            int users = config.hashCode() % 5;
            if(users < 0) users *= -1;

            System.out.println(mConfig.getSSID());

            ((TextView) findViewById(R.id.tv_network_name)).setText(config.getSSID());
            ((TextView) findViewById(R.id.tv_active_users)).setText(users + " active user(s)");

            if(users % 2 == 0) {
                ((ImageView) findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_check_green_24dp);
            }


            setOnClickListener(onClick());
        }

        private View.OnClickListener onClick(){
            return new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NetworkActivity.class);
                    intent.putExtra("NetworkInfo", mConfig.getSSID());
                    System.out.println("THIS IS THE SSID " + mConfig.getSSID());
                    System.out.println("\nSTARTING NETWORK ACTIVITY\n");
                    getContext().startActivity(intent);
                }
            };
        }
    }
}

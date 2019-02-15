package com.example.eric.wishare;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiConfiguredNetworkList {

    private LinearLayout mScrollLayout;

    public WiConfiguredNetworkList(Context c, LinearLayout parent) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_configured_network_list_item, null);
        mScrollLayout = parent;
        mScrollLayout.addView(layout);
    }

    public void addView(LinearLayout layout) {
        mScrollLayout.addView(layout);
    }

    public class WiConfiguredNetworkListItem {

        private WiConfiguration mConfig;

        public WiConfiguredNetworkListItem(Context c, WiConfiguration mConfig, LinearLayout parent) {
            this.mConfig = mConfig;

            LayoutInflater inflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_configured_network_list_item, null);
            parent.addView(layout);
        }


        public void onClick() {

        }
    }
}

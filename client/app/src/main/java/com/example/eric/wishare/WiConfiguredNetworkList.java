package com.example.eric.wishare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.lang.ref.WeakReference;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiConfiguredNetworkList {

    private WeakReference<Context> mContext;
    private LinearLayout mScrollLayout;
    private LayoutInflater mInflater;

    public WiConfiguredNetworkList(Context c, LinearLayout parent) {
        mContext = new WeakReference<>(c);
        mInflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.layout_configured_network_list_item, null);
        mScrollLayout = parent;
        mScrollLayout.addView(layout);
    }

    public void addView(WiConfiguredNetworkListItem listItem) {
        mScrollLayout.addView(listItem.getLayout());
    }

    public class WiConfiguredNetworkListItem  {

        private WiConfiguration mConfig;
        private LinearLayout mLayout;

        public WiConfiguredNetworkListItem(WiConfiguration mConfig) {

            this.mConfig = mConfig;
            mLayout = (LinearLayout) mInflater.inflate(R.layout.layout_configured_network_list_item, null);
            mScrollLayout.addView(layout);
        }

        public View getLayout() {
            return mLayout;
        }

        public void onClick() {

        }
    }
}

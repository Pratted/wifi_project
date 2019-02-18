package com.example.eric.wishare;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiConfiguredNetworkListView extends View {

    private WeakReference<Context> mContext;
    private LinearLayout mScrollLayout;
    private LayoutInflater mInflater;

    public WiConfiguredNetworkListView(Context c, LinearLayout parent) {
        super(c);
        mContext = new WeakReference<>(c);
        mInflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.layout_configured_network_list_item, null);
        mScrollLayout = parent;
        mScrollLayout.addView(layout);
    }

    public void addView(WiConfiguration config) {
        addView(new WiConfiguredNetworkListViewItem(config));
    }

    private void addView(WiConfiguredNetworkListViewItem listViewItem) {
        mScrollLayout.addView(listViewItem.getLayout());
    }

    public class WiConfiguredNetworkListViewItem implements OnClickListener {

        private WiConfiguration mConfig;
        private LinearLayout mLayout;

        public WiConfiguredNetworkListViewItem(final WiConfiguration config) {
            this.mConfig = config;
            mLayout = (LinearLayout) mInflater.inflate(R.layout.layout_configured_network_list_item, null);

            int users = config.hashCode() % 5;
            if(users < 0) users *= -1;

            System.out.println(mConfig.getSSID());


            ((TextView) mLayout.findViewById(R.id.tv_network_name)).setText(config.getSSID());
            ((TextView) mLayout.findViewById(R.id.tv_active_users)).setText(users + " active user(s)");

            if(users % 2 == 0) {
                ((ImageView) mLayout.findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_check_green_24dp);
            }

            mLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NetworkActivity.class);
                    intent.putExtra("NetworkInfo", mConfig.getSSID());
                    System.out.println("THIS IS THE SSID " + mConfig.getSSID());
                    System.out.println("\nSTARTING NETWORK ACTIVITY\n");
                    getContext().startActivity(intent);
                }
            });
        }

        public View getLayout() {
            return mLayout;
        }

        @Override
        public void onClick(View v) {
            mLayout.callOnClick();
        }
    }
}

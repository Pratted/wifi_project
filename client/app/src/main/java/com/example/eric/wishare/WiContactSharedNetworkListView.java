package com.example.eric.wishare;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class WiContactSharedNetworkListView extends LinearLayout {

    private CheckBox mSelectAll;
    private Button mNetworkLabel;

    private LinearLayout mSharedNetworkListItems;

    private ArrayList<WiContactSharedNetworkListViewItem> mSharedNetworks;

    public WiContactSharedNetworkListView(Context c) {
        super(c);

        init();
    }

    public WiContactSharedNetworkListView(Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);

        init();
    }

    public void init() {
        inflate(getContext(), R.layout.layout_contact_shared_network_list, this);

        mSharedNetworks = new ArrayList<>();

        mSelectAll = findViewById(R.id.cb_select_all_networks);
        mNetworkLabel = findViewById(R.id.btn_sort_networks);

        mSharedNetworkListItems = findViewById(R.id.ll_network_list_items);

        mSelectAll.setVisibility(INVISIBLE);
        mSelectAll.setOnCheckedChangeListener(onSelect());
    }

    private CompoundButton.OnCheckedChangeListener onSelect() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(WiContactSharedNetworkListViewItem network : mSharedNetworks) {
                    network.mCheckBox.setSelected(isChecked);
                }
            }
        };
    }

    public void addSharedNetwork(WiConfiguration config) {
        WiContactSharedNetworkListViewItem item = new WiContactSharedNetworkListViewItem(getContext(), config);

        mSharedNetworks.add(item);
        mSharedNetworkListItems.addView(item);
    }

    private class WiContactSharedNetworkListViewItem extends LinearLayout {

        private WiConfiguration mConfig;
        private CheckBox mCheckBox;

        private Button mNetworkActivityButton;

        private TextView mNetworkName;

        private LinearLayout mContactsWithSharedNetwork;

        private ExpandableLayout mExpandableLayout;

        public WiContactSharedNetworkListViewItem(Context c, WiConfiguration config) {
            super(c);
            mConfig = config;

            init();
        }

        public void init() {
            inflate(getContext(), R.layout.layout_contact_shared_network_list_item, this);
            System.out.println("IN LIST ITEM INIT");
            mCheckBox = findViewById(R.id.cb_network_select);

            mExpandableLayout = findViewById(R.id.expandable_network);

            mNetworkName = findViewById(R.id.btn_network_name);
            mNetworkName.setText(mConfig.getSSID());

            mNetworkActivityButton = findViewById(R.id.btn_network_activity);
            mContactsWithSharedNetwork = findViewById(R.id.ll_contacts_with_shared_network);

            mNetworkName.setOnClickListener(expand());
//            mNetworkName.setOnClickListener(expand());
            (findViewById(R.id.center_view)).setOnClickListener(expand());
            System.out.println("LEAVING LIST ITEM INIT");
        }

        private View.OnClickListener expand() {
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()) {
                        mExpandableLayout.collapse();
                    } else {
                        mExpandableLayout.expand();
                    }
                }
            };
        }
    }

}

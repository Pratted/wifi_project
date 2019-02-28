package com.example.eric.wishare;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import net.cachapa.expandablelayout.ExpandableLayout;

public class WiContactSharedNetworkListView extends LinearLayout {

    public WiContactSharedNetworkListView(Context c) {
        super(c);
    }

    public WiContactSharedNetworkListView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public void addView(WiConfiguration config) {
        this.addView(new WiContactSharedNetworkListViewItem(getContext(), config));
    }

    private class WiContactSharedNetworkListViewItem extends LinearLayout {

        private WiConfiguration mConfig;
        private CheckBox mCheckBox;

        private Button mNetworkActivityButton;

        private ExpandableLayout mExpandableLayout;

        public WiContactSharedNetworkListViewItem(Context c, WiConfiguration config) {
            super(c);
            mConfig = config;

            init();
        }

        public void init() {
            inflate(getContext(), R.layout.layout_test, this);
            // Follow WiPermittedContactsViewListItem
        }
    }

}

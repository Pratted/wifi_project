package com.example.eric.wishare;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class WiPermittedContactsView extends LinearLayout {

    private CheckBox mHeaderSelectAll;
    private Button mHeaderName;
    private Button mHeaderData;
    private Button mHeaderExpires;

    private LinearLayout mHeaders;
    private LinearLayout mItems;

    private ArrayList<WiPermittedContactsViewListItem> mPermittedContacts;

    public WiPermittedContactsView(Context context) {
        super(context);

        init();
    }

    public WiPermittedContactsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private class WiPermittedContactsViewListItem extends LinearLayout {

        private CheckBox mCheckBox;
        private Button mName;
        private TextView mData;
        private TextView mExpires;

        private WiContact mContact;

        public WiPermittedContactsViewListItem(Context context) {
            super(context);

            init();
        }

        public WiPermittedContactsViewListItem(Context context, WiContact contact) {
            super(context);
            mContact = contact;

            init();
        }

        public WiPermittedContactsViewListItem(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);

            init();
        }

        private void init(){
            inflate(getContext(), R.layout.layout_permitted_contacts_list_item, this);

            mCheckBox = (CheckBox) findViewById(R.id.cb_select);
            mName = (Button) findViewById(R.id.btn_name);
            mData = (TextView) findViewById(R.id.tv_data);
            mExpires = (TextView) findViewById(R.id.tv_expires);

            mName.setText(mContact.name);
            mData.setText("10 Gb");
            mExpires.setText("3d 2h");
        }

        private View.OnLongClickListener onLongClick(){
            return new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAllCheckBoxes();
                    return false;
                }
            };
        }
    }

    private void init(){
        inflate(getContext(), R.layout.tabbed_view_permitted_contacts, this);
        mPermittedContacts = new ArrayList<>();

        mHeaderSelectAll = (CheckBox) findViewById(R.id.cb_select_all);
        mHeaderName = (Button) findViewById(R.id.btn_name);
        mHeaderData = (Button) findViewById(R.id.btn_data);
        mHeaderExpires = (Button) findViewById(R.id.btn_expires);

        mHeaders = findViewById(R.id.headers);
        mItems = findViewById(R.id.items);

        mHeaderSelectAll.setVisibility(INVISIBLE);
    }

    public void addPermittedContact(WiContact contact){
        WiPermittedContactsViewListItem item = new WiPermittedContactsViewListItem(getContext(), contact);

        mPermittedContacts.add(item);
        mItems.addView(item);
    }


    private CompoundButton.OnCheckedChangeListener onSelectAll(){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(WiPermittedContactsViewListItem child: mPermittedContacts){
                    child.mCheckBox.setChecked(true);
                }
            }
        };
    }

    private void showAllCheckBoxes(){
        for(WiPermittedContactsViewListItem child: mPermittedContacts){
            child.mCheckBox.setVisibility(VISIBLE);
        }
    }

    private void hideAllCheckBoxes(){
        for(WiPermittedContactsViewListItem child: mPermittedContacts){
            child.mCheckBox.setVisibility(INVISIBLE);
        }
    }

    private boolean areCheckBoxesEnabled(){
        return mHeaderSelectAll.getVisibility() == VISIBLE;
    }
}

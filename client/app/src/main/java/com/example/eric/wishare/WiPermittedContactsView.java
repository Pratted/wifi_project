package com.example.eric.wishare;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;

public class WiPermittedContactsView extends LinearLayout {

    private CheckBox mHeaderSelectAll;
    private Button mHeaderName;
    private Button mHeaderData;
    private Button mHeaderExpires;

    private LinearLayout mHeaders;
    private LinearLayout mItems;

    private boolean mAscendingName;

    private ArrayList<WiPermittedContactsViewListItem> mPermittedContacts;

    public WiPermittedContactsView(Context context) {
        super(context);

        init();
    }

    public WiPermittedContactsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_permitted_contacts, this);
        mPermittedContacts = new ArrayList<>();

        mHeaderSelectAll = (CheckBox) findViewById(R.id.cb_select_all);
        mHeaderName = (Button) findViewById(R.id.btn_name);
        mHeaderData = (Button) findViewById(R.id.btn_data);
        mHeaderExpires = (Button) findViewById(R.id.btn_expires);

        mHeaders = findViewById(R.id.headers);
        mItems = findViewById(R.id.items);

        mHeaderSelectAll.setVisibility(INVISIBLE);
        mHeaderSelectAll.setOnCheckedChangeListener(onSelectAll());

        mAscendingName = true;
        mHeaderName.setOnClickListener(sortName());
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
                    child.mCheckBox.setChecked(isChecked);
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

    private void setContactsClickable(boolean clickable){
        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            contact.setContactClickable(clickable);
        }
    }

    private View.OnClickListener sortName(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                sortName(mAscendingName);
            }
        };
    }

    public void sortName(final boolean ascending){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPermittedContacts.sort(new Comparator<WiPermittedContactsViewListItem>() {
                @Override
                public int compare(WiPermittedContactsViewListItem o1, WiPermittedContactsViewListItem o2) {
                    return ascending ?
                            o1.mContact.name.compareTo(o2.mContact.name) :
                            o2.mContact.name.compareTo(o1.mContact.name);
                }
            });
        }

        mAscendingName = !ascending;
        mItems.removeAllViewsInLayout();
        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            mItems.addView(contact);
        }
    }

    public void filter(String searchString) {
        mItems.removeAllViews();

        if(!searchString.isEmpty()) {

            for(WiPermittedContactsViewListItem item : mPermittedContacts) {
                if(item.getContact().getName().toLowerCase().contains(searchString.toLowerCase())) {
                    mItems.addView(item);
                }
            }
        } else {
            for(WiPermittedContactsViewListItem item : mPermittedContacts) {
                mItems.addView(item);
            }
        }
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

            if(mContact != null) setContact(mContact);
        }

        private View.OnClickListener startContactActivity(final WiContact contact){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ContactActivity.class);
                    intent.putExtra("contact", contact);
                    getContext().startActivity(intent);
                }
            };
        }

        public void setContactClickable(boolean clickable){
            mName.setOnClickListener(clickable ? startContactActivity(mContact) : resetOnClick());
        }

        public void setContact(WiContact contact){
            mName.setText(mContact.name);
            mData.setText("10 Gb");
            mExpires.setText("3d 2h");

            mName.setOnLongClickListener(onLongClick());
            mName.setOnClickListener(startContactActivity(mContact));
        }

        public WiContact getContact() {
            return mContact;
        }

        private View.OnClickListener resetOnClick(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            };
        }

        private View.OnLongClickListener onLongClick(){
            return new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setContactsClickable(false);
                    showAllCheckBoxes();
                    mHeaderSelectAll.setVisibility(VISIBLE);
                    return false;
                }
            };
        }
    }
}

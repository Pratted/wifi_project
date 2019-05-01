package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.ContactActivity;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.messaging.WiRevokeAccessDataMessage;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Predicate;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class WiPermittedContactsView extends LinearLayout {
    private String TAG = "WiPermContactView";

    private Button mHeaderName;
    private Button mHeaderData;
    private Button mHeaderExpires;

    private Button mButtonRevokeSelected;
    private Button mButtonCancel;


    public static final int COL_NAME = 0;
    public static final int COL_DATA = 1;
    public static final int COL_EXPIRES = 2;

    private HashMap<Integer, Boolean> mSortCriteria;

    private ArrayList<WiPermittedContactsViewListItem> mPermittedContacts;
    private WiConfiguration mNetwork;

    private LinearLayout mItems;
    private ConstraintLayout mLinearLayout;
    private LinearLayout mLinearLayoutEmpty;
    private CheckBox mHeaderSelectAll;

    public WiPermittedContactsView(Context context, WiConfiguration network) {
        super(context);

        mSortCriteria = new HashMap<>();
        mSortCriteria.put(COL_NAME, false);
        mSortCriteria.put(COL_DATA, false);
        mSortCriteria.put(COL_EXPIRES, false);

        mNetwork = network;
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_permitted_contacts, this);
        mPermittedContacts = new ArrayList<>();

        mHeaderName = findViewById(R.id.btn_name);
        mHeaderData = findViewById(R.id.btn_data);
        mHeaderExpires = findViewById(R.id.btn_expires);
        mButtonRevokeSelected = findViewById(R.id.btn_revoke_access_selected);
        mButtonCancel = findViewById(R.id.btn_cancel);
        mHeaderSelectAll = findViewById(R.id.cb_select_all);
        mItems = findViewById(R.id.items);
        mLinearLayout = findViewById(R.id.ll_permitted_contact);
        mLinearLayoutEmpty = findViewById(R.id.ll_permitted_contact_empty);

        mHeaderName.setOnClickListener(sort(COL_NAME, 0));
        mHeaderData.setOnClickListener(sort(COL_DATA, 0));
        mHeaderExpires.setOnClickListener(sort(COL_EXPIRES, 0));

        mButtonCancel.setOnClickListener(Cancel);

        setCheckBoxVisibilities(View.INVISIBLE);
    }

    private View.OnClickListener Cancel = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            setCheckBoxVisibilities(INVISIBLE);
        }
    };

    public void setCheckBoxVisibilities(int visibility){
        mButtonRevokeSelected.setVisibility(visibility);
        mButtonCancel.setVisibility(visibility);
        mHeaderSelectAll.setVisibility(visibility);
        mHeaderSelectAll.setChecked(false);

        for(WiPermittedContactsViewListItem child: mPermittedContacts){
            // if they have a pending invitation the checkbox is effectively disabled
            child.mCheckBox.setVisibility(visibility);
        }
    }

    private void paintRows(){
        boolean alt = false;

        for(WiPermittedContactsViewListItem item: mPermittedContacts){
            if(item.getVisibility() == VISIBLE){
                item.setBackgroundResource(alt ? R.color.themedarkest : R.color.themedarkGreen);
                alt = !alt;
            }
        }
    }


    public void addPermittedContact(WiContact contact){
        WiPermittedContactsViewListItem item = new WiPermittedContactsViewListItem(getContext(), contact);

        mPermittedContacts.add(item);
        mItems.addView(item);

        paintRows();
    }

    public MaterialDialog.SingleButtonCallback removeSelectedContacts(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mPermittedContacts.removeIf(new Predicate<WiPermittedContactsViewListItem>() {
                        @Override
                        public boolean test(WiPermittedContactsViewListItem wiPermittedContactsViewListItem) {
                            if(wiPermittedContactsViewListItem.mCheckBox.isChecked()){
                                WiRevokeAccessDataMessage msg = new WiRevokeAccessDataMessage(mNetwork, wiPermittedContactsViewListItem.getContact().getPhone());

                                wiPermittedContactsViewListItem.getContact().revokeAccess(mNetwork.SSID);
                                WiContactList.getInstance(getContext()).save(wiPermittedContactsViewListItem.getContact());

                                WiDataMessageController.getInstance(getContext().getApplicationContext()).send(msg);

                            }

                            return wiPermittedContactsViewListItem.mCheckBox.isChecked();
                        }
                    });
                }
            }
        };
    }

    // sort mPermittedContacts
    private void sort(final int column, final boolean ascending){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            switch (column) {
                case COL_NAME:
                    mPermittedContacts.sort(new Comparator<WiPermittedContactsViewListItem>() {
                        @Override
                        public int compare(WiPermittedContactsViewListItem o1, WiPermittedContactsViewListItem o2) {
                            return ascending ?
                                    o1.mContact.getName().compareTo(o2.mContact.getName()) :
                                    o2.mContact.getName().compareTo(o1.mContact.getName());
                        }
                    });
                    break;

                case COL_DATA:
                    mPermittedContacts.sort(new Comparator<WiPermittedContactsViewListItem>() {
                        @Override
                        public int compare(WiPermittedContactsViewListItem o1, WiPermittedContactsViewListItem o2) {
                            return ascending ?
                                    o1.mContact.getDataUsage().compareTo(o2.mContact.getDataUsage()) :
                                    o2.mContact.getDataUsage().compareTo(o1.mContact.getDataUsage());
                        }
                    });
                    break;

                case COL_EXPIRES:
                    mPermittedContacts.sort(new Comparator<WiPermittedContactsViewListItem>() {
                        @Override
                        public int compare(WiPermittedContactsViewListItem o1, WiPermittedContactsViewListItem o2) {
                            return ascending ?
                                    o1.mContact.getExpiresIn().compareTo(o2.mContact.getExpiresIn()) :
                                    o2.mContact.getExpiresIn().compareTo(o1.mContact.getExpiresIn());
                        }
                    });
                    break;
            }
        }

        mSortCriteria.put(column, !ascending);

        mItems.removeAllViews();

        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            mItems.addView(contact);
        }

        paintRows();
    }

    public void sort(int column){
        sort(column, mSortCriteria.get(column));
    }

    private OnClickListener sort(final int column, int temp){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                sort(column, mSortCriteria.get(column));
            }
        };
    }

    public void filter(String searchString) {
        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            contact.setVisibility(contact.mContact.getName().toLowerCase().contains(searchString.toLowerCase()) ? VISIBLE : GONE);
        }

        paintRows();
    }

    public void display(){
        Log.d(TAG, "Refreshing");
        Log.d(TAG, "Permitted Contacts: " + mPermittedContacts.size());

        mLinearLayout.setVisibility(mPermittedContacts.isEmpty() ? View.GONE : View.VISIBLE);
        mLinearLayoutEmpty.setVisibility(mPermittedContacts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /*
    private OnClickListener displayMultiRevokeAccessDialog() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = getSelectedItemCount();
                String contacts = qty > 1 ? " contacts" : " contact";

                if(qty == 0){
                    Toast.makeText(getContext(), "No Contacts Selected!", Toast.LENGTH_LONG);
                }
                else{
                    new MaterialDialog.Builder(getContext())
                            .title("Revoke Access to " + mNetwork.getSSIDNoQuotes())
                            .content("Are you want to revoke access for " + qty + contacts + "? This action cannot be undone.")
                            .negativeText("Cancel")
                            .positiveText("Revoke")
                            .onPositive(removeSelectedContacts())
                            .show();
                }
            }
        };
    }
    */

    private class WiPermittedContactsViewListItem extends LinearLayout {
        private CheckBox mCheckBox;
        private TextView mName;
        private TextView mData;
        private TextView mExpires;
        private Button mRevokeAccess;
        private Button mVisitProfile;

        private WiContact mContact;

        private ExpandableLayout mExpandableLayout;
        private SwipeLayout mSwipeLayout;

        private LinearLayout mRow;

        public WiPermittedContactsViewListItem(Context context, WiContact contact) {
            super(context);
            mContact = contact;

            init();
        }

        private void init(){
            inflate(getContext(), R.layout.layout_permitted_contacts_list_item, this);

            mCheckBox = (CheckBox) findViewById(R.id.cb_select);
            mName = (TextView) findViewById(R.id.btn_name);
            mData = (TextView) findViewById(R.id.tv_data);
            mExpires = (TextView) findViewById(R.id.tv_expires);
            mRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
            mVisitProfile = (Button) findViewById(R.id.btn_visit_profile);
            mSwipeLayout = findViewById(R.id.swipe_layout);
            mRow = findViewById(R.id.row);

            mRow.setOnClickListener(expand());

            if(mContact != null) setContact(mContact);

            mExpandableLayout = findViewById(R.id.expandable_contact);

            mRevokeAccess.setOnClickListener(displayRevokeAccessDialog(mContact));
            mVisitProfile.setOnClickListener(startContactActivity(mContact));
            mRow.setOnLongClickListener(DisplayCheckBoxes);
        }

        private View.OnClickListener expand(){
            return new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()){
                        mExpandableLayout.collapse();
                        WiPermittedContactsViewListItem.this.setBackgroundResource(R.color.background_material_dark);
                    }
                    else{
                        mExpandableLayout.expand();
                        WiPermittedContactsViewListItem.this.setBackgroundResource(R.color.themedarker);
                    }
                }
            };
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

        private View.OnClickListener displayRevokeAccessDialog(final WiContact contact){
            return new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getContext())
                            .title("Revoke Access for " + contact.getName())
                            .content("Are you want to revoke access for " + contact.getName() + "? This action cannot be undone.")
                            .negativeText("Cancel")
                            .positiveText("Revoke")
                            .onPositive(revokeAccess(contact))
                            .show();
                }
            };
        }

        private MaterialDialog.SingleButtonCallback revokeAccess(final WiContact contact){
            return new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    //removeListItem(WiPermittedContactsViewListItem.this);

                    Log.d("WiPermittedContact", "Revoking access for " + mNetwork.SSID);
                    contact.revokeAccess(mNetwork.SSID);
                    WiContactList.getInstance(getContext()).save(contact);
                    WiRevokeAccessDataMessage msg = new WiRevokeAccessDataMessage(mNetwork, contact.getPhone());
                    WiDataMessageController.getInstance(getContext().getApplicationContext()).send(msg);
                }
            };
        }

        public void setContact(WiContact contact){
            mName.setText(mContact.getName());

            int hours = mContact.getExpiresIn() / 24;
            int min = mContact.getExpiresIn() - (24 * hours);

            String expires = mContact.getExpiresIn() == -1 ? "-" : hours + "h " + min + "m";
            String data = mContact.getDataUsage() == -1 ? "-" : mContact.getDataUsage() + " Gb";

            mData.setText(data);
            mExpires.setText(expires);
        }

        public WiContact getContact() {
            return mContact;
        }

        private View.OnLongClickListener DisplayCheckBoxes = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(40);

                setCheckBoxVisibilities(VISIBLE);

                return true;
            }
        };
    }
}
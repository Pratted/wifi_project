package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
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
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Predicate;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class WiPermittedContactsView extends WiPage{
    private Button mHeaderName;
    private Button mHeaderData;
    private Button mHeaderExpires;

    private Button mBtnLhs;
    private Button mBtnRhs;

    public static final int COL_NAME = 0;
    public static final int COL_DATA = 1;
    public static final int COL_EXPIRES = 2;

    private HashMap<Integer, Boolean> mSortCriteria;

    private ArrayList<WiPermittedContactsViewListItem> mPermittedContacts;
    private WiConfiguration mNetwork;

    public WiPermittedContactsView(Context context, Button lhs, Button rhs, WiConfiguration network) {
        super(context);

        mSortCriteria = new HashMap<>();
        mSortCriteria.put(COL_NAME, false);
        mSortCriteria.put(COL_DATA, false);
        mSortCriteria.put(COL_EXPIRES, false);

        mBtnLhs = lhs;
        mBtnRhs = rhs;
        mNetwork = network;

        init();

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) refresh();
            }
        });
    }

    protected void init(){
        inflate(getContext(), R.layout.layout_permitted_contacts, this);
        mPermittedContacts = new ArrayList<>();

        mHeaderName = (Button) findViewById(R.id.btn_name);
        mHeaderData = (Button) findViewById(R.id.btn_data);
        mHeaderExpires = (Button) findViewById(R.id.btn_expires);

        mHeaderName.setOnClickListener(sort(COL_NAME, 0));
        mHeaderData.setOnClickListener(sort(COL_DATA, 0));
        mHeaderExpires.setOnClickListener(sort(COL_EXPIRES, 0));
    }

    public void addPermittedContact(WiContact contact){
        WiPermittedContactsViewListItem item = new WiPermittedContactsViewListItem(getContext(), contact);

        mPermittedContacts.add(item);
        addListItem(item);
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
                                removeListItem(wiPermittedContactsViewListItem);
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

        int x = 0;
        removeAllItems();

        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            addListItem(contact);
        }
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
    }

    private void setButtonVisibilities(int visibility){
        mBtnLhs.setVisibility(visibility);
        mBtnRhs.setVisibility(visibility);
    }

    @Override
    public void refresh(){
        mBtnLhs.setText("Done");
        mBtnRhs.setText("Revoke");

        mBtnLhs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckBoxVisibilities(INVISIBLE);
                setButtonVisibilities(GONE);
            }
        });

        mBtnRhs.setOnClickListener(displayMultiRevokeAccessDialog());
    }

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
                            .title("Revoke Access to " + mNetwork.getSSID())
                            .content("Are you want to revoke access for " + qty + contacts + "? This action cannot be undone.")
                            .negativeText("Cancel")
                            .positiveText("Revoke")
                            .onPositive(removeSelectedContacts())
                            .show();
                }
            }
        };
    }

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

            //mName.setOnLongClickListener(onLongClick());
            mRow.setOnLongClickListener(onLongClick());
            mRow.setOnClickListener(expand());

            if(mContact != null) setContact(mContact);

            mExpandableLayout = findViewById(R.id.expandable_contact);

            //mRow.setOnClickListener(expand());

            mRevokeAccess.setOnClickListener(displayRevokeAccessDialog(mContact));
            mVisitProfile.setOnClickListener(startContactActivity(mContact));
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
                            .onPositive(removeContact(WiPermittedContactsViewListItem.this))
                            .show();
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

        private View.OnLongClickListener onLongClick(){
            return new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Vibrate and display checkboxes if not already displayed
                    if(getCheckBoxVisibilties() == INVISIBLE){
                        Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibe.vibrate(10);

                        setCheckBoxVisibilities(VISIBLE);
                        setButtonVisibilities(VISIBLE);
                    }

                    return false;
                }
            };
        }

        private MaterialDialog.SingleButtonCallback removeContact(final WiPermittedContactsViewListItem item){
            return new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    removeListItem(item);
                }
            };
        }
    }
}
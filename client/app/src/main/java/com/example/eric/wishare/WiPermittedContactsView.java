package com.example.eric.wishare;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Comparator;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class WiPermittedContactsView extends LinearLayout {

    private CheckBox mHeaderSelectAll;
    private Button mHeaderName;
    private Button mHeaderData;
    private Button mHeaderExpires;

    private LinearLayout mHeaders;
    private LinearLayout mItems;

    private boolean mAscendingName;

    private ArrayList<WiPermittedContactsViewListItem> mPermittedContacts;

    public interface OnSelectContactsEnabledListener {
        void onSelectContactsEnabled();
    }

    public interface OnSelectContactsDisabledListener {
        void onSelectContactsDisabled();
    }

    private OnSelectContactsEnabledListener mContactsEnabledListener;
    private OnSelectContactsDisabledListener mContactsDisabledListener;

    public void setOnContactsEnabledListener(OnSelectContactsEnabledListener listener){
        mContactsEnabledListener = listener;
    }

    public void setOnContactsDisabledListener(OnSelectContactsDisabledListener listener){
        mContactsDisabledListener = listener;
    }

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

    public void showAllCheckBoxes(){
        for(WiPermittedContactsViewListItem child: mPermittedContacts){
            child.mCheckBox.setVisibility(VISIBLE);
        }

        if(mContactsEnabledListener != null){
            mContactsEnabledListener.onSelectContactsEnabled();
        }
    }

    public int getSelectedContactCount(){
        int count = 0;
        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            count += contact.mCheckBox.isChecked() ? 1 : 0;
        }
        return count;
    }

    public void hideAllCheckBoxes(){
        mHeaderSelectAll.setVisibility(INVISIBLE);
        mHeaderSelectAll.setChecked(false); // reset select all checkbox

        for(WiPermittedContactsViewListItem child: mPermittedContacts){
            child.mCheckBox.setChecked(false); // reset the checkbox...
            child.mCheckBox.setVisibility(INVISIBLE);
        }

        if(mContactsDisabledListener != null){
            mContactsDisabledListener.onSelectContactsDisabled();
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
                            o1.mContact.getName().compareTo(o2.mContact.getName()) :
                            o2.mContact.getName().compareTo(o1.mContact.getName());
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
        for(WiPermittedContactsViewListItem contact: mPermittedContacts){
            contact.setVisibility(contact.mContact.getName().toLowerCase().contains(searchString.toLowerCase()) ? VISIBLE : GONE);
        }
    }

    private class WiPermittedContactsViewListItem extends LinearLayout {

        private CheckBox mCheckBox;
        private Button mName;
        private TextView mData;
        private TextView mExpires;
        private Button mRevokeAccess;
        private Button mVisitProfile;

        private WiContact mContact;

        private ExpandableLayout mExpandableLayout;
        private SwipeLayout mSwipeLayout;

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
            mRevokeAccess = (Button) findViewById(R.id.btn_revoke_all_access);
            mVisitProfile = (Button) findViewById(R.id.btn_visit_profile);
            mSwipeLayout = findViewById(R.id.swipe_layout);

            mName.setOnLongClickListener(onLongClick());

            if(mContact != null) setContact(mContact);

            mExpandableLayout = findViewById(R.id.expandable_contact);

            mName.setOnClickListener(expand());
            mData.setOnClickListener(expand());
            mExpires.setOnClickListener(expand());

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
                            .show();
                }
            };
        }

        public void setContactClickable(boolean clickable){
            mName.setOnClickListener(clickable ? startContactActivity(mContact) : resetOnClick());
        }

        public void setContact(WiContact contact){
            mName.setText(mContact.getName());
            mData.setText("10 Gb");
            mExpires.setText("3d 2h");

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
                    Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(40);
                    setContactsClickable(false);
                    showAllCheckBoxes();
                    mHeaderSelectAll.setVisibility(VISIBLE);
                    return false;
                }
            };
        }
    }
}

package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.ContactActivity;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.dialog.WiCancelInvitationDialog;
import com.example.eric.wishare.dialog.WiCreateInvitationDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.messaging.WiInvitationDataMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

public class WiInvitableContactsView extends LinearLayout {

    private LinearLayout mItems;
    private ArrayList<WiInvitableContactListItem> mInvitableContacts;

    private CheckBox mHeaderSelectAll;
    private Button mHeaderName;
    private Button mButtonCancel;
    private Button mButtonInviteSelected;

    private boolean mAscendingName;

    private WiConfiguration mWiConfiguration;

    public WiInvitableContactsView(Context context, WiConfiguration config) {
        super(context);
        mWiConfiguration = config;

        init();
    }

    public WiInvitableContactsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_invitable_contacts, this);
        mInvitableContacts = new ArrayList<>();

        mItems = findViewById(R.id.items);
        mHeaderSelectAll = findViewById(R.id.cb_select_all);
        mHeaderName = findViewById(R.id.btn_name);

        mButtonCancel = findViewById(R.id.btn_cancel);
        mButtonInviteSelected = findViewById(R.id.btn_invite_selected);
        mButtonCancel.setVisibility(View.VISIBLE);
        mButtonInviteSelected.setVisibility(View.VISIBLE);

        mHeaderSelectAll.setOnCheckedChangeListener(SelectAll);
        mHeaderName.setOnClickListener(SortName);
        mButtonCancel.setOnClickListener(Cancel);

        setCheckBoxVisibilities(View.INVISIBLE);
    }

    public void add(WiContact contact){
        WiInvitableContactListItem item = new WiInvitableContactListItem(getContext(), contact);

        mInvitableContacts.add(item);
        mItems.addView(item);

        paintRows();
    }

    public void filter(String searchString) {
        for(WiInvitableContactListItem contact: mInvitableContacts){
            contact.setVisibility(contact.mContact.getName().toLowerCase().contains(searchString.toLowerCase()) ? VISIBLE : GONE);
        }

        paintRows();
    }

    private CompoundButton.OnCheckedChangeListener SelectAll = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            for(WiInvitableContactListItem child: mInvitableContacts){
                child.mCheckBox.setChecked(isChecked);
            }
        }
    };

    private View.OnClickListener SortName = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            sortName(mAscendingName);
        }
    };

    private View.OnClickListener Cancel = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            setCheckBoxVisibilities(INVISIBLE);
        }
    };

    public void sortName(final boolean ascending){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mInvitableContacts.sort(new Comparator<WiInvitableContactListItem>() {
                @Override
                public int compare(WiInvitableContactListItem o1, WiInvitableContactListItem o2) {
                    return ascending ?
                            o1.mContact.getName().compareTo(o2.mContact.getName()) :
                            o2.mContact.getName().compareTo(o1.mContact.getName());
                }
            });
        }

        mAscendingName = !ascending;
        mItems.removeAllViewsInLayout();
        for(WiInvitableContactListItem contact: mInvitableContacts){
            mItems.addView(contact);
        }

        paintRows();
    }

    public void setCheckBoxVisibilities(int visibility){
        mButtonInviteSelected.setVisibility(visibility);
        mButtonCancel.setVisibility(visibility);
        mHeaderSelectAll.setVisibility(visibility);
        mHeaderSelectAll.setChecked(false);

        for(WiInvitableContactListItem child: mInvitableContacts){
            if(visibility == VISIBLE){
                if(child.hasPendingInvitation()){
                    child.mCheckBox.setVisibility(View.GONE);
                }
                else{
                    child.mImagePlaceHolder.setVisibility(GONE);
                    child.mCheckBox.setVisibility(VISIBLE);
                }
            }
            else{
                child.mCheckBox.setVisibility(GONE);
                child.mImagePlaceHolder.setVisibility(VISIBLE);
            }
        }
    }

    private void paintRows(){
        boolean alt = false;

        for(WiInvitableContactListItem item: mInvitableContacts){
            if(item.getVisibility() == VISIBLE){
                item.setBackgroundResource(alt ? R.color.themedarkest : R.color.themedarkGreen);
                alt = !alt;
            }
        }
    }

    private class WiInvitableContactListItem extends LinearLayout {
        private WiContact mContact;
        private CheckBox mCheckBox;

        private Button mButtonInvite;
        private ImageButton mVisitProfile;
        private ImageView mImagePlaceHolder;

        private Animation mSwipeLeftAnimation;

        public WiInvitableContactListItem(Context context) {
            super(context);
        }

        public WiInvitableContactListItem(Context context, WiContact contact){
            super(context);

            mContact = contact;
            init();
        }

        public WiInvitableContactListItem(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public boolean hasPendingInvitation(){
            return mContact.hasPendingInvitation(mWiConfiguration.SSID);
        }

        private void init(){
            inflate(getContext(), R.layout.layout_invitable_contacts_list_item, this);

            mCheckBox = findViewById(R.id.cb_select);
            mImagePlaceHolder = findViewById(R.id.iv_placeholder);
            mButtonInvite = findViewById(R.id.btn_invite_contact);
            mVisitProfile = findViewById(R.id.btn_visit_profile);

            mButtonInvite.setText(mContact.getName());
            mCheckBox.setVisibility(GONE);

            mButtonInvite.setOnLongClickListener(DisplayCheckBoxes);
            mButtonInvite.setOnClickListener(DisplayInvitationDialog);
            mVisitProfile.setOnClickListener(StartContactActivity);

            mSwipeLeftAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.swipe_left);
            mSwipeLeftAnimation.setAnimationListener(AnimateSlideLeft);

            if(mContact.hasPendingInvitation(mWiConfiguration.SSID)){
                configureWithPendingInvitation();
            }
        }

        private void configureWithPendingInvitation(){
            mCheckBox.setChecked(false);
            mCheckBox.setVisibility(GONE);
            mImagePlaceHolder.setImageResource(R.drawable.ic_empty);
            mImagePlaceHolder.setVisibility(View.VISIBLE);

            mButtonInvite.setOnClickListener(CancelInvitation);
        }

        private Animation.AnimationListener AnimateSlideLeft = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCheckBox.setVisibility(GONE);
                mImagePlaceHolder.setVisibility(VISIBLE);
                mImagePlaceHolder.setImageResource(R.drawable.ic_full);
                mCheckBox.setChecked(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCheckBox.setChecked(false);
                mImagePlaceHolder.setImageResource(R.drawable.ic_empty);
                mButtonInvite.setText("Invitation Sent!");

                configureWithPendingInvitation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        private View.OnClickListener CancelInvitation = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                LinearLayout customLayout = (LinearLayout) inflater.inflate(R.layout.layout_view_pending_invitation, null);

                WiInvitation invitation = mContact.getPendingInvitation(mWiConfiguration.SSID);

                ((TextView) customLayout.findViewById(R.id.tv_expires)).setText(invitation.expires);
                ((TextView) customLayout.findViewById(R.id.tv_data)).setText(invitation.dataLimit);

                new MaterialDialog.Builder(getContext())
                        .title("You already invited " + mContact.getName() + " to " + mWiConfiguration.getSSIDNoQuotes())
                        .customView(customLayout, false)
                        .negativeText("Cancel Invitation")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mContact.removePendingInvitation(mWiConfiguration.SSID);
                                WiContactList.getInstance(getContext()).save(mContact);
                                mImagePlaceHolder.setImageResource(R.drawable.ic_person_black_24dp);
                                mButtonInvite.setOnClickListener(DisplayInvitationDialog);
                            }
                        })
                        .positiveText("Ok")
                        .show();
            };
        };

        private View.OnClickListener DisplayInvitationDialog = new OnClickListener() {
                @Override
                public void onClick(View v) {

                    WiCreateInvitationDialog dialog = new WiCreateInvitationDialog(getContext(), mWiConfiguration.SSID);
                    dialog.setOnInvitationCreatedListener(new WiCreateInvitationDialog.OnInvitationCreatedListener() {
                        @Override
                        public void onInvitationCreated(WiInvitation invitation) {
                            invitation.setWiConfiguration(mWiConfiguration);
                            mContact.invite(invitation); // invite the contact by giving it the invitation

                            // save this information in the contact list and in the database.
                            WiContactList.getInstance(getContext()).save(mContact);

                            WiInvitationDataMessage msg = new WiInvitationDataMessage(invitation, mContact) {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            };

                            System.out.println("The phone is: " + mContact.getPhone());
                            WiDataMessageController.getInstance(getContext()).send(msg);

                            mButtonInvite.startAnimation(mSwipeLeftAnimation);

                        }
                    });
                    dialog.show();
                }
        };

        private View.OnClickListener StartContactActivity = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ContactActivity.class);
                    intent.putExtra("contact", mContact.getPhone());
                    getContext().startActivity(intent);
                }
        };

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
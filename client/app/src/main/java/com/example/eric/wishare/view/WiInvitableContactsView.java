package com.example.eric.wishare.view;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eric.wishare.ContactActivity;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.dialog.WiCancelInvitationDialog;
import com.example.eric.wishare.dialog.WiCreateInvitationDialog;
import com.example.eric.wishare.dialog.WiInviteContactToNetworkDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.messaging.WiInvitationDataMessage;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class WiInvitableContactsView extends LinearLayout {

    private LinearLayout mItems;
    private ArrayList<WiInvitableContactListItem> mInvitableContacts;

    private CheckBox mHeaderSelectAll;
    private Button mHeaderName;
    private Button mName;

    private LinearLayout mHeaders;
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
        mHeaders = findViewById(R.id.headers);
        mHeaderSelectAll = findViewById(R.id.cb_select_all);
        mHeaderName = findViewById(R.id.btn_name);

        mHeaderSelectAll.setOnCheckedChangeListener(onSelectAll());
        mHeaderName.setOnClickListener(sortName());
    }

    public void add(WiContact contact){
        WiInvitableContactListItem item = new WiInvitableContactListItem(getContext(), contact);

        mInvitableContacts.add(item);
        mItems.addView(item);
    }

    public void filter(String searchString) {
        for(WiInvitableContactListItem contact: mInvitableContacts){
            contact.setVisibility(contact.mContact.getName().toLowerCase().contains(searchString.toLowerCase()) ? VISIBLE : GONE);
        }
    }

    private CompoundButton.OnCheckedChangeListener onSelectAll(){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(WiInvitableContactListItem child: mInvitableContacts){
                    child.mCheckBox.setChecked(isChecked);
                }
            }
        };
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
    }

    public void setCheckBoxVisibilities(int visibility){
        mHeaderSelectAll.setVisibility(visibility);
        mHeaderSelectAll.setChecked(false);

        for(WiInvitableContactListItem child: mInvitableContacts){
            // if they have a pending invitation the checkbox is effectively disabled
            if(child.hasPendingInvitation()){
                child.mCheckBox.setVisibility(View.GONE);
            }
            else{
                child.mCheckBox.setVisibility(visibility);
            }
        }
    }

    private class WiInvitableContactListItem extends LinearLayout {
        private WiContact mContact;

        private CheckBox mCheckBox;
        private Button mName;
        private ExpandableLayout mExpandableLayout;

        private TextView mTitle;
        private Button mInvite;
        private Button mVisitProfile;
        private CheckBox mHourglass;

        private SwipeLayout mSwipeLayout;
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
            mHourglass = findViewById(R.id.cb_select_hourglass);
            mName = findViewById(R.id.btn_name);
            mExpandableLayout = findViewById(R.id.expandable_contact);
            mTitle = findViewById(R.id.title);
            mInvite = findViewById(R.id.btn_invite_contact);
            mVisitProfile = findViewById(R.id.btn_visit_profile);
            mSwipeLayout = findViewById(R.id.swipe_layout);

            mName.setText(mContact.getName());
            mTitle.setText(mContact.getName() + " doesn't have access to any networks");

            mSwipeLayout.setRightSwipeEnabled(false);
            mCheckBox.setVisibility(GONE);

            mName.setOnClickListener(ExpandOrCollapse);
            mName.setOnLongClickListener(DisplayCheckBoxes);
            mInvite.setOnClickListener(DisplayInvitationDialog);
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
            mHourglass.setButtonDrawable(R.drawable.ic_empty);
            mHourglass.setVisibility(View.VISIBLE);

            mInvite.setText("View Invitation");
            mInvite.setOnClickListener(CancelInvitation);
        }

        private Animation.AnimationListener AnimateSlideLeft = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCheckBox.setVisibility(GONE);
                mHourglass.setVisibility(VISIBLE);
                mHourglass.setButtonDrawable(R.drawable.ic_full);
                mCheckBox.setChecked(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCheckBox.setChecked(false);
                mHourglass.setButtonDrawable(R.drawable.ic_empty);
                mName.setText("Invitation Sent!");

                if(mExpandableLayout.isExpanded()) {
                    mExpandableLayout.collapse();
                }

                configureWithPendingInvitation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        private View.OnClickListener ExpandOrCollapse = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()){
                        mExpandableLayout.collapse();
                    }
                    else{
                        mExpandableLayout.expand();
                    }
                }
            };

        private View.OnClickListener CancelInvitation = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new WiCancelInvitationDialog(getContext(), mWiConfiguration.SSID, mContact.getPhone()).show();
            };
        };

        private View.OnClickListener DisplayInvitationDialog = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()){
                        mExpandableLayout.collapse();
                    }

                    // TODO: notify user client has already been invited
                    if(mContact.hasPendingInvitation(mWiConfiguration.SSID)){
                        new WiCancelInvitationDialog(getContext(), mWiConfiguration.SSID, mContact.getPhone()).show();
                        return;
                    }

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

                            mName.startAnimation(mSwipeLeftAnimation);

                        }
                    });
                    dialog.show();
                }
        };

        private View.OnClickListener StartContactActivity = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ContactActivity.class);
                    intent.putExtra("contact", mContact);
                    getContext().startActivity(intent);
                }
        };

        private View.OnLongClickListener DisplayCheckBoxes = new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(40);

                    setCheckBoxVisibilities(VISIBLE);
                    return false;
                }
        };
    }
}
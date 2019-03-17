package com.example.eric.wishare;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eric.wishare.dialog.WiCreateInvitationDialog;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import net.cachapa.expandablelayout.ExpandableLayout;

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
    private boolean mMultiSelectEnabled;

    public interface OnCheckBoxVisibilitiesChangedListener {
        void onCheckBoxVisibilitiesChanged(int visibilty);
    }

    private OnCheckBoxVisibilitiesChangedListener mCheckBoxVisibilitiesChangedListener;


    public WiInvitableContactsView(Context context) {
        super(context);

        init();
    }

    public WiInvitableContactsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }


    public void animateLeftSwipe() {
        for(WiInvitableContactListItem child: mInvitableContacts){
            if(child.mCheckBox.isChecked()){
                child.mName.startAnimation(child.mSwipeLeftAnimation);
            }
        }
    }
    public void setOnCheckBoxVisibilitiesChangedListener(OnCheckBoxVisibilitiesChangedListener listener){
        mCheckBoxVisibilitiesChangedListener = listener;
    }

    public int getSelectedContactCount(){
        int count = 0;
        for(WiInvitableContactListItem contact: mInvitableContacts){
            count += contact.mCheckBox.isChecked() ? 1 : 0;
        }
        return count;
    }

    private void init(){
        inflate(getContext(), R.layout.layout_invitable_contacts, this);
        mInvitableContacts = new ArrayList<>();
        mMultiSelectEnabled = false;

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

        // checkboxes turned on -> multiSelection is possible...
        mMultiSelectEnabled = (visibility == VISIBLE);

        for(WiInvitableContactListItem child: mInvitableContacts){
            child.mCheckBox.setVisibility(visibility);
        }

        if(mCheckBoxVisibilitiesChangedListener != null){
            mCheckBoxVisibilitiesChangedListener.onCheckBoxVisibilitiesChanged(visibility);
        }
    }

    private class WiInvitableContactListItem extends LinearLayout {
        private WiContact mContact;

        private CheckBox mCheckBox;
        private Button mName;
        private ExpandableLayout mExpandableLayout;

        private TextView mTitle;
        private LinearLayout mItems;
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

        private void init(){
            inflate(getContext(), R.layout.layout_invitable_contacts_list_item, this);

            mCheckBox = findViewById(R.id.cb_select);
            mHourglass = findViewById(R.id.cb_select_hourglass);
            mName = findViewById(R.id.btn_name);

            mExpandableLayout = findViewById(R.id.expandable_contact);
            mTitle = findViewById(R.id.title);
            mItems = findViewById(R.id.items);
            mInvite = findViewById(R.id.btn_invite_contact);
            mVisitProfile = findViewById(R.id.btn_visit_profile);
            mSwipeLayout = findViewById(R.id.swipe_layout);
            mSwipeLayout.setRightSwipeEnabled(false);

            mSwipeLeftAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.swipe_left);
            mSwipeLeftAnimation.setAnimationListener(new Animation.AnimationListener() {
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
                    mName.setOnClickListener(doNothing());
                    if(mExpandableLayout.isExpanded()) {
                        mExpandableLayout.collapse();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //setAnimation(mSwipeLeftAnimation);

            mName.setText(mContact.getName());
            mTitle.setText(mContact.getName() + " doesn't have access to any networks");

            mCheckBox.setVisibility(INVISIBLE);

            mName.setOnLongClickListener(onLongClick());
            mName.setOnClickListener(expand());
            mInvite.setOnClickListener(displayInvitationDialog());
            mVisitProfile.setOnClickListener(startContactActivity());
        }

        private View.OnClickListener doNothing(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            };
        }

        private View.OnClickListener expand(){
            return new OnClickListener() {
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
        }

        private View.OnClickListener displayInvitationDialog(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()){
                        mExpandableLayout.collapse();
                    }

                    WiCreateInvitationDialog dialog = new WiCreateInvitationDialog(getContext());
                    dialog.setOnInvitationCreatedListener(new WiCreateInvitationDialog.OnInvitationCreatedListener() {
                        @Override
                        public void onInviationCreated(WiInvitation invitation) {
                            mName.startAnimation(mSwipeLeftAnimation);
                        }
                    });
                    dialog.show();
                }
            };
        }

        private View.OnClickListener startContactActivity(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ContactActivity.class);
                    intent.putExtra("contact", mContact);
                    getContext().startActivity(intent);
                }
            };
        }

        private View.OnLongClickListener onLongClick(){
            return new OnLongClickListener() {
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
}
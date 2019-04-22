package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiInvitationList;
import com.example.eric.wishare.model.WiInvitation;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

public class WiInvitationListDialog extends WiDialog{
    private ArrayList<WiInvitationListItem> mInvitationListItems = new ArrayList<>();

    private LinearLayout mCustomView;
    private ScrollView mScrollView;
    private LinearLayout mLinearLayout;

    public WiInvitationListDialog(Context context){
        super(context);

        // create an empty layout to place into the dialog...
        mCustomView = new LinearLayout(context);
        mLinearLayout = new LinearLayout(context);
        mScrollView = new ScrollView(context);

        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView.LayoutParams params = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        mScrollView.setLayoutParams(params);

        // .customView()
        //      LinearLayout <- this height automatically gets resized to WRAP_CONTENT
        //          ScrollView <- set a fixed height here to prevent dialog from growing/shrinking
        //              LinearLayout <- the actual container for our WiInvitationListItems
        mScrollView.addView(mLinearLayout);
        mCustomView.addView(mScrollView);
    }

    private void add(WiInvitationListItem invitation){
        mInvitationListItems.add(invitation);
        mLinearLayout.addView(invitation);
    }

    @Override
    public MaterialDialog build() {
        mLinearLayout.removeAllViews(); // remove any existing children (prevents duplicates)

        // get all current invitations...
        List<WiInvitation> invitations = WiInvitationList.getInstance(context.get()).getAllInvitations();

        for(WiInvitation invitation: invitations){
            add(new WiInvitationListItem(context.get(), invitation));
        }

        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context.get())
                .title("My Invitations")
                .positiveText("Ok");

        if(invitations.size() == 0){
            dialog = dialog.content("You have no invitations!");
        }
        else{
            dialog = dialog.customView(mCustomView, false)
                    .negativeText("Clear All")
                    .onNegative(onNegative());
        }
        return dialog.build();
    }

    public MaterialDialog.SingleButtonCallback onNegative(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                for(WiInvitationListItem item: mInvitationListItems){
                    mLinearLayout.removeView(item);
                }
            }
        };
    }

    public void remove(WiInvitation invitation){
        for(int i = 0; i < mInvitationListItems.size(); i++){
            WiInvitation lhs = mInvitationListItems.get(i).mInvitation;

            if (lhs.equals(invitation)) {
                mLinearLayout.removeView(mInvitationListItems.get(i));
                mInvitationListItems.remove(i);

                return;
            }
        }
    }

    private class WiInvitationListItem extends LinearLayout{
        private WiInvitation mInvitation;

        private TextView tvInvitationTitle;
        private TextView tvInvitationOwner;
        private WiInvitationAcceptDeclineDialog mAcceptDeclineDialog;
        private ExpandableLayout mExpandableLayout;
        //private TextView tvInvitationExpires;


        public WiInvitationListItem(Context context, WiInvitation invitation){
            super(context);
            mInvitation = invitation;

            init();
        }

        private void init(){
            inflate(getContext(), R.layout.layout_invitation_list_item_eric, this);

            tvInvitationTitle = findViewById(R.id.tv_invitation_title);
            tvInvitationOwner = findViewById(R.id.tv_invitation_owner);

            mExpandableLayout = findViewById(R.id.expandable_layout_invitation);

            tvInvitationTitle.setText(mInvitation.networkName);
            tvInvitationOwner.setText(mInvitation.sender);

            mAcceptDeclineDialog = new WiInvitationAcceptDeclineDialog(context.get(), mInvitation);
            mAcceptDeclineDialog.setOnAcceptedListener(onAccepted());
            mAcceptDeclineDialog.setOnDeclinedListener(onDeclined());

            this.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()) {
                        mExpandableLayout.collapse();
                    } else {
                        mExpandableLayout.expand();
                    }
                }
            });
        }

        private WiInvitationAcceptDeclineDialog.OnAcceptedListener onAccepted(){
            return new WiInvitationAcceptDeclineDialog.OnAcceptedListener() {
                @Override
                public void onAccepted(WiInvitation invitation) {
                    remove(invitation);
                }
            };
        }

        private WiInvitationAcceptDeclineDialog.OnDeclinedListener onDeclined(){
            return new WiInvitationAcceptDeclineDialog.OnDeclinedListener() {
                @Override
                public void onDeclined(WiInvitation invitation) {
                    remove(invitation);
                }
            };
        }
    }
}
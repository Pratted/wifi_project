package com.example.eric.wishare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.view.WiMyInvitationsButton;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiInvitationListDialog extends WiDialog{
    private LinearLayout mParent;
    private LayoutInflater mInflater;
    private ArrayList<WiInvitationListItem> mInvitations = new ArrayList<>();
    private WiMyInvitationsButton mMyInvitationsButton;

    public WiInvitationListDialog(Context context, WiMyInvitationsButton btnMyInvitations){
        super(context);

        mMyInvitationsButton = btnMyInvitations;

        // create an empty layout to place into the dialog...
        mParent = new LinearLayout(context);
        mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mParent.setOrientation(LinearLayout.VERTICAL);

        mMyInvitationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiInvitationListDialog.this.show();
            }
        });
    }

    public void add(WiInvitation invitation){
        add(new WiInvitationListItem(invitation));
    }

    private void add(WiInvitationListItem invitation){
        mInvitations.add(invitation);
        mMyInvitationsButton.setInvitationCount(mInvitations.size());
    }

    public ArrayList<WiInvitation> getInvitations() {
        ArrayList<WiInvitation> result = new ArrayList<>();
        for (WiInvitationListItem invite : mInvitations){
            result.add(invite.mInvitation);
        }
        return result;
    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("My Invitations")
                .customView(mParent, false)
                .positiveText("Close")
                .build();
    }

    public void remove(WiInvitation invitation){
        for(int i = 0; i < mInvitations.size(); i++){
            WiInvitation lhs = mInvitations.get(i).mInvitation;

            if (lhs.equals(invitation)) {
                mParent.removeView(mInvitations.get(i).mLayout);
                mInvitations.remove(i);

                mMyInvitationsButton.setInvitationCount(mInvitations.size());
                return;
            }
        }
    }

    private class WiInvitationListItem {
        private LinearLayout mLayout;
        private WiInvitation mInvitation;

        private TextView tvInvitationTitle;
        private TextView tvInvitationOwner;
        private WiInvitationAcceptDeclineDialog mAcceptDeclineDialog;
        private ExpandableLayout mExpandableLayout;
        //private TextView tvInvitationExpires;

        public WiInvitationListItem(WiInvitation invitation){
            mInvitation = invitation;

            mLayout = (LinearLayout) mInflater.inflate(R.layout.layout_invitation_list_item, null);
            mParent.addView(mLayout);

            refresh();

            mAcceptDeclineDialog = new WiInvitationAcceptDeclineDialog(context.get(), mInvitation);
            mAcceptDeclineDialog.setOnAcceptedListener(onAccepted());
            mAcceptDeclineDialog.setOnDeclinedListener(onDeclined());

            mLayout.setOnClickListener(new View.OnClickListener() {
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

        public void refresh(){
            tvInvitationTitle = mLayout.findViewById(R.id.tv_invitation_title);
            tvInvitationOwner = mLayout.findViewById(R.id.tv_invitation_owner);

            mExpandableLayout = mLayout.findViewById(R.id.expandable_layout_invitation);

            tvInvitationTitle.setText(String.format("Invitation to '%s'", mInvitation.networkName));
            tvInvitationOwner.setText(mInvitation.sender);
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
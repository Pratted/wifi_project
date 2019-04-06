package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiInvitation;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiInvitationListDialog extends WiDialog{
    private LinearLayout mParent;
    private LayoutInflater mInflater;
    private ArrayList<WiInvitationListItem> mInvitationListItems = new ArrayList<>();
    private ScrollView mParentParent;
    private OnInvitationsUpdatedListener mOnInvitationsUpdatedListener;

    public interface OnInvitationsUpdatedListener {
        void onInvitationsUpdated(List<WiInvitation> invitations);
    }

    public WiInvitationListDialog(Context context){
        super(context);

        mParentParent = new ScrollView(context);
        // create an empty layout to place into the dialog...
        mParent = new LinearLayout(context);
        mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mParent.setOrientation(LinearLayout.VERTICAL);

        mParentParent.addView(mParent);

        ArrayList<WiInvitation> invitations = WiSQLiteDatabase.getInstance(context).loadAllInvitations();
        for(WiInvitation inv: invitations){
            add(inv);
        }
    }

    public void add(WiInvitation invitation){
        add(new WiInvitationListItem(invitation));

        if(mOnInvitationsUpdatedListener != null){
            mOnInvitationsUpdatedListener.onInvitationsUpdated(getInvitations());
        }
    }

    private List<WiInvitation> getInvitations(){
        ArrayList<WiInvitation> invitations = new ArrayList<>();

        for(WiInvitationListItem inv: mInvitationListItems){
            invitations.add(inv.mInvitation);
        }
        return invitations;
    }

    private void add(WiInvitationListItem invitation){
        mInvitationListItems.add(invitation);
    }

    public void setOnInvitationsUpdatedListener(OnInvitationsUpdatedListener listener){
        mOnInvitationsUpdatedListener = listener;
    }

    @Override
    public MaterialDialog build() {
        ArrayList<String> invs = new ArrayList<>();
        for(WiInvitationListItem item: mInvitationListItems){
            invs.add("Invitation to " + item.mInvitation.networkName);
        }

        return new MaterialDialog.Builder(context.get())
                .title("My Invitations")
                .customView(mParentParent, false)
                //.items(invs)
                .negativeText("Clear All")
                .onNegative(onNegative())
                .positiveText("Close")
                .build();
    }

    public MaterialDialog.SingleButtonCallback onNegative(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                for(WiInvitationListItem item: mInvitationListItems){
                    mParent.removeView(item.mLayout);
                }
            }
        };
    }


    public void remove(WiInvitation invitation){
        for(int i = 0; i < mInvitationListItems.size(); i++){
            WiInvitation lhs = mInvitationListItems.get(i).mInvitation;

            if (lhs.equals(invitation)) {
                mParent.removeView(mInvitationListItems.get(i).mLayout);
                mInvitationListItems.remove(i);

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
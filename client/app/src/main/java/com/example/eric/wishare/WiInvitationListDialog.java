package com.example.eric.wishare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiInvitationListDialog {
    private Context mContext;
    private LinearLayout mParent;
    private LayoutInflater mInflater;
    private TextView mNumInvites;

    private ArrayList<WiInvitationListItem> mInvitations = new ArrayList<>();
    private MaterialDialog.Builder mDialog;

    public WiInvitationListDialog(Context context, TextView numInvites){
        mContext = context;
        mNumInvites = numInvites;


        // create an empty layout to place into the dialog...
        mParent = new LinearLayout(context);
        mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mParent.setOrientation(LinearLayout.VERTICAL);

        mDialog = new MaterialDialog.Builder(context)
                .title("My Invitations")
                .customView(mParent, false)
                .positiveText("Close");
    }



    public void add(WiInvitation invitation){
        add(new WiInvitationListItem(invitation));
    }

    private void add(WiInvitationListItem invitation){
        mInvitations.add(invitation);
    }

    public void show(){
        mDialog.show();
    }

    public void remove(WiInvitation invitation){
        for(int i = 0; i < mInvitations.size(); i++){
            WiInvitation lhs = mInvitations.get(i).mInvitation;

            if (lhs.equals(invitation)) {
                mParent.removeView(mInvitations.get(i).mLayout);
                mInvitations.remove(i);

                mNumInvites.setText(mInvitations.size() + "");
                mNumInvites.setVisibility((mInvitations.size() != 0) ? View.VISIBLE : View.INVISIBLE);

                return;
            }
        }
    }

    private class WiInvitationListItem {
        private LinearLayout mLayout;
        private WiInvitation mInvitation;

        private TextView tvInvitationTitle;
        private TextView tvInvitationOwner;
        private TextView tvInvitationExpires;

        public WiInvitationListItem(WiInvitation invitation){
            mInvitation = invitation;

            mLayout = (LinearLayout) mInflater.inflate(R.layout.layout_invitation_list_item, null);
            mParent.addView(mLayout);

            refresh();

            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new WiInvitationAcceptDeclineDialog().show();
                }
            });
        }

        private class WiInvitationAcceptDeclineDialog {
            private ConstraintLayout mLayoutAcceptDeclineDialog;

            private TextView tvAcceptDeclineTitle;
            private TextView tvInvitationOwner;
            private TextView tvInvitationExpires;
            private TextView tvDataLimit;
            private TextView tvTimeLimit;

            private MaterialDialog.Builder builder;

            private WiInvitationAcceptDeclineDialog() {
                mLayoutAcceptDeclineDialog = (ConstraintLayout) mInflater.inflate(R.layout.layout_accept_decline_invitation_dialog, null);
                tvAcceptDeclineTitle = null;

                builder = new MaterialDialog.Builder(mContext)
                        .title(mInvitation.networkName)
                        .customView(mLayoutAcceptDeclineDialog, true)
                        .positiveText("Accept")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                remove(mInvitation);

                            }
                        })
                        .negativeText("Decline")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                remove(mInvitation);
                            }
                        });

            }

            public void show() {
                builder.show();
            }

        }

        public void refresh(){
            tvInvitationTitle = mLayout.findViewById(R.id.tv_invitation_title);
            tvInvitationOwner = mLayout.findViewById(R.id.tv_invitation_owner);
            tvInvitationExpires = mLayout.findViewById(R.id.tv_invitation_expiration);

            tvInvitationTitle.setText(String.format("Invitation to '%s'", mInvitation.networkName));
            tvInvitationOwner.setText(mInvitation.owner);
            tvInvitationExpires.setText(mInvitation.expires);

        }
    }
}

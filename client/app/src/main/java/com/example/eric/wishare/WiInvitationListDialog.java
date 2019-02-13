package com.example.eric.wishare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiInvitationListDialog implements WiDialog{
    private WeakReference<Context> mContext;
    private LinearLayout mParent;
    private LayoutInflater mInflater;
    private TextView mNumInvites;

    private ArrayList<WiInvitationListItem> mInvitations = new ArrayList<>();
    private MaterialDialog mDialog;

    public WiInvitationListDialog(Context context, ArrayList<WiInvitation> invitations, TextView numInvites){
        mContext = new WeakReference<>(context);
        mNumInvites = numInvites;

        // create an empty layout to place into the dialog...
        mParent = new LinearLayout(context);
        mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mParent.setOrientation(LinearLayout.VERTICAL);

        mDialog = new MaterialDialog.Builder(mContext.get())
                .title("My Invitations")
                .customView(mParent, false)
                .positiveText("Close")
                .build();

        for(WiInvitation invitation: invitations){
            add(invitation);
        }
    }

    private void add(WiInvitation invitation){
        add(new WiInvitationListItem(invitation));
    }

    private void add(WiInvitationListItem invitation){
        mInvitations.add(invitation);
    }

    public void show(){
        mDialog.show();
    }

    public void refresh(Context context){
        mContext = new WeakReference<>(context);
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
        //private TextView tvInvitationExpires;

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
            private LinearLayout mLayoutAcceptDeclineDialog;

            private MaterialDialog mDialog;

            private WiInvitationAcceptDeclineDialog() {
                mLayoutAcceptDeclineDialog = (LinearLayout) mInflater.inflate(R.layout.layout_accept_decline_invitation_dialog, null);

                ((TextView) mLayoutAcceptDeclineDialog.findViewById(R.id.tv_time_limit)).setText(mInvitation.timeLimit);
                ((TextView) mLayoutAcceptDeclineDialog.findViewById(R.id.tv_data_limit)).setText(mInvitation.dataLimit);
                ((TextView) mLayoutAcceptDeclineDialog.findViewById(R.id.tv_invitation_expiration)).setText(mInvitation.expires);
                ((TextView) mLayoutAcceptDeclineDialog.findViewById(R.id.tv_invitation_owner)).setText(mInvitation.owner);

                mDialog = new MaterialDialog.Builder(mContext.get())
                        .title(mInvitation.networkName)
                        .customView(mLayoutAcceptDeclineDialog, true)
                        .positiveText("Accept")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                   remove(mInvitation);
                                   Toast.makeText(mContext.get(), mInvitation.networkName + " has been accepted", Toast.LENGTH_LONG).show();
                                   WiNetworkManager.getInstance().add(mInvitation.getWiConfiguration());
                            }
                        })
                        .negativeText("Decline")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                remove(mInvitation);
                            }
                        })
                        .build();

            }

            public void show() {
                mDialog.show();
            }

        }

        public void refresh(){
            tvInvitationTitle = mLayout.findViewById(R.id.tv_invitation_title);
            tvInvitationOwner = mLayout.findViewById(R.id.tv_invitation_owner);
            //tvInvitationExpires = mLayout.findViewById(R.id.tv_invitation_expiration);

            tvInvitationTitle.setText(String.format("Invitation to '%s'", mInvitation.networkName));
            tvInvitationOwner.setText(mInvitation.owner);
            //tvInvitationExpires.setText(mInvitation.expires);
        }
    }
}

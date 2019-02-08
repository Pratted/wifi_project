package com.example.eric.wishare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiInvitationListDialog {
    private Context mContext;
    private LinearLayout mParent;
    private LayoutInflater mInflater;

    private ArrayList<WiInvitationListItem> mInvitations = new ArrayList<>();
    private MaterialDialog.Builder mDialog;

    public WiInvitationListDialog(Context context){
        mContext = context.getApplicationContext();

        // create an empty layout to place into the dialog...
        mParent = new LinearLayout(context);
        mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mParent.setOrientation(LinearLayout.VERTICAL);

        mDialog = new MaterialDialog.Builder(context)
                .title("My Invitations")
                .customView(mParent, false)
                .positiveText("Ok");
    }

    public void add(WiInvitation invitation){
        add(new WiInvitationListItem(invitation));
    }

    public void show(){
        mDialog.show();
    }

    public void remove(WiInvitation invitation){
        for(int i = 0; i < mInvitations.size(); i++){
            WiInvitation lhs = mInvitations.get(i).mInvitation;

            if (lhs.equals(invitation)) {
                mInvitations.remove(i);
                return;
            }
        }
    }

    public int size(){
        return mInvitations.size();
    }

    private void add(WiInvitationListItem invitation){
        mInvitations.add(invitation);
    }

    private class WiInvitationListItem {
        private LinearLayout mLayout;
        private WiInvitation mInvitation;

        private TextView tvInvitationTitle;
        private TextView tvInvitationOwner;
        private TextView tvInvitationExpires;

        public WiInvitationListItem(WiInvitation invitation){
            mInvitation = invitation;

            mLayout = (LinearLayout) mInflater.inflate(R.layout.layout_invitation_list_item, mParent);

            refresh();
        }

        public void refresh(){
            tvInvitationTitle = mLayout.findViewById(R.id.tv_invitation_title);
            tvInvitationOwner = mLayout.findViewById(R.id.tv_invitation_owner);
            tvInvitationExpires = mLayout.findViewById(R.id.tv_invitation_expiration);

            tvInvitationTitle.setText(String.format("Invitation to '%s'", mInvitation.networkName));
            tvInvitationOwner.setText(mInvitation.owner);
            tvInvitationExpires.setText(mInvitation.expires);

            setOnClickListener();
        }

        public void setOnClickListener(){
            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(WiInvitationListItem.this.mInvitation);
                }
            });
        }
    }
}

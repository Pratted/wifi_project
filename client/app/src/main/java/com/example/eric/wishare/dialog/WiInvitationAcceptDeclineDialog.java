package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.model.WiInvitation;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiInvitationAcceptDeclineDialog extends WiDialog {
    private LinearLayout mCustomView;
    private WiInvitation mInvitation;

    interface OnAcceptedListener {
        void onAccepted(WiInvitation invitation);
    }

    interface OnDeclinedListener{
        void onDeclined(WiInvitation invitation);
    }

    private OnAcceptedListener mAcceptListener;
    private OnDeclinedListener mDeclineListener;

    public WiInvitationAcceptDeclineDialog(Context context, WiInvitation invitation) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        mCustomView = (LinearLayout) inflater.inflate(R.layout.layout_accept_decline_invitation_dialog, null);
        mInvitation = invitation;

        ((TextView) mCustomView.findViewById(R.id.tv_time_limit)).setText(invitation.timeLimit);
        ((TextView) mCustomView.findViewById(R.id.tv_data_limit)).setText(invitation.dataLimit);
        ((TextView) mCustomView.findViewById(R.id.tv_invitation_expiration)).setText(invitation.expires);
        ((TextView) mCustomView.findViewById(R.id.tv_invitation_owner)).setText(invitation.owner.getName());
    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title(mInvitation.networkName)
                .customView(mCustomView, true)
                .positiveText("Accept")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(mAcceptListener != null){
                            mAcceptListener.onAccepted(mInvitation);
                        }

                        Toast.makeText(context.get(), mInvitation.networkName + " has been accepted", Toast.LENGTH_LONG).show();
                        //WiNetworkManager.getInstance().add(mInvitation.getWiConfiguration());
                    }
                })
                .negativeText("Decline")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(mDeclineListener != null){
                            mDeclineListener.onDeclined(mInvitation);
                        }
                    }
                })
                .build();
    }

    public void setOnAcceptedListener(OnAcceptedListener listener){
        mAcceptListener = listener;
    }
    public void setOnDeclinedListener(OnDeclinedListener listener){
        mDeclineListener = listener;
    }
}

package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

public class WiCreateInvitationDialog extends WiDialog {
    private LinearLayout mCustomView;

    public interface OnInvitationCreatedListener{
        void onInviationCreated(WiInvitation invitation);
    }

    OnInvitationCreatedListener mOnInvitationCreatedListener;

    public WiCreateInvitationDialog(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCustomView = (LinearLayout) inflater.inflate(R.layout.layout_contact_search, null);
    }

    public void setOnInvitationCreatedListener(OnInvitationCreatedListener listener){
        mOnInvitationCreatedListener = listener;
    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Setup Invitation")
                .customView(mCustomView, true)
                .negativeText("Cancel")
                .positiveText("Invite")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(mOnInvitationCreatedListener != null) {
                            WiContact contact = new WiContact("Eric", "453453");

                            mOnInvitationCreatedListener.onInviationCreated(new WiInvitation("Test Network", contact, "45", "0", "haha"));
                        }
                    }
                })
                .build();
    }
}

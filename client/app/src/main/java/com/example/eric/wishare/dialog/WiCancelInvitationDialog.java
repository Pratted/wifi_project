package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;

import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

public class WiCancelInvitationDialog extends WiDialog {
    private String mPhone;
    private String mSsid;

    public WiCancelInvitationDialog(Context context, String ssid, String phone){
        super(context);
        mPhone = phone;
        mSsid = ssid;
    }

    @Override
    public MaterialDialog build() {
        WiContact contact = WiContactList.getInstance(context.get()).getContactByPhone(mPhone);

        return new MaterialDialog.Builder(context.get())
                .title("You already invited " + contact.getName() + " to " + mSsid)
                .content("Would you like to cancel " + contact.getName() + "'s invitation to " + mSsid + "?")
                .negativeText("No")
                .positiveText("Yes")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        WiContact contact = WiContactList.getInstance(context.get()).getContactByPhone(mPhone);
                        contact.removePendingInvitation(mSsid);
                        WiContactList.getInstance(context.get()).save(contact);
                    }
                })
                .build();
    }
}

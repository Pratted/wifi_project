package com.example.eric.wishare;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;

public class WiCreateInvitationDialog extends WiDialog {
    private LinearLayout mCustomView;

    public WiCreateInvitationDialog(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCustomView = (LinearLayout) inflater.inflate(R.layout.layout_contact_search, null);
    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Setup Invitation")
                .customView(mCustomView, true)
                .negativeText("Cancel")
                .positiveText("Invite")
                .build();
    }
}

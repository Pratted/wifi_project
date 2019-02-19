package com.example.eric.wishare;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

public class WiCreateInvitationDialog extends WiDialog {
    public WiCreateInvitationDialog(Context context) {
        super(context);


    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Hello World")

                .build();
    }
}

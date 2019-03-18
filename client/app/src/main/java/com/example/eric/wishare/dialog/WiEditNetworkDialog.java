package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.model.WiConfiguration;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WiEditNetworkDialog extends WiDialog {

    LinearLayout mCustomView;
    WiConfiguration mConfig;
    WiNetworkManager mNetworkManager;

    public WiEditNetworkDialog(final Context context, WiConfiguration config) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mCustomView = (LinearLayout) inflater.inflate(R.layout.layout_edit_network, null);
        /*
        mNetworkManager = WiNetworkManager.getInstance();
        mCustomView.findViewById(R.id.btn_test_connection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toToast;
                if(mNetworkManager.testConnection(context, mConfig)) {
                    toToast = "Connection successful!";
                } else {
                    toToast = "Error in connection.";
                }

                Toast.makeText(context, toToast, Toast.LENGTH_SHORT).show();
            }
        });
        */

        mConfig = config;
    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Edit " + mConfig.getSSID())
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String newPass = ((EditText)mCustomView.findViewById(R.id.edit_text_change_password)).getText().toString();
                        mConfig.setPassword(newPass);
                        Toast.makeText(context.get(), "Password: " + newPass, Toast.LENGTH_SHORT).show();
                    }
                })
                .customView(mCustomView, true)
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {}
                })
                .build();
    }
}

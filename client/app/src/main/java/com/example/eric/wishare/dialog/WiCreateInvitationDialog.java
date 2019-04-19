package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.view.WiEditTextPermHint;

public class WiCreateInvitationDialog extends WiDialog {
    private LinearLayout mCustomView;
    private String mSSID;
    private WiEditTextPermHint mExpires;
    private WiEditTextPermHint mDataLimit;

    public interface OnInvitationCreatedListener{
        void onInvitationCreated(WiInvitation invitation);
    }

    OnInvitationCreatedListener mOnInvitationCreatedListener;

    public WiCreateInvitationDialog(Context context, String ssid) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCustomView = (LinearLayout) inflater.inflate(R.layout.layout_contact_search, null);

        mExpires = (WiEditTextPermHint) mCustomView.findViewById(R.id.expires);
        mDataLimit = (WiEditTextPermHint) mCustomView.findViewById(R.id.data_limit);

        mSSID = ssid;
    }

    public void setOnInvitationCreatedListener(OnInvitationCreatedListener listener){
        mOnInvitationCreatedListener = listener;
    }

    private int getMinutes(String value){
        switch (value){
            case "Never":
                return -1;
            case "1 hour":
                return 60;
            case "1 day":
                return 1440;
            case "1 week":
                return 10080;
            default:
               return Integer.valueOf(value) * 60;
        }
    }

    private int getDataLimit(String value){
        return -1;
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

                        int expires = getMinutes(mExpires.getCurrentValue());
                        int dataLimit = getDataLimit(mDataLimit.getCurrentValue());

                        WiInvitation inv = new WiInvitation(mSSID, WiUtils.getDevicePhone(), "Never", "None");

                        if(mOnInvitationCreatedListener != null) {
                            mOnInvitationCreatedListener.onInvitationCreated(inv);
                        }
                    }
                })
                .build();
    }
}

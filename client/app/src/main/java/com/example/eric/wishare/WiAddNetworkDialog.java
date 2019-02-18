package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class WiAddNetworkDialog extends WiDialog {
    private WiNetworkManager mManager;
    private ArrayList<String> mNetworks;
    private OnPasswordEnteredListener mListener;

    interface OnPasswordEnteredListener {
        void OnPasswordEntered(WiConfiguration config);
    }

    public void setOnPasswordEnteredListener(OnPasswordEnteredListener listener) {
        mListener = listener;
    }

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Select a Network")
                .items(mNetworks)
                .itemsCallback(onNetWorkSelect())
                .negativeText("Cancel")
                .build();
    }

    public WiAddNetworkDialog(Context context, Button btnAddNetwork){
        super(context);

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(MainActivity.WIFI_SERVICE);
        List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();

        mNetworks = new ArrayList<>();
        for(WifiConfiguration config : wifiList) {
            mNetworks.add(config.SSID.replace("\"", ""));
        }

        btnAddNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiAddNetworkDialog.this.show();
            }
        });

        build();
    }

    private MaterialDialog.ListCallback onNetWorkSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, final CharSequence wifiName) {
                new MaterialDialog.Builder(context.get())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence password) {

                                mListener.OnPasswordEntered(new WiConfiguration(wifiName.toString(), password.toString()));

                                Toast.makeText(context.get(), "Wifi name " + password, Toast.LENGTH_LONG).show();
                            }})
                        .show();
            }
        };
    }
}

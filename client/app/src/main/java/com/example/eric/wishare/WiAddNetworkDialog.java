package com.example.eric.wishare;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WiAddNetworkDialog implements WiDialog {
    private WeakReference<Context> mContext;
    private MaterialDialog mDialog;
    private WiNetworkManager mManager;
    private ArrayList<String> mNetworks;
    private OnPasswordEnteredListener mListener;

    interface OnPasswordEnteredListener {
        void OnPasswordEntered(WiConfiguration config);
    }

    public void setOnPasswordEnteredListener(OnPasswordEnteredListener listener) {
        mListener = listener;
    }


    public WiAddNetworkDialog(final Context context){
        mContext = new WeakReference<>(context);

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(MainActivity.WIFI_SERVICE);
        final List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();
        mNetworks = new ArrayList<>();

        if(wifiList == null) {
            mNetworks.add("Eric Home");
            mNetworks.add("Foo");
            mNetworks.add("Bar");
            mNetworks.add("Club");
        } else {
            for(WifiConfiguration config : wifiList) {
                mNetworks.add(config.SSID.replace("\"", ""));
            }
        }

        mDialog = new MaterialDialog.Builder(context)
                .title("Select Network")
                .items(mNetworks)
                .itemsCallback(onNetWorkSelect())
                .negativeText("Cancel")
                .build();
    }

    public void show(){
        mDialog.show();
    }

    @Override
    public void refresh(Context context) {
        mContext = new WeakReference<>(context);
    }

    private MaterialDialog.ListCallback onNetWorkSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, final CharSequence wifiName) {
                new MaterialDialog.Builder(mContext.get())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence password) {

                                mListener.OnPasswordEntered(new WiConfiguration(wifiName.toString(), password.toString()));

                                Toast.makeText(mContext.get(), "Wifi name " + wifiName, Toast.LENGTH_LONG).show();
                            }})
                        .show();
            }
        };
    }
}

package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.model.WiConfiguration;

import java.util.ArrayList;
import java.util.List;

public class WiAddNetworkDialog extends WiDialog {
    private String TAG = "WiAddNetworkDialog";

    private WiNetworkManager mNetworkManager;
    private OnNetworkReadyListener mOnNetworkReadyListener;

    private List<WifiConfiguration> mUnConfiguredNetworks;

    @Override
    public MaterialDialog build() {
        ArrayList<String> networks = new ArrayList<>();
        mUnConfiguredNetworks = mNetworkManager.getUnConfiguredNetworks();

        // remove quotes from the SSID's for the Material Dialog
        for(WifiConfiguration config: mUnConfiguredNetworks){
            networks.add(config.SSID.replace("\"", ""));
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select a Network")
                .items(networks)
                .itemsCallback(onNetWorkSelect())
                .negativeText("Cancel")
                .build();
    }

    public WiAddNetworkDialog(Context context){
        super(context);
        mNetworkManager = WiNetworkManager.getInstance(context.getApplicationContext());
    }

    private MaterialDialog.ListCallback onNetWorkSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, final int position, final CharSequence wifiName) {
                new MaterialDialog.Builder(context.get())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, final CharSequence password) {
                                // get the original configuration with the quotes
                                WifiConfiguration original = mUnConfiguredNetworks.get(position);
                                final WiConfiguration config = new WiConfiguration(original.SSID, password.toString());

                                // now that the password has been entered the network is configured
                                mNetworkManager.configureNetwork(config);

                                // this callback should start Network Activity
                                if(mOnNetworkReadyListener != null){
                                    mOnNetworkReadyListener.onNetworkReady(config);
                                }
                            }}).show();
            }
        };
    }

    public interface OnNetworkReadyListener {
        void onNetworkReady(WiConfiguration configuration);
    }

    public void setOnNetworkReadyListener(OnNetworkReadyListener listener){
        mOnNetworkReadyListener = listener;
    }
}

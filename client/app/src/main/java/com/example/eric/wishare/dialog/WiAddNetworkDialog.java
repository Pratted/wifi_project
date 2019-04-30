package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.model.WiConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WiAddNetworkDialog extends WiDialog {
    private String TAG = "WiAddNetworkDialog";

    private WiNetworkManager mNetworkManager;
    private OnNetworkReadyListener mOnNetworkReadyListener;

    private List<WifiConfiguration> mUnConfiguredNetworks;
    private LinearLayout mCustomLayout;

    @Override
    public MaterialDialog build() {
        mUnConfiguredNetworks = mNetworkManager.getUnConfiguredNetworks();

        mCustomLayout = new LinearLayout(context.get());
        mCustomLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mUnConfiguredNetworks.sort(new Comparator<WifiConfiguration>() {
                @Override
                public int compare(WifiConfiguration left, WifiConfiguration right) {
                    return left.SSID.toLowerCase().compareTo(right.SSID.toLowerCase());
                }
            });
        }

        // remove quotes from the SSID's for the Material Dialog
        for(WifiConfiguration config: mUnConfiguredNetworks){
            WiAddNetworkListItem item = new WiAddNetworkListItem(context.get(), config.SSID.replace("\"", ""));
            item.setOnClickListener(onNetWorkSelect(config));

            //networks.add(config.SSID.replace("\"", ""));
            mCustomLayout.addView(item);
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select a Network")
                .customView(mCustomLayout, true)
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

    private View.OnClickListener onNetWorkSelect(final WifiConfiguration config) {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new MaterialDialog.Builder(context.get())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, final CharSequence password) {
                                // get the original configuration with the quotes
                                WifiConfiguration original = config;
                                final WiConfiguration config = new WiConfiguration(original.SSID, password.toString());

                                // now that the password has been entered the network is configured
                                mNetworkManager.configureNetwork(config);

                                mCustomLayout.removeView(view);

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

    private class WiAddNetworkListItem extends LinearLayout {
        public WiAddNetworkListItem(Context context, String ssid){
            super(context);

            inflate(context, R.layout.layout_add_network_list_item, this);
            ((TextView) findViewById(R.id.tv_ssid)).setText(ssid);
        }
    }
}

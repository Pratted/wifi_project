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

public class WiAddNetworkDialog extends WiDialog {
    private String TAG = "WiAddNetworkDialog";

    private WiNetworkManager mNetworkManager;
    private OnNetworkReadyListener mOnNetworkReadyListener;

    @Override
    public MaterialDialog build() {
        ArrayList<String> networks = new ArrayList<>();

        // pretty format the SSID's for the Material Dialog
        for(WifiConfiguration config: mNetworkManager.getUnConfiguredNetworks()){
            networks.add(config.SSID.replace("\"", ""));
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select a Network")
                .items(networks)
                .itemsCallback(onNetWorkSelect())
                .negativeText("Cancel")
                .build();
    }

    public WiAddNetworkDialog(Context context, Button btnAddNetwork){
        super(context);
        mNetworkManager = WiNetworkManager.getInstance(context.getApplicationContext());

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
                            public void onInput(@NonNull MaterialDialog dialog, final CharSequence password) {
                                final WiConfiguration config = new WiConfiguration(wifiName.toString(), password.toString());
                                mNetworkManager.configureNetwork(config); // now that the password has been entered the network is configured

                                if(mOnNetworkReadyListener != null){
                                    mOnNetworkReadyListener.onNetworkReady(config);
                                }

                                /*
                                // SSID is in range, try and test the connection...
                                if(mNetworkManager.isSsidInRange(wifiName.toString())){

                                    // Circular Progress dialog. Times out after N seconds if unsuccessful.
                                    final MaterialDialog progressDialog = new MaterialDialog.Builder(context.get())
                                            .progress(true, 100)
                                            .content("Configuring Network...")
                                            .show();

                                    mNetworkManager.setOnTestConnectionCompleteListener(new WiNetworkManager.OnTestConnectionCompleteListener() {
                                        @Override
                                        public void onTestConnectionComplete(boolean success) {
                                            progressDialog.dismiss(); // close the progress dialog. the test is finished
                                            Log.d(TAG, "Success? " + success);

                                            // display a warning prompt telling the user that their credentials may be incorrect...
                                            if(!success){
                                                String msg = "WiShare was unable to connect to the network using the password you provided.";

                                                new MaterialDialog.Builder(context.get())
                                                        .title(config.SSID)
                                                        .content(msg)
                                                        .positiveText("Ok")
                                                        .show();
                                            }

                                            if(mOnNetworkReadyListener != null)
                                                mOnNetworkReadyListener.onNetworkReady(config);
                                        }
                                    });

                                    progressDialog.show();
                                    mNetworkManager.testConnection(wifiName.toString());

                                }
                                // Network is out of range, unable to attempt a connection
                                else {
                                    if(mOnNetworkReadyListener != null){
                                        mOnNetworkReadyListener.onNetworkReady(config);
                                    }
                                }
                                */
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

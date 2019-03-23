package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import java.util.ArrayList;
import java.util.List;

public class WiInviteContactToNetworkDialog extends WiDialog {

    private List<WiConfiguration> mNetworks;
    private WiNetworkManager mNetworkManager;
    private WiContact mContact;
    private OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInviteClick(List<WiConfiguration> networks);
    }

    public WiInviteContactToNetworkDialog(Context context, WiContact contact, Button btnAddContactToNetwork) {
        super(context);
        mContact = contact;
        System.out.println("Contact name: " + mContact.getName());
//        mContact.updateInvitedNetworks(context);
        mNetworkManager = WiNetworkManager.getInstance(context);
//        mNetworks = WiNetworkManager.getConfiguredNetworks(context);
        buildAvailableNetworkList();
        btnAddContactToNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiInviteContactToNetworkDialog.this.show();
            }
        });
    }

    public void setOnInviteClickListener(OnInviteClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MaterialDialog build() {

        ArrayList<String> networkList = new ArrayList<>();

        for (WifiConfiguration configuration : mNetworks) {
            networkList.add(configuration.SSID);
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select networks to add this contact to")
                .items(networkList)
                .itemsCallbackMultiChoice(null, ignore())
                .positiveText("Invite")
                .negativeText("Cancel")
                .onPositive(onInviteClick())
                .build();

    }

    private MaterialDialog.ListCallbackMultiChoice ignore(){
        return new MaterialDialog.ListCallbackMultiChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                return false;
            }
        };
    }

    private void buildAvailableNetworkList() {
        mNetworks = mNetworkManager.getConfiguredNetworks();
        for(WiConfiguration config : mNetworks) {
            System.out.println("IN BUILD Config SSID: " + config.getSSID());
        }

        mNetworks.removeAll(mContact.getInvitedNetworks());

        for(WiConfiguration config : mContact.getInvitedNetworks()) {
            System.out.println("IN BUILD 2 Config SSID: " + config.getSSID());
        }
    }

    private MaterialDialog.SingleButtonCallback onInviteClick(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Integer indices[] = dialog.getSelectedIndices();

                if(indices.length == 1) {
                    System.out.println("SelectedIndex: " + 0);
                    WiConfiguration config = mNetworks.get(indices[0]);
                    System.out.println("Config SSID: " + config.getSSID());
                    mContact.addToInvitedNetworks(config);
                    mNetworks.remove(config);
                } else {
                    for(int i = indices.length - 1; i >= 0; i--) {
                        WiConfiguration config = mNetworks.get(i);
                        System.out.println("Config SSID: " + config.getSSID());
                        mContact.addToInvitedNetworks(config);
                        mNetworks.remove(config);
                    }
                }
                listener.onInviteClick(mContact.getInvitedNetworks());
            }
        };
    }
}

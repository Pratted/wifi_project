package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;

import java.util.ArrayList;

public class WiAddContactToNetworkDialog extends WiDialog {


    private ArrayList<WifiConfiguration> mNetworks;


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
                .onPositive(onInviteClick())
                .positiveText("Invite")
                .negativeText("Cancel")
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

    private MaterialDialog.SingleButtonCallback onInviteClick(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Integer indices[] = dialog.getSelectedIndices();

                for(Integer i: indices){
                    System.out.println("You invited " + mNetworks.get(i).SSID);
                }
            }
        };
    }


    public WiAddContactToNetworkDialog(Context context, Button btnAddContactToNetwork) {
        super(context);

        mNetworks = WiNetworkManager.getConfiguredNetworks(context);


        btnAddContactToNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiAddContactToNetworkDialog.this.show();
            }
        });


    }
}

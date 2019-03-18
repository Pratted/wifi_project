package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.WiNetworkManager;

import java.util.ArrayList;

public class WiRevokeAccessDialog extends WiDialog{

    private ArrayList<WifiConfiguration> mNetworks;

    @Override
    public MaterialDialog build() {

        ArrayList<String> networkList = new ArrayList<>();

        for (WifiConfiguration configuration : mNetworks) {
            networkList.add(configuration.SSID);
        }


        return new MaterialDialog.Builder(context.get())
                .title("Select networks to revoke this contact from")
                .items(networkList)
                .itemsCallbackMultiChoice(null, ignore())
                .onPositive(onRevokeClick())
                .positiveText("Revoke")
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

    private MaterialDialog.SingleButtonCallback onRevokeClick(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Integer indices[] = dialog.getSelectedIndices();

                for(Integer i: indices){
                    System.out.println("You Revoked Joe Schmoe from " + mNetworks.get(i).SSID);
                }
            }
        };
    }

    public WiRevokeAccessDialog(Context context, Button btnRevokeAccess) {
        super(context);

        mNetworks = WiNetworkManager.getConfiguredNetworks(context);

        btnRevokeAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiRevokeAccessDialog.this.show();
            }
        });
    }
}

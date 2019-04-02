package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.messaging.WiInvitationDataMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WiInviteContactToNetworkDialog extends WiDialog {

    private String TAG = "WiInviteContactToNetworkDialog";
    private List<WiConfiguration> mNetworks;
    private WiNetworkManager mNetworkManager;
    private WiContact mContact;
    private OnInviteClickListener listener;

    private WiCreateInvitationDialog mCreateInvitationDialog;

    public interface OnInviteClickListener {
        void onInviteClick(List<WiConfiguration> networks);
    }

    public WiInviteContactToNetworkDialog(Context context, WiContact contact, Button btnAddContactToNetwork) {
        super(context);
        mContact = contact;
        System.out.println("Contact name: " + mContact.getName());

        mNetworkManager = WiNetworkManager.getInstance(context.getApplicationContext());

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

        Log.d(TAG, "mNetworks.isEmpty() " + mNetworks.isEmpty());

        for(WiConfiguration config : mNetworks) {
            Log.d(TAG, "IN BUILD Config SSID: " + config.getSSID());
        }

        mNetworks.removeAll(mContact.getInvitedNetworks());

        Log.d(TAG, "mContact.getInvitedNetworks().isEmpty() " + mContact.getInvitedNetworks().isEmpty());

        for(WiConfiguration config : mContact.getInvitedNetworks()) {
            Log.d(TAG, "IN BUILD 2 Config SSID: " + config.getSSID());
        }
    }

    private List<WiConfiguration> getSelectedNetworks(Integer[] indices){
        ArrayList<WiConfiguration> selected = new ArrayList<>();
        if(indices != null){
            for(Integer i: indices){
                selected.add(mNetworks.get(i));
            }
        }
        return selected;
    }

    private MaterialDialog.SingleButtonCallback onInviteClick(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                final Integer indices[] = dialog.getSelectedIndices();

                mCreateInvitationDialog = new WiCreateInvitationDialog(context.get(), "dummy");
                mCreateInvitationDialog.setOnInvitationCreatedListener(new WiCreateInvitationDialog.OnInvitationCreatedListener() {
                    @Override
                    public void onInvitationCreated(WiInvitation invitation) {

                        for(WiConfiguration config: getSelectedNetworks(dialog.getSelectedIndices())){
                            invitation.networkName = config.getSSID();
                            mNetworks.remove(config);

                            WiInvitationDataMessage msg = new WiInvitationDataMessage(invitation, mContact) {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            };

                            WiDataMessageController.getInstance(context.get()).send(msg);
                        }
                    }
                });

                /*
                WiDataMessage msg = new WiDataMessage(WiDataMessage.MSG_INVITATION);

                if(indices.length == 1) {
                    WiConfiguration config = mNetworks.get(indices[0]);

                    mContact.addToInvitedNetworks(config);
                    mNetworks.remove(config);

                    msg = new WiDataMessage(new WiInvitation(config.getSSID(), WiUtils.getDeviceToken(), "never", "", "500"), mContact);

                    WiDataMessageController.getInstance(context.get()).send(msg);

                } else {
                    for(int i = indices.length - 1; i >= 0; i--) {
                        WiConfiguration config = mNetworks.get(i);

                        mContact.addToInvitedNetworks(config);
                        mNetworks.remove(config);

                        msg = new WiDataMessage(new WiInvitation(config.getSSID(), WiUtils.getDeviceToken(), "never", "", "150"));
                        msg.addRecipient(mContact);

                        WiDataMessageController.getInstance(context.get()).send(msg);
                    }
                }
                */

                /* TODO: put this listener in the class which receives the accept
                   TODO: or decline message from the invited contact
                */

                listener.onInviteClick(mContact.getInvitedNetworks());

                final MaterialDialog spinnyThing = new MaterialDialog.Builder(context.get())
                        .progress(true, 100)
                        .content("Sending Invitation")
                        .canceledOnTouchOutside(false)
                        .show();

                /*
                msg.setOnResponseListener(new WiDataMessage.OnResponseListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        spinnyThing.dismiss();
                    }
                });
                */

//                msg.putRecipient(mContact.getPhone());
//                WiDataMessageController.getInstance(context.get()).send(msg);
            }
        };
    }
}

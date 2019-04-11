package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiDataMessageController;
import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.WiSQLiteDatabase;
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
    private Context mContext;


    public interface OnInviteClickListener {
        void onInviteClick(WiConfiguration config);
    }

    public WiInviteContactToNetworkDialog(Context context, WiContact contact, Button btnAddContactToNetwork) {
        super(context);
        mContext = context.getApplicationContext();
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
                .positiveText("Create Invitation")
                .negativeText("Cancel")
                .onPositive(onInvitationCreateClick())
                .build();
    }

    private MaterialDialog.ListCallbackMultiChoice ignore() {
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

        for (WiConfiguration config : mNetworks) {
            Log.d(TAG, "IN BUILD Config SSID: " + config.getSSID());
        }

        mNetworks.removeAll(mContact.getPermittedNetworks());

        Log.d(TAG, "mContact.getInvitedNetworks().isEmpty() " + mContact.getPermittedNetworks().isEmpty());

        for (WiConfiguration config : mContact.getPermittedNetworks()) {
            Log.d(TAG, "IN BUILD 2 Config SSID: " + config.getSSID());
        }
    }

    private List<WiConfiguration> getSelectedNetworks(Integer[] indices) {
        ArrayList<WiConfiguration> selected = new ArrayList<>();
        if (indices != null) {
            for (Integer i : indices) {
                selected.add(mNetworks.get(i));
            }
        }
        return selected;
    }

    private MaterialDialog.SingleButtonCallback onInvitationCreateClick() {
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                final Integer indices[] = dialog.getSelectedIndices();
                mCreateInvitationDialog = new WiCreateInvitationDialog(context.get(), "dummy");
                mCreateInvitationDialog.setOnInvitationCreatedListener(new WiCreateInvitationDialog.OnInvitationCreatedListener() {
                    @Override
                    public void onInvitationCreated(WiInvitation invitation) {
                        for (WiConfiguration config : getSelectedNetworks(indices)) {
                            Log.d(TAG, "config id: " + config.getSSID());
                            invitation.networkName = config.getSSID();
                            invitation.setWiConfiguration(config);

                            mContact.invite(invitation);

                            WiContactList.getInstance(mContext).save(mContact);
                            WiSQLiteDatabase.getInstance(mContext).insertPendingInvitation(invitation, mContact);

                            Log.d(TAG, "Adding permitted contact to database");

                            WiInvitationDataMessage msg = new WiInvitationDataMessage(invitation, mContact) {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            };
                            WiDataMessageController.getInstance(context.get().getApplicationContext()).send(msg);
                            listener.onInviteClick(config);
                        }
                    }
                });
                mCreateInvitationDialog.show();
            }
        };
    }
}

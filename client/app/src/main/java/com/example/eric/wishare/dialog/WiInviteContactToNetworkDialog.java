package com.example.eric.wishare.dialog;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WiInviteContactToNetworkDialog extends WiDialog {

    private String TAG = "WiInviteContactToNetworkDialog";
    private List<WiConfiguration> mNetworks;
    private WiNetworkManager mNetworkManager;
    private WiContact mContact;
    private OnInviteAcceptListener listener;
    private WiCreateInvitationDialog mCreateInvitationDialog;
    private Context mContext;

    public interface OnInviteAcceptListener {
        void onInviteAccept(WiConfiguration config);
    }

    public WiInviteContactToNetworkDialog(Context context, WiContact contact, Button btnAddContactToNetwork) {
        super(context);
        mContext = context.getApplicationContext();
        mContact = WiContactList.getInstance(context.getApplicationContext()).getContactByPhone(contact.getPhone());
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

    public void setOnInviteAcceptListener(OnInviteAcceptListener listener) {
        this.listener = listener;
    }

    public void inviteIsAccepted(WiConfiguration config) {
        listener.onInviteAccept(config);
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
            Log.d(TAG, "IN BUILD Config SSID: " + config.getSSIDNoQuotes());
        }

        Set<String> SSID = new HashSet<>(WiSQLiteDatabase.getInstance(mContext).getContactNetworks(mContact));
        Log.d(TAG, "SSID.isEmpty() " + SSID.isEmpty());

        for(String ssid : SSID) {
            ssid = ssid.replace("\"", "");
            Log.d(TAG, "SSID: " + ssid);
        }

        for (int i = 0; i < mNetworks.size(); i++) {
            Log.d(TAG, "network SSID: " + mNetworks.get(i).SSID);
            if(SSID.contains(mNetworks.get(i).SSID)){
                Log.d(TAG, "contains");
                mNetworks.remove(i--);
            }
        }

        for(WiConfiguration config : mNetworks) {
            config.SSID = config.SSID.replace("\"", "");
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
                        for (final WiConfiguration config : getSelectedNetworks(indices)) {
                            Log.d(TAG, "config id: " + config.getSSIDNoQuotes());
                            invitation.networkName = config.SSID;
                            invitation.setWiConfiguration(config);

                            mContact.invite(invitation);

                            WiContactList.getInstance(mContext).save(mContact);

                            Log.d(TAG, "Adding permitted contact to database");

                            WiInvitationDataMessage msg = new WiInvitationDataMessage(invitation, mContact) {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            };
                            WiDataMessageController.getInstance(context.get().getApplicationContext()).send(msg);
                            String toastText = mContact.getName() + " has been invited to " + config.getSSIDNoQuotes();
                            Toast.makeText(context.get(), toastText, Toast.LENGTH_LONG).show();
//                            listener.onInviteClick(config);
                        }
                    }
                });
                mCreateInvitationDialog.show();
            }
        };
    }
}

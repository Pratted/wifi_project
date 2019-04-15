package com.example.eric.wishare;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiNotification;
import com.example.eric.wishare.model.messaging.WiConfigurationDataMessage;
import com.example.eric.wishare.model.messaging.WiDataMessage;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.messaging.WiIncomingDataMessage;
import com.example.eric.wishare.model.messaging.WiInvitationDataMessage;
import com.example.eric.wishare.model.WiInvitationNotification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WiMessagingService extends FirebaseMessagingService {
    private static final String TAG = "WiMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    
        System.out.println("DATA RECEIVED" + remoteMessage.toString());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();

            Log.d(TAG, "There are " + data.keySet().size() + "keys");

            WiDataMessage msg = new WiIncomingDataMessage(data);

            switch (msg.getMessageType()){
                case WiDataMessage.MSG_ACKNOWLEDGE:
                    break;

                case WiDataMessage.MSG_INVITATION:
                    onWiInvitationReceived(new WiInvitation(msg)); // constructor accepts JSONObject
                    break;

                case WiDataMessage.MSG_INVITATION_ACCEPTED:
                    onWiInvitationAccepted(new WiInvitation(msg));
                    break;

                case WiDataMessage.MSG_INVITATION_DECLINED:
                    onWiInvitationDeclined(new WiInvitation(msg));
                    break;

                case WiDataMessage.MSG_CREDENTIALS:
                    onCredentialsReceived(new WiConfiguration(msg));
                    break;

                case WiDataMessage.MSG_REVOKE_ACCESS:
                    onAccessYoinked(new WiConfiguration(msg));
                    break;

                default:
                    Log.d(TAG, "Unknown message type received -> " + msg.getMessageType());
                    break;
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }


    public void onAcknowledgeReceived(){

    }

    public void onWiInvitationReceived(final WiInvitation invitation){
        Log.d(TAG, "About to show notification");

        //WiSQLiteDatabase.getInstance(this).insert(invitation);

        /*
        WiInvitationNotification inv = new WiInvitationNotification(this, invitation, WiNotification.REGULAR_NOTIFICATION) {
            @Override
            public void onNotificationClick() {
                String currentActivity = WiSharedPreferences.getString("current_activity", "");

                Intent intent = new Intent("fuck");
                PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // app is not in foreground.
                if(currentActivity.equals("")){
                    Log.d(TAG, "The app is not in the foreground...");
                }

                // main activity is in foreground
                else if(currentActivity.equals(WiUtils.ACTIVITY_MAIN)){
                    Log.d(TAG, "MainActivity is in the foreground...");
                    //sendInvitationToActivity(invitation);
                    //sendMessageToActivity(WiUtils.ACTIVITY_MAIN, "");
                }

                //
                else{

                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    mNotification.setContentIntent(contentIntent);
                }
                else{
                    mOldBuilder.setContentIntent(contentIntent);
                }
            }
        };

        inv.show();
        */

        WiInvitationNotification notification = new WiInvitationNotification(this, invitation, WiNotification.REGULAR_NOTIFICATION);
        notification.show();
        //notification.show();
        Log.d(TAG, "Showing notification");
    }

    public void onWiInvitationAccepted(WiInvitation invitation){
        Log.d(TAG, invitation.sender + " has accepted the invitation");
        WiConfiguration config = WiNetworkManager.getInstance(this).getConfiguredNetwork(invitation.networkName);

        WiConfigurationDataMessage msg = new WiConfigurationDataMessage(config, invitation.sender) {
            @Override
            public void onResponse(JSONObject response) {

            }
        };

        WiDataMessageController.getInstance(this).send(msg);

        WiContact contact = WiContactList.getInstance(this).getContactByPhone(invitation.sender);

        String name = contact != null ? contact.getName() : invitation.sender;
        displayToast(name + " has accepted your invitation to " + invitation.networkName);

        //contact.grantAccess(config);
        //WiSQLiteDatabase.getInstance(this).insert(contact, config);
    }

    public void onWiInvitationDeclined(WiInvitation invitation){

    }

    public void onAccessYoinked(WiConfiguration config){
        Log.d(TAG, "Revoke Access Request Received!");
        Log.d(TAG, "SSID = " + config.SSID);
        Log.d(TAG, "PASSWORD = " + config.getPassword());
    }

    public void onCredentialsReceived(WiConfiguration config){
        Log.d(TAG, "Credentials Received!");

        Log.d(TAG, "SSID = " + config.SSID);
        Log.d(TAG, "PASSWORD = " + config.getPassword());

        sendMessageToActivity(WiUtils.ACTIVITY_MAIN, "Successfully configured " + config.SSID);

        WiNetworkManager.getInstance(this).addConfiguredNetwork(config);
    }

    @Override
    public void onNewToken(String token){
        Log.d(TAG, "The new token is: " + token);

        WiSharedPreferences.putString("token", token);

        // get the phone number, register the device with remote DB
        String phone = WiSharedPreferences.getString("phone", "");

        if(!phone.isEmpty()){
            registerDevice(token, phone);
            WiSharedPreferences.putBoolean("registered", true);
        }
        else{
            Log.d(TAG, "Cannot register device. Phone is empty -> " + phone);
        }

        WiSharedPreferences.save();
    }

    public static void registerDevice(String token, String phone){
        Map<String, Object> record = new HashMap<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        record.put("token", token);
        record.put("phone", phone);
        record.put("date_created", FieldValue.serverTimestamp());

        firestore.collection("devices")
                .document(phone)
                .set(record)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // can't add the token to the DB -> cannot authenticate, (we're fucked)
                        Log.wtf(TAG, "Failed to add token to database", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully added token to database!");
                    }
                });

    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent(WiUtils.ACTIVITY_MAIN);
// You can also include some extra data.
        intent.putExtra("key", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendInvitationToActivity(WiInvitation inv){
        Intent intent = new Intent(WiUtils.ACTIVITY_MAIN);

        intent.putExtra("invitation", inv);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void sendMessageToActivity(String activity, String msg){
        Intent intent = new Intent(activity);
// You can also include some extra data.
        intent.putExtra("key", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void displayToast(final String msg){
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}

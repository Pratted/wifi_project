package com.example.eric.wishare;

import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.model.WiInvitation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
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


            WiDataMessage msg = new WiDataMessage(data);

            if (msg.getMessageType() == WiDataMessage.MSG_ACKNOWLEDGE) {

            }
            if(msg.getMessageType() == WiDataMessage.MSG_INVITATION){
                Log.d(TAG, "About to show notification");
                WiInvitation inv = msg.getWiInvitation();

                WiInvitationNotification notification = new WiInvitationNotification(this, inv);

                notification.show();
                Log.d(TAG, "Showing notification");
            }
            if(msg.getMessageType() == WiDataMessage.MSG_CREDENTIALS){

            }

            /*
            Map<String, String> data = remoteMessage.getData();

            WiNotificationInviteReceived notification = new WiNotificationInviteReceived(this, data.get("title"), data.get("desc"), data);
            notification.show();
            */

            // TODO: handle the data message
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
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
}

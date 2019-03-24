package com.example.eric.wishare;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.model.WiInvitation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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
                WiInvitation inv = msg.getWiInvitation();

                WiNotificationInviteReceived notification = new WiNotificationInviteReceived(this,
                        "WiShare Invitation",
                        "Invitation to " + inv.getNetworkName(),
                        data);
                notification.show();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        // immediately save the new token into shared prefs for future use.
        editor.putString("token", token);
        editor.apply();

        // update the token for future outgoing data messages
        WiDataMessageController.TOKEN = token;

        // get the phone number, register the device with remote DB
        String phone = prefs.getString("phone", "");

        if(!phone.isEmpty()){
            registerDevice(token, phone);
            editor.putBoolean("registered", true);
            editor.commit();
        }
        else{
            Log.d(TAG, "Cannot register device. Phone is empty -> " + phone);
        }
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

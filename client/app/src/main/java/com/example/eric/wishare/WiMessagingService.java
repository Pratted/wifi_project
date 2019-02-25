package com.example.eric.wishare;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

public class WiMessagingService extends FirebaseMessagingService {
    private static final String TAG = "WiMessagingService";
    private static final String SERVER_IP = "127.0.0.1";
    private static final Integer SERVER_PORT = 5000;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        String body = remoteMessage.getNotification().getBody();
        Map<String, String> data = remoteMessage.getData();

        WiNotificationInviteReceived notification = new WiNotificationInviteReceived(this, body, data.get("Nick"));
        notification.show();



        System.out.println("DATA RECEIVED" + remoteMessage.toString());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());



            // TODO: handle the data message
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    // send a message to our server
    public boolean sendMessage(String json){
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            OutputStream os = socket.getOutputStream();
            os.write(json.getBytes());
            return true;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onNewToken(String token){
        Log.d(TAG, "The new token is: " + token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}

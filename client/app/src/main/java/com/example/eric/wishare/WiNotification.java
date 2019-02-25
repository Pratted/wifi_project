package com.example.eric.wishare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.app.NotificationCompat;

import java.util.logging.Handler;

public abstract class WiNotification {
    private NotificationChannel mChannel;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private Notification.Builder mOldBuilder;
    private String mChannelID;

    public WiNotification(Context context, String title, String text){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannelID = "defaultChannel";
            mChannel = new NotificationChannel(mChannelID, "defaultName",
            NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription("This is the default channel description.");

            mNotificationManager.createNotificationChannel(mChannel);
            mNotification = new Notification.Builder(context, mChannelID)
                    .setSmallIcon(R.drawable.ic_wifi_black_48dp)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();
        } else {
            mOldBuilder = new Notification.Builder(context);

            mOldBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_wifi_black_48dp)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentInfo("Info");

            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        }
    }


    abstract public void onTap();
    public void show() {
        onTap();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotificationManager.notify(1, mNotification);
        } else {
            mNotificationManager.notify(1, mOldBuilder.build());
        }
    }
}

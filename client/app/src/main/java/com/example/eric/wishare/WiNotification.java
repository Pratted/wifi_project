package com.example.eric.wishare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.logging.Handler;

public abstract class WiNotification{
    protected String mTitle;
    protected String mText;
    protected NotificationChannel mChannel;
    protected NotificationManager mNotificationManager;
    protected NotificationCompat.Builder mNotification;
    protected NotificationCompat.Builder mOldBuilder;
    protected String mChannelID;


    public WiNotification(Context context, String title, String text){
        mTitle = title;
        mText = text;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannelID = "defaultChannel";
            mChannel = new NotificationChannel(mChannelID, "defaultName",
            NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription("This is the default channel description.");

            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
            mNotification = new NotificationCompat.Builder(context, mChannelID)
                    .setSmallIcon(R.drawable.ic_wifi_black_48dp)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            mOldBuilder = new NotificationCompat.Builder(context);

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

    public String getTitle(){
        return mTitle;
    }
    public String getText(){
        return mText;
    }


    public abstract void setOnNotificationClick();
    public void show() {
        setOnNotificationClick();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotificationManager.notify(1, mNotification.build());
        } else {
            mNotificationManager.notify(1, mOldBuilder.build());
        }
    }
}

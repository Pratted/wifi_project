package com.example.eric.wishare.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.eric.wishare.R;
import com.example.eric.wishare.model.messaging.InvitationNotificationGroup;

import java.util.Random;

public abstract class WiNotification{
    private String TAG = "WiNotification";

    protected String mTitle;
    protected String mText;
    protected NotificationChannel mChannel;
    protected NotificationManager mNotificationManager;
    protected NotificationCompat.Builder mNotification;
    protected NotificationCompat.Builder mOldBuilder;
    protected String mChannelID;
    protected int mNotificationType;
    protected Context mContext;

    public static final int SILENT_NOTIFICATION = 0;
    public static final int REGULAR_NOTIFICATION = 1;


    public WiNotification(Context context, String title, String text, int notificationType){
        mTitle = title;
        mText = text;
        mContext = context;
        mNotificationType = notificationType;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannelID = "defaultChannel";
            mChannel = new NotificationChannel(mChannelID, "defaultName",
            NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription("This is the default channel description.");
            mChannel.setSound(null, null);
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);

            mNotification = buildSilentNotification(title, text);
            if(notificationType != SILENT_NOTIFICATION) {
                mNotification.setDefaults(Notification.DEFAULT_ALL);
            }

        } else {
            mOldBuilder = buildSilentNotification(title, text);
            if(notificationType != SILENT_NOTIFICATION) {
                mOldBuilder.setDefaults(Notification.DEFAULT_ALL);
            }
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    private NotificationCompat.Builder buildSilentNotification(String title, String text) {
        return new NotificationCompat.Builder(mContext, mChannelID)
                .setSmallIcon(R.drawable.ic_wifi_black_48dp)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(null)
                .setGroup(Integer.toString(mNotificationType))
                .setSound(null, 0);
    }

    public String getTitle(){
        return mTitle;
    }
    public String getText(){
        return mText;
    }

    public abstract void onNotificationClick();
    public void show() {
        Log.d(TAG, "SHOWING NOTIFICATION...");
        onNotificationClick();
        //Random r = new Random();
        //int rand = r.nextInt(10000)+1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
/*            if(mNotificationType == 1) {
                InvitationNotificationGroup group = InvitationNotificationGroup.getInstance(mContext, mChannelID);
                mNotificationManager.notify(mNotificationType, group.getBuilder().build());
            }*/
            mNotificationManager.notify(1, mNotification.build());
        } else {
/*            if(mNotificationType == 1) {
                InvitationNotificationGroup group = InvitationNotificationGroup.getInstance(mContext, mChannelID);
                mNotificationManager.notify(mNotificationType, group.getBuilder().build());
            }*/
            mNotificationManager.notify(1, mOldBuilder.build());
        }
    }
}

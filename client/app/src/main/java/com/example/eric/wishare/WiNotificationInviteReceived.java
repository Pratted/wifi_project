package com.example.eric.wishare;

import android.content.Context;

public class WiNotificationInviteReceived extends WiNotification{


    public WiNotificationInviteReceived(Context context, String title, String text) {
        super(context, title, text);
    }

    @Override
    public void setOnNotificationClick() {
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("phone", "+12223334444");
//            PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            mNotification.setContentIntent(contentIntent);
//        }
//        else{
//            mOldBuilder.setContentIntent(contentIntent);
//        }
    }
}

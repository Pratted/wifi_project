package com.example.eric.wishare;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Map;

public class WiNotificationInviteReceived extends WiNotification{
    private Map<String,String> mData;

    public WiNotificationInviteReceived(Context context, String title, String text) {
        super(context, title, text);

    }

    public WiNotificationInviteReceived(Context context, String title, String text, Map<String, String> json) {
        super(context, title, text);

        mData = json;
    }

    @Override
    public void setOnNotificationClick() {
        Intent intent = new Intent(mContext, MainActivity.class);

        if(mData != null){
            for(String key: mData.keySet()){
                intent.putExtra(key, mData.get(key));
            }
        }

        intent.putExtra("inviteNetwork", mTitle);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotification.setContentIntent(contentIntent);
        }
        else{
            mOldBuilder.setContentIntent(contentIntent);
        }
    }
}

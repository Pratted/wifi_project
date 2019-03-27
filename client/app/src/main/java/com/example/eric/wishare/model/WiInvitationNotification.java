package com.example.eric.wishare.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.eric.wishare.MainActivity;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.WiNotification;

import java.util.Map;

public class WiInvitationNotification extends WiNotification {
    private Map<String,String> mData;
    private String TAG = "WiInvitationNotification";

    private WiInvitation mInvitation;

    public WiInvitationNotification(Context context, String title, String text, Map<String, String> json) {
        super(context, title, text);

        mData = json;
    }

    public WiInvitationNotification(Context context, WiInvitation invitation){
        super(context, "WiShare Invitation", "Invitation to " + invitation.getNetworkName());
        mInvitation = invitation;
    }

    @Override
    public void onNotificationClick() {
        Intent intent = new Intent(mContext, MainActivity.class);
        
        Log.d(TAG, "Current Activity " + WiUtils.getCurrentActivity());
        intent.putExtra("invitation", mInvitation);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotification.setContentIntent(contentIntent);
        }
        else{
            mOldBuilder.setContentIntent(contentIntent);
        }
    }
}

package com.example.eric.wishare.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.eric.wishare.ContactActivity;
import com.example.eric.wishare.MainActivity;
import com.example.eric.wishare.NetworkActivity;
import com.example.eric.wishare.SettingsActivity;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.WiNotification;

import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class WiInvitationNotification extends WiNotification {
    private Map<String,String> mData;
    private String TAG = "WiInvitationNotification";

    private WiInvitation mInvitation;

    public WiInvitationNotification(Context context, String title, String text, Map<String, String> json, int type) {
        super(context, title, text, type);

        mData = json;
    }

    public WiInvitationNotification(Context context, WiInvitation invitation, int type){
        super(context, "WiShare Invitation", "Invitation to " + invitation.getNetworkName(), type);
        mInvitation = invitation;
    }

    private Intent getIntentForMostRecentActivity(){
        String currentActivity = WiUtils.getCurrentActivity();

        switch (currentActivity){
            case WiUtils.ACTIVITY_NETWORK:
                return new Intent(mContext, NetworkActivity.class);
            case WiUtils.ACTIVITY_SETTINGS:
                return new Intent(mContext, SettingsActivity.class);
            case WiUtils.ACTIVITY_CONTACT:
                return new Intent(mContext, ContactActivity.class);
            default:
                return new Intent(mContext, MainActivity.class);
        }
    }

    public void onNotificationClick() {
        Log.d(TAG, "The notification was clicked!");
        Intent intent = getIntentForMostRecentActivity();

        //Log.d(TAG, "Current Activity " + WiUtils.getCurrentActivity());
        //intent.putExtra("invitation", mInvitation);
        intent.putExtra("invitation", mInvitation.networkName);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotification.setContentIntent(contentIntent);
        }
        else{
            mOldBuilder.setContentIntent(contentIntent);
        }
    }

}

package com.example.eric.wishare.model.messaging;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

public class InvitationNotificationGroup {
    private static InvitationNotificationGroup invitationNotificationGroup;
    private NotificationCompat.Builder mGroupBuilder;
    
    private InvitationNotificationGroup(Context context, String channelID){
        mGroupBuilder = new NotificationCompat.Builder(context, channelID)
                .setContentTitle("WiShare")
                .setContentText("You've been invited to multiple networks")
                .setGroupSummary(true)
                .setGroup("1");
    }

    public static InvitationNotificationGroup getInstance(Context context, String channelID){
        if (invitationNotificationGroup == null){
            return new InvitationNotificationGroup(context, channelID);
        }
        return invitationNotificationGroup;
    }
    public NotificationCompat.Builder getBuilder(){
        return mGroupBuilder;
    }
}

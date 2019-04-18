package com.example.eric.wishare;

import android.content.Context;

import com.example.eric.wishare.model.WiInvitation;

import java.util.ArrayList;
import java.util.List;

public class WiInvitationList {
    private static WiInvitationList sInstance;
    private ArrayList<WiInvitation> mInvitations;
    private Context mContext;

    private WiInvitationList(Context context){
        mContext = context.getApplicationContext();

        // all existing invitations are loaded here...
        mInvitations = WiSQLiteDatabase.getInstance(context).loadAllInvitations();
    }

    public void add(WiInvitation invitation){
        mInvitations.add(invitation);
        WiSQLiteDatabase.getInstance(mContext).insert(invitation);
    }

    public void remove(WiInvitation invitation){
        mInvitations.remove(invitation);
        WiSQLiteDatabase.getInstance(mContext).delete(invitation);
    }

    public List<WiInvitation> getAllInvitations(){
        return mInvitations;
    }

    public int size(){
        return mInvitations.size();
    }

    public synchronized static WiInvitationList getInstance(Context context){
        if(sInstance == null){
            sInstance = new WiInvitationList(context.getApplicationContext());
        }
        return sInstance;
    }
}

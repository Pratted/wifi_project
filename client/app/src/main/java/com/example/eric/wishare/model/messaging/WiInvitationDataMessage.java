package com.example.eric.wishare.model.messaging;

import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import org.json.JSONObject;

public abstract class WiInvitationDataMessage extends WiDataMessage {
    private WiInvitation mInvitation;

    public WiInvitationDataMessage(WiInvitation invitation){
        super(MSG_INVITATION);
        mInvitation = invitation;

        deepCopy(invitation.toJSON());
    }

    public WiInvitationDataMessage(WiInvitation invitation, WiContact recipient){
        super(MSG_INVITATION, recipient);
        mInvitation = invitation;

        deepCopy(invitation.toJSON());
    }

    public void setSender(String sender){
        mInvitation.sender = sender;
    }

    public abstract void onResponse(JSONObject response);

    public static WiInvitation createInvitation(WiDataMessage msg){
        return new WiInvitation(msg);
    }
}

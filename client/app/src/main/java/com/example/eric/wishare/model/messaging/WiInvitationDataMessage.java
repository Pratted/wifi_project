package com.example.eric.wishare.model.messaging;

import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import org.json.JSONObject;

public abstract class WiInvitationDataMessage extends WiDataMessage {

    public WiInvitationDataMessage(WiInvitation invitation){
        super(MSG_INVITATION);
    }

    public WiInvitationDataMessage(WiInvitation invitation, WiContact recipient){
        super(MSG_INVITATION, recipient);
    }

    public abstract void onResponse(JSONObject response);

    public static WiInvitation createInvitation(WiDataMessage msg){
        return new WiInvitation(msg);
    }
}

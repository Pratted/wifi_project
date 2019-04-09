package com.example.eric.wishare.model.messaging;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONObject;

public class WiRevokeAccessDataMessage extends WiDataMessage{

    public WiRevokeAccessDataMessage(WiConfiguration config, String recipient){
        super(MSG_CREDENTIALS);
        deepCopy(config.toJSONObject());
        addRecipient(recipient);
    }

    @Override
    public void onResponse(JSONObject response) {

    }
}

package com.example.eric.wishare.model.messaging;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONObject;

public abstract class WiConfigurationDataMessage extends WiDataMessage {
    public WiConfigurationDataMessage(WiConfiguration config, String recipient) {
        super(MSG_CREDENTIALS);
        deepCopy(config.toJSONObject());

        addRecipient(recipient);
    }

    public static WiConfiguration createWiConfiguration(WiDataMessage msg){
        return new WiConfiguration(msg);
    }

    public abstract void onResponse(JSONObject response);
}

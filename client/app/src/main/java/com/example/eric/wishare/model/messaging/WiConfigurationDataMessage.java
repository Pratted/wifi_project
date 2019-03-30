package com.example.eric.wishare.model.messaging;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONObject;

public abstract class WiConfigurationDataMessage extends WiDataMessage {
    public WiConfigurationDataMessage(WiConfiguration config){
        super(MSG_ACKNOWLEDGE);
    }

    public WiConfigurationDataMessage(WiConfiguration config, WiContact recipient) {
        super(MSG_ACKNOWLEDGE);

        addRecipient(recipient);
    }

    public abstract void onResponse(JSONObject response);
}

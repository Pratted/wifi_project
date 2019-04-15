package com.example.eric.wishare.model.messaging;

import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONObject;

public class WiRevokeAccessDataMessage extends WiDataMessage{
    public WiRevokeAccessDataMessage(WiConfiguration config, String recipient){
        super(MSG_REVOKE_ACCESS, recipient);
        deepCopy(config.toJSONObject());
    }

    @Override
    public void onResponse(JSONObject response) {

    }
}

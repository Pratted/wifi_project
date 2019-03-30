package com.example.eric.wishare.model.messaging;

import android.util.Log;

import org.json.JSONObject;

import java.util.Map;

// helper class so we can instantiate an incoming DataMessage in WiMessagingService
public class WiIncomingDataMessage extends WiDataMessage {
    private String TAG = "WiIncomingDataMessage";

    public WiIncomingDataMessage(Map<String, String> data){
        super(Integer.valueOf(data.get("msg_type")));

        try{
            Log.d(TAG, "Begin copying data");
            for(String key: data.keySet()){
                put(key, data.get(key));
            }
            Log.d(TAG, "Finished Copying data");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(JSONObject response) {

    }
}

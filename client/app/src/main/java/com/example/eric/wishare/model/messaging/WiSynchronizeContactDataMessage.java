package com.example.eric.wishare.model.messaging;

import android.util.Log;

import com.example.eric.wishare.model.WiContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class WiSynchronizeContactDataMessage extends WiDataMessage {
    private String TAG = "WiSynchronizeContactDataMessage";

    public WiSynchronizeContactDataMessage(List<WiContact> contacts) {
        super(WiDataMessage.MSG_CONTACT_LIST);

        try {
            JSONArray arr = new JSONArray();
            for(WiContact contact: contacts){
                if(!contact.getPhone().isEmpty()){
                    arr.put(contact.getPhone());
                    Log.d(TAG, "Adding " + contact.getPhone() + " to list...");
                }
            }

            put("phones", arr);
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    public List<String> getIncomingPhoneNumbers(JSONObject response){
        List<String> phones = new ArrayList<>();
        try{
            for(int i = 0; i < response.getJSONArray("phones").length(); i++){
                String phone = response.getJSONArray("phones").get(i).toString();
                phones.add(phone);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return phones;
    }

    public abstract void onResponse(JSONObject response);
}

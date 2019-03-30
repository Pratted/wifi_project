package com.example.eric.wishare.model.messaging;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WiDataMessage extends JSONObject {

    private final String TAG = "WiDataMessage";
    private String mUrl;

    public static final Integer MSG_ACKNOWLEDGE = 0;
    public static final Integer MSG_INVITATION = 1;
    public static final Integer MSG_CREDENTIALS = 2;
    public static final Integer MSG_CONTACT_LIST = 3;

    public static String BASE_URL = "http://192.3.135.177:3000/";

    private int messageType;
    private List<WiContact> mRecipients = new ArrayList<>();

    public WiDataMessage(Integer msg_type){
        messageType = msg_type;
    }

    public WiDataMessage(Integer msg_type, WiContact recipient){
        messageType = msg_type;
        addRecipient(recipient);
    }

    public WiDataMessage(Map<String, String> data){
        messageType = Integer.valueOf(data.get("msg_type"));

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

    public Integer getMessageType(){
        return messageType;
    }

    // replace 'this' with json
    private void deepCopy(JSONObject json){
        Iterator<String> keys = json.keys();

        try{
            while(keys.hasNext()){
                String key = keys.next();
                put(key, json.getString(key));
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void addRecipient(WiContact contact){
        mRecipients.add(contact);
    }

    public JsonObjectRequest build(){
        Log.d(TAG, "Building WiDataMessage...");

        mUrl = BASE_URL + (messageType != MSG_CONTACT_LIST ? "msg" : "");
        mUrl += "?token=" + WiUtils.getDeviceToken();

        try{
            put("msg_type", messageType);
            put("to", new JSONArray());

            for(WiContact recipient: mRecipients){
                getJSONArray("to").put(recipient.getPhone());
            }

        } catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, mUrl, this,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // call the method the user defined. not this one.
                        WiDataMessage.this.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                            if(error != null){
                                error.printStackTrace();
                            }
                    }
                });


        req.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        return req;
    }

    public void onResponse(JSONObject response){

    }
}

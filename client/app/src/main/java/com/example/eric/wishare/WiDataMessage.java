package com.example.eric.wishare;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.eric.wishare.model.WiInvitation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WiDataMessage extends JSONObject {

    private final String TAG = "WiDataMessage";
    private String mUrl;

    public static final Integer MSG_ACKNOWLEDGE = 0;
    public static final Integer MSG_INVITATION = 1;
    public static final Integer MSG_CREDENTIALS = 2;
    public static final Integer MSG_CONTACT_LIST = 3;

    public static final String BASE_URL = "http://192.3.135.177:3000/";
    
    private OnResponseListener mOnResponseListener;
    private int messageType;

    // every data message has a 'to' and 'msg_type' field
    private void init(){
        try {
            put("msg_type", messageType);
            put("to", new JSONArray());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getMessageType(){
        return messageType;
    }

    public WiDataMessage(Integer msg_type){
        messageType = msg_type;
        init();
    }

    public WiDataMessage(Integer msg_type, List<String> recipients){
        messageType = msg_type;

        init();

        for(String recipient: recipients){
            addRecipient(recipient);
        }
    }

    public WiDataMessage(Integer msg_type, String recipient){
        messageType = msg_type;

        init();

        addRecipient(recipient);
    }

    public void addRecipient(String phone){
        try {
            getJSONArray("to").put(phone);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public JsonObjectRequest build(){
        Log.d(TAG, "Building WiDataMessage...");
        mUrl = BASE_URL + (getMessageType() != MSG_CONTACT_LIST ? "msg" : "");

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, mUrl, this,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(mOnResponseListener != null){
                            mOnResponseListener.onResponse(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(mOnResponseListener != null){
                            if(error != null){
                                error.printStackTrace();
                            }
                            mOnResponseListener.onResponse(null);
                        }
                    }
                });


        req.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        return req;
    }

    public void setOnResponseListener(OnResponseListener listener){
        mOnResponseListener = listener;
    }

    public ArrayList<WiInvitation> getWiInvitations() {
        ArrayList<WiInvitation> invitations = new ArrayList<>();
        try {
            JSONArray arr = getJSONArray("networks");

            for(int i = 0; i < arr.length(); i++){
               invitations.add(new WiInvitation((JSONObject) arr.get(i)));
            }

            return invitations;
        } catch (JSONException e){
            e.printStackTrace();
            return invitations;
        }
    }

    public interface OnResponseListener {
        void onResponse(JSONObject response);
    }

    public void put(WiInvitation invitation) {
        try {
            if(!has("networks")) {
                put("networks", new JSONArray());
            }
            getJSONArray("networks").put(invitation.toJSON());

        } catch (JSONException e) {

        }
    }
}

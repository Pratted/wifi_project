package com.example.eric.wishare.model.messaging;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class WiDataMessage extends JSONObject {

    private final String TAG = "WiDataMessage";
    private String mUrl;

    public static final int MSG_ACKNOWLEDGE = 0;
    public static final int MSG_INVITATION = 1;
    public static final int MSG_CREDENTIALS = 2;
    public static final int MSG_CONTACT_LIST = 3;
    public static final int MSG_INVITATION_ACCEPTED = 4;
    public static final int MSG_INVITATION_DECLINED = 5;
    public static final int MSG_REVOKE_ACCESS = 6;
    public static final int MSG_TEST_CONNECTION = 7;

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

    public WiDataMessage(Integer msg_type, String recipient){
        messageType = msg_type;

        WiContact temp = new WiContact("", recipient);
        addRecipient(temp);
    }

    public Integer getMessageType(){
        return messageType;
    }

    public String getSender(){
        String sender = "UNKNOWN";

        try{
            sender = getString("sender");
        } catch (JSONException e){
            e.printStackTrace();
        }

        return sender;
    }

    // replace 'this' with json
    protected void deepCopy(JSONObject json){
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

    public void addRecipient(String phone){
        mRecipients.add(new WiContact("", phone));
    }

    public JsonObjectRequest build(){
        Log.d(TAG, "Building WiDataMessage...");

        mUrl = BASE_URL;

        if (messageType == MSG_CONTACT_LIST || messageType == MSG_TEST_CONNECTION){
            if(messageType == MSG_TEST_CONNECTION)
                mUrl += "hello";
        }
        else{
            mUrl += "msg";
        }

        mUrl += "?token=" + WiUtils.getDeviceToken();

        try{
            put("msg_type", messageType);
            put("to", new JSONArray());
            put("sender", WiUtils.getDevicePhone());

            boolean sendToSelf = WiUtils.sendInvitationsToSelf();
            Log.d(TAG, "SEND INVITATION TO SELF ENABLED? " + sendToSelf);

            // discard any recipients and only send to self...
            if(sendToSelf && messageType == MSG_INVITATION){
                mRecipients.clear();
                mRecipients.add(new WiContact("Self", WiUtils.getDevicePhone()));
            }

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

                        Log.d(TAG, "Sending same requestin...");
                        sendPost(mUrl);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "FUCK");

                            if(error != null){
                                error.printStackTrace();
                                sendPost(mUrl);
                            }
                    }
                });


        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        return req;
    }

    public void sendPost(final String urlAdress) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAdress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    Log.i("JSON", WiDataMessage.this.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(WiDataMessage.this.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public abstract void onResponse(JSONObject response);
}

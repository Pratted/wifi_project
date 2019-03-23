package com.example.eric.wishare;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class WiDataMessage {

    private final String TAG = "WiDataMessage";
    private JSONObject mData;
    private String mUrl;

    public final static Integer MSG_ACKNOWLEDGE = 0;
    public final static Integer MSG_INVITATION = 1;
    public final static Integer MSG_CREDENTIALS = 2;

    public final static Integer PORT = 3000;

    private static String QUERY_STRING = "?token=abc123";
    public static final String BASE_URL = "http://192.3.135.177:3000/";

    private OnResponseListener mOnResponseListener;

    public WiDataMessage() {
        mData = new JSONObject();
        mUrl = BASE_URL;
    }

    public WiDataMessage(JSONObject json){
        mData = json;
        mUrl = BASE_URL;
    }

    public WiDataMessage(Integer msg_type, JSONObject data){
        try {
            mData = data;
            mData.put("msg_type", msg_type);
        } catch (Exception e){

        }
    }

    public static void setToken(String token){
        QUERY_STRING = "?token="+token;
    }

    public void put(String key, Object value){
        try {
            mData.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public JsonObjectRequest build(){
        Log.d(TAG, "Building WiDataMessage...");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, mUrl, mData,
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

    public interface OnResponseListener {
        void onResponse(JSONObject response);
    }
}

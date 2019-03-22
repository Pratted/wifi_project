package com.example.eric.wishare;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class WiDataMessage {

    private final String TAG = "WiDataMessage";
    private JSONObject mJSONObect;
    private String mUrl;


    private static String QUERY_STRING = "?token=abc123";
    public static final String BASE_URL = "http://192.3.135.177:3000/";

    private OnResponseListener mOnResponseListener;

    public WiDataMessage() {
        mJSONObect = new JSONObject();
        mUrl = BASE_URL;
    }

    public WiDataMessage(JSONObject json){
        mJSONObect = json;
        mUrl = BASE_URL;
    }

    public static void setToken(String token){
        QUERY_STRING = "?token="+token;
    }

    public void put(String key, Object value){
        try {
            mJSONObect.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public JsonObjectRequest build(){
        Log.d(TAG, "Building WiDataMessage...");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, mUrl, mJSONObect,
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

        req.setRetryPolicy(new DefaultRetryPolicy(10000,
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

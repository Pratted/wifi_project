package com.example.eric.wishare;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class WiDataMessage {

    private JSONObject mJSONObect;
    private Map<String, Object> mData;

    private static String QUERY_STRING = "?token=abc123";
    private static final String BASE_URL = "http://192.3.135.177:3000/";

    private OnResponseListener mOnResponseListener;

    public WiDataMessage(JSONObject json) {
        mJSONObect = json;
    }

    public static void setToken(String token){
        QUERY_STRING = "?token="+token;
    }

    public JsonArrayRequest send() {
        String url = BASE_URL + QUERY_STRING;

        JSONArray arr = new JSONArray();
        arr.put("717-802-9623");

        Log.d("WiDataMessage", "Sending request...." + url);
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST, url, arr, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response != null){
                    Log.d("WiDataMessage", "Got response: " + response.toString());
                }
                else{
                    Log.d("WiDataMessage", "No resposne");
                }

                //mOnResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("WiDataMessage", "Shit");
                //error.printStackTrace();

                if(error.getMessage() != null) {
                    VolleyLog.e(error.getMessage());
                }
            }
        });

        return req;
    }

    public void setOnResponseListener(OnResponseListener listener){
        mOnResponseListener = listener;
    }

    public interface OnResponseListener {
        void onResponse(JSONObject response);
    }
}

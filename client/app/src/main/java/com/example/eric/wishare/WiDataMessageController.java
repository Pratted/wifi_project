package com.example.eric.wishare;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;

public class WiDataMessageController {
    public static final String TAG = "com.example.eric.wishare.WiDataMessageController";

    private RequestQueue mRequestQueue;
    private static WiDataMessageController sInstance;
    private WeakReference<Context> mContext;

    private WiDataMessageController(Context context){
        mContext = new WeakReference<>(context.getApplicationContext());
        mRequestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext.get().getApplicationContext());
        }
        return mRequestQueue;
    }


    public static synchronized WiDataMessageController getInstance(Context context){
        if(sInstance == null){
            sInstance = new WiDataMessageController(context.getApplicationContext());
        }
        return sInstance;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag){
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        mRequestQueue.add(req);
    }

    public <T> void addToRequestQueue(Request<T> req){
        req.setTag(TAG);
        mRequestQueue.add(req);
    }

    public void cancelPendingRequests(Object tag){
        if(mRequestQueue != null){
            mRequestQueue.cancelAll(tag);
        }
    }

    public void send(WiDataMessage msg){
        Log.d("WiDataMessageController", "Adding to request queue!");
        addToRequestQueue(msg.build());
        Log.d("WiDataMessageController", "Added to request queue!");
    }
}
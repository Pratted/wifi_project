package com.example.eric.wishare.model.messaging;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import org.json.JSONObject;

public abstract class TestConnectionMessage extends WiDataMessage{
    private Context mContext;

    public TestConnectionMessage(Context context){
        super(MSG_TEST_CONNECTION);
        mContext = context;
    }
}

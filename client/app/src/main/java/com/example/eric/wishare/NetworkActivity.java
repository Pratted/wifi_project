package com.example.eric.wishare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class NetworkActivity extends AppCompatActivity {

    private WiConfiguration mConfig;
    private TextView mTvNetworkName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        if(getIntent().hasExtra("NetworkInfo")) {
            System.out.println("IT HAS THE INTENT");
        }

        mConfig = (WiConfiguration) getIntent().getSerializableExtra("NetworkInfo");
        if(mConfig != null) {
            System.out.println("IN NETWORK ACTIVITY: " + mConfig.getSSID());
            ((TextView)findViewById(R.id.tv_network_name)).setText(mConfig.getSSID());
        } else {
            ((TextView)findViewById(R.id.tv_network_name)).setText("WHAT");
        }
    }
}

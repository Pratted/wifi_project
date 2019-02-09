package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;

@SuppressLint("ParcelCreator")
public class WiConfiguration extends WifiConfiguration {

    private String mPassword;
    private String mSSID;

    public WiConfiguration(String mSSID, String mPassword) {
        this.mPassword = mPassword;
        this.mSSID = super.SSID;
    }

    public String getSSID() {
        return mSSID;
    }
}

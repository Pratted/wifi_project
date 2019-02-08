package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;

@SuppressLint("ParcelCreator")
public class WiConfigurations extends WifiConfiguration {

    private String mPassword;
    private String mSSID;

    public WiConfigurations(String mSSID, String mPassword) {
        this.mPassword = mPassword;
        this.mSSID = super.SSID;
    }

    public String getSSID() {
        return mSSID;
    }
}

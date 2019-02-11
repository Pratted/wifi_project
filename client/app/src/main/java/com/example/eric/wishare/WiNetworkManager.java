package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WiNetworkManager {
    private static WiNetworkManager sNetManager;
    private WifiManager mManager;

    public void add(WiConfiguration configuration) {
       // WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public synchronized static WiNetworkManager getInstance() {
        if (sNetManager == null) {
            sNetManager = new WiNetworkManager();
        }
        return sNetManager;
    }
}

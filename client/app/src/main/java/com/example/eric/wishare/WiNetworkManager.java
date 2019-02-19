package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

public class WiNetworkManager {
    private static WiNetworkManager sNetManager;
    private WifiManager mManager;

    public static ArrayList<WifiConfiguration> getConfiguredNetworks(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(MainActivity.WIFI_SERVICE);
        List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();

        ArrayList<WifiConfiguration> networks = new ArrayList<>();

        for(WifiConfiguration config : wifiList) {
            networks.add(config);
        }
        return networks;
    }

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

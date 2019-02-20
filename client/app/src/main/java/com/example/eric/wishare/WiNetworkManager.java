package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

public class WiNetworkManager {
    private static WiNetworkManager sNetManager;
    private WifiManager mManager;
    private static ArrayList<WiConfiguration> mConfiguredNetworks;
    private static ArrayList<WifiConfiguration> mNotConfiguredNetworks;

    private WiNetworkManager() {
        mConfiguredNetworks = new ArrayList<>();
        mNotConfiguredNetworks = new ArrayList<>();
    }

    public static ArrayList<WifiConfiguration> getConfiguredNetworks(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(MainActivity.WIFI_SERVICE);
        List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();
        mNotConfiguredNetworks.addAll(wifiList);
        return mNotConfiguredNetworks;
    }

    public void addConfiguredNetwork(WiConfiguration config) {
       // WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mConfiguredNetworks.add(config);
    }

    public void addNotConfiguredNetwork(WifiConfiguration config) {
        mNotConfiguredNetworks.add(config);
    }

    public void removeConfiguredNetwork(WiConfiguration config) {
        mNotConfiguredNetworks.remove(config);
    }

    public void removeNotConfiguredNetwork(WifiConfiguration config) {
        for(WifiConfiguration configuration : mNotConfiguredNetworks) {
            if(config.SSID.equals(configuration.SSID.replace("\"", ""))) {
                mNotConfiguredNetworks.remove(configuration);
                break;
            }
        }
    }

    public ArrayList<WifiConfiguration> getNotConfiguredNetworks() {
        return mNotConfiguredNetworks;
    }

    public ArrayList<WiConfiguration> getConfiguredNetworks() {
        return mConfiguredNetworks;
    }

    public synchronized static WiNetworkManager getInstance() {
        if (sNetManager == null) {
            sNetManager = new WiNetworkManager();
        }
        return sNetManager;
    }
}

package com.example.eric.wishare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class WiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        String prevNetwork = prefs.getString("prev_network", "");
        String targetNetwork = prefs.getString("target_network", "");
        boolean testConnection = prefs.getBoolean("test_connection", false);

        System.out.println("Prev " + prevNetwork);
        System.out.println("Target " + targetNetwork);

        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) && testConnection){
            System.out.println("Connected changed!!!");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            System.out.println("Connected? " + info.isConnected());

            if(info.isConnected()){
                WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                String ssid = manager.getConnectionInfo().getSSID();

                System.out.println("Connected!!!");

                if(ssid.replace("\"","").equals(targetNetwork.replace("\"", ""))){
                    System.out.println("The connection was successful!");

                    //update shared prefs to avoid testing the connection more than once
                    editor.putString("prev_network", "");
                    editor.putString("target_network", "");
                    editor.putBoolean("test_connection", false);
                    editor.commit();

                    // if the user was previously connect to 'A' and just tested connection 'B', disconnect them from 'B'
                    // and connect them back to 'A'.
                    if(!prevNetwork.replace("\"", "").equals(targetNetwork.replace("\"", ""))){
                        manager.disconnect(); // succeeded, close this connection

                        // connect back to original network..
                        manager.enableNetwork(getPrevNetworkId(manager, prevNetwork), true);
                    }
                }
            }
        }
    }

    private int getPrevNetworkId(WifiManager manager, String ssid){
        for(WifiConfiguration config: manager.getConfiguredNetworks()) {
            if (config.SSID.replace("\"", "").equals(ssid.replace("\"", ""))) {
                return config.networkId;
            }
        }
        return -1;
    }
}

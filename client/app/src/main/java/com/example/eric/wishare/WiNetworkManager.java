package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.eric.wishare.model.WiConfiguration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

/****
 Some terminology...

 A 'configured' network is a network with a known password. All instances of WiConfiguration are
 configured networks because you need a password to instantiate a WiConfiguration.

 An 'unconfigured' network is any network that doesn't have a known password.
***/
public class WiNetworkManager {
    private static WiNetworkManager sInstance;
    private static WifiManager sWifiManager;

    private WeakReference<Context> mContext;
    private String TAG = "WiNetworkManager";

    private Map<String, WiConfiguration> mConfiguredNetworks;
    private Map<String, WifiConfiguration> mUnConfiguredNetworks;


    private WiNetworkManager(Context context) {
        mContext = new WeakReference<>(context.getApplicationContext());
        sWifiManager = (WifiManager) mContext.get().getApplicationContext().getSystemService(WIFI_SERVICE);
        WiSharedPreferences.initialize(context);

        synchronizeNetworks();
    }

    private void synchronizeNetworks(){
        List<WiConfiguration> dbNetworks = WiSQLiteDatabase.getInstance(mContext.get()).loadNetworks();
        List<WifiConfiguration> deviceNetworks = sWifiManager.getConfiguredNetworks();

        mConfiguredNetworks = new HashMap<>();
        mUnConfiguredNetworks = new HashMap<>();

        for(WiConfiguration config: dbNetworks){
            Log.d(TAG, "Adding " + config.SSID + " to configured networks...");
            mConfiguredNetworks.put(config.SSID, config);
        }

        for(WifiConfiguration config: deviceNetworks){
            // if not already in configured, add to unConfiguredNetworks...
            if(!mConfiguredNetworks.containsKey(config.SSID)){
                mUnConfiguredNetworks.put(config.SSID, config);
                Log.d(TAG, config.SSID + " is an UNconfigured network...");
            }
            else{
                Log.d(TAG, config.SSID + " is a configured network...");
            }
        }
    }

    // called by a client when a host sends them network credentials
    public void addConfiguredNetwork(WiConfiguration config) {
        Log.d(TAG, "WifiManagerEnabled? " + WiUtils.isWifiManagerEnabled());

        if(WiUtils.isWifiManagerEnabled()){
            try{
                // the call to configure() here ensures all the parameters are formatted correctly
                // (i.e. double quotes)
                int retVal = sWifiManager.addNetwork(config.configure()); // <- actually saves the network into android wifi
                System.out.println("STRING LITERALLLLLLL" + retVal); // retVal might be an error code
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // called by client when revoke access data message arrives
    public void removeConfiguredNetwork(WiConfiguration config){
        try{
            int id = getNetworkId(config);

            Log.d(TAG, "Android network ID is: " + id);
            boolean retVal = sWifiManager.removeNetwork(id);
            Log.d(TAG, "Remove success boolean: " + retVal);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // invoked by the host when they enter a network password.
    public void configureNetwork(WiConfiguration config){
        mConfiguredNetworks.put(config.SSID, config);
        mUnConfiguredNetworks.remove(config.SSID);
    }

    // called by host when they want to remove a network from configured network list view.
    public void unConfigureNetwork(WiConfiguration config){
        mConfiguredNetworks.remove(config.SSID);
        mUnConfiguredNetworks.put(config.SSID, config);
    }

    private int getNetworkId(WiConfiguration config){
        List<WifiConfiguration> networks = sWifiManager.getConfiguredNetworks();
        int nID = -1;
        for (WifiConfiguration wc: networks){
            if(wc.SSID.replace("\"", "").equals(config.SSID.replace("\"", ""))){
                return wc.networkId;
            }
        }

        return -1;
    }

    // returning ArrayList here so .values() gets changed from collection to list
    public List<WifiConfiguration> getUnConfiguredNetworks() {
        return new ArrayList<>(mUnConfiguredNetworks.values());
    }

    public WiConfiguration getConfiguredNetwork(String ssid){
        return mConfiguredNetworks.get(ssid);
    }

    public List<WiConfiguration> getConfiguredNetworks() {
        for(WiConfiguration configuration: mConfiguredNetworks.values()){
            if(configuration != null){
                Log.d(TAG, "Configured Network {" + configuration.SSID + "}");
            }
            else{
                Log.d(TAG, "Null configuration found!");
            }
        }

        return new ArrayList<>(mConfiguredNetworks.values());
    }

    public synchronized static WiNetworkManager getInstance(Context context) {
        if(sInstance == null){
            sInstance = new WiNetworkManager(context.getApplicationContext());
        }
        return sInstance;
    }
}

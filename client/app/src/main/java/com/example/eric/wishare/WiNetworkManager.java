package com.example.eric.wishare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
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

    private SharedPreferences.Editor mEditor;
    private WeakReference<Context> mContext;
    private String TAG = "WiNetworkManager";

    private Map<String, WiConfiguration> mConfiguredNetworks;
    private Map<String, WifiConfiguration> mUnConfiguredNetworks;


    private WiNetworkManager(Context context) {
        mContext = new WeakReference<>(context.getApplicationContext());
        sWifiManager = (WifiManager) mContext.get().getApplicationContext().getSystemService(WIFI_SERVICE);
        WiSharedPreferences.initialize(context);

        mEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();

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
            // android wifi manager surrounds wifi SSID with quotes, remove them for comparision
            String ssid = config.SSID.replace("\"", "");

            // if not already in configured, add to unConfiguredNetworks...
            if(!mConfiguredNetworks.containsKey(ssid)){
                mUnConfiguredNetworks.put(ssid, config);
                Log.d(TAG, ssid + " is an UNconfigured network...");
            }
            else{
                Log.d(TAG, ssid + " is a configured network...");
            }
        }
    }

    public void refresh(){
        synchronizeNetworks();
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



    public boolean isSsidInRange(String ssid){
        List<ScanResult> results = sWifiManager.getScanResults();

        for(ScanResult res: results){
            if(res.SSID.replace("\"", "").equals(ssid.replace("\"", ""))){
                return true;
            }
        }
        return false;
    }

    public void testConnection(String ssid){
        Log.d(TAG, "Preparing for test connection");

        WiConfiguration targetNetwork = getConfiguredNetwork(ssid);
        if(targetNetwork == null){
            Log.d(TAG, "Could not find WiConfiguration");
            return;
        }

        Log.d(TAG, "Adding network to devices Wifi...");
        int networkId = sWifiManager.addNetwork(targetNetwork.configure());
        Log.d(TAG, "Target Network Id: " + networkId);

        if(networkId == -1){
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.get().registerReceiver(getWifiConnectedReceiver(), intentFilter);

        ConnectivityManager cm = (ConnectivityManager) mContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo tmp = cm.getActiveNetworkInfo();

        if(tmp == null){
            Log.d(TAG, "No current network...");
            return;
        }

        if(tmp.isConnected()){
            WifiInfo info = sWifiManager.getConnectionInfo();
            Log.d(TAG, "Currently connected to "+ info.getSSID()+ ". Saving current connection...");

            WiSharedPreferences.putString("prev_network", info.getSSID());
            WiSharedPreferences.putString("target_network", ssid);
            WiSharedPreferences.putBoolean("test_connection", true);
            WiSharedPreferences.save(); // save now, blocking


            Log.d(TAG, "Disabling " + info.getSSID());
            Log.d(TAG, "Success? " + sWifiManager.disableNetwork(info.getNetworkId()));
            sWifiManager.disconnect();
        }

        Log.d(TAG, "Target Network: " + ssid);
        Log.d(TAG, "Attempting to connect to " + targetNetwork.getSSID());
        sWifiManager.enableNetwork(networkId, true);
        sWifiManager.reconnect();
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

    public interface OnTestConnectionCompleteListener{
        void onTestConnectionComplete(boolean success);
    }

    private OnTestConnectionCompleteListener mOnTestConnectionCompleteListener;

    public void setOnTestConnectionCompleteListener(OnTestConnectionCompleteListener listener){
        mOnTestConnectionCompleteListener = listener;
    }

    private BroadcastReceiver getWifiConnectedReceiver(){
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String TAG = "WiNetworkManager.Rec";
                String action = intent.getAction();
                String prevNetwork = WiSharedPreferences.getString("prev_network", "");
                String targetNetwork = WiSharedPreferences.getString("target_network", "");
                boolean testConnection = WiSharedPreferences.getBoolean("test_connection", false);

                if(action == null){
                    Log.d(TAG, "No action found. Exiting");
                    return;
                }

                Log.d(TAG, "Previous Network: " + prevNetwork);
                Log.d(TAG, "Target Network: " + targetNetwork);

                // only pay attention to network state if we're trying to test a connection
                if(testConnection && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                    Log.d(TAG, "Connection state changed");

                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    Log.d(TAG, "Currently connected to a network? " + info.isConnected());

                    if(info.isConnected()){
                        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo curr = manager.getConnectionInfo();


                        if(curr == null){
                            Log.d(TAG, "Could not get the current connection SSID. Unable to continue");

                            return;
                        }

                        String ssid = curr.getSSID();

                        if(ssid.replace("\"","").equals(targetNetwork.replace("\"", ""))){
                            Log.d(TAG, "Connected to target network");

                            //update shared prefs to avoid testing the connection more than once
                            WiSharedPreferences.putString("prev_network", "");
                            WiSharedPreferences.putString("target_network", "");
                            WiSharedPreferences.putBoolean("test_connection", false);
                            WiSharedPreferences.save();

                            // unregister this receiver since it is no longer needed
                            context.unregisterReceiver(this);

                            /*
                            // if the user was previously connect to 'A' and just tested connection 'B', disconnect them from 'B'
                            // and connect them back to 'A'.
                            if(!prevNetwork.replace("\"", "").equals(targetNetwork.replace("\"", ""))){
                                manager.disconnect(); // succeeded, close this connection

                                // connect back to original network..
                                manager.enableNetwork(getPrevNetworkId(manager, prevNetwork), true);
                            }
                            */

                            mOnTestConnectionCompleteListener.onTestConnectionComplete(true);
                        }
                        else{
                            Log.d(TAG, "Connected to old network, disconnecting...");
                            manager.disconnect();
                            //manager.enableNetwork(getPrevNetworkId(manager, ssid), true);
                            //manager.reconnect();
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
        };
    }
}

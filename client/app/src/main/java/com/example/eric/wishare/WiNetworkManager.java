package com.example.eric.wishare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.eric.wishare.model.WiConfiguration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

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

        mEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        synchronizeNetworks();
    }

    private void synchronizeNetworks(){
        List<WiConfiguration> dbNetworks = WiSQLiteDatabase.getInstance(mContext.get()).loadNetworks();
        List<WifiConfiguration> deviceNetworks = sWifiManager.getConfiguredNetworks();

        mConfiguredNetworks = new HashMap<>();
        mUnConfiguredNetworks = new HashMap<>();

        for(WiConfiguration config: dbNetworks){
            mConfiguredNetworks.put(config.SSID, config);
        }

        for(WifiConfiguration config: deviceNetworks){
            // if not already in configured, add to unConfiguredNetworks...
            if(!mConfiguredNetworks.containsKey(config.SSID)){
                mUnConfiguredNetworks.put(config.SSID, config);
            }
        }
    }

    public void refresh(){
        synchronizeNetworks();
    }

    // called by a client when a host sends them network credentials
    public void addConfiguredNetwork(WiConfiguration config) {
        try{
            sWifiManager.addNetwork(config); // <- actually saves the network into android wifi
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // invoked by the host when they enter a network password
    public void configureNetwork(WiConfiguration config){
        mConfiguredNetworks.put(config.SSID, config);
        mUnConfiguredNetworks.remove(config.SSID);
    }

    public void removeConfiguredNetwork(WiConfiguration config) {
        mConfiguredNetworks.remove(config);
        mUnConfiguredNetworks.put(config.SSID, config);
    }

    public boolean testConnection(String ssid){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.get().registerReceiver(getWifiConnectedReceiver(), intentFilter);

        ConnectivityManager cm = (ConnectivityManager) mContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo tmp = cm.getActiveNetworkInfo();

        if(tmp == null){
            System.out.println("No current network...");
            return false;
        }

        if(tmp.isConnected()){
            WifiInfo info = sWifiManager.getConnectionInfo();
            System.out.println("adding shit to prefs...");

            mEditor.putString("prev_network", info.getSSID());
            mEditor.putString("target_network", ssid);
            mEditor.putBoolean("test_connection", true);
            mEditor.commit(); // save now, blocking
        }

        System.out.println("Target: " + ssid);

        for(WifiConfiguration config : sWifiManager.getConfiguredNetworks()){
            System.out.println("Current: " + config.SSID.replace("\"", ""));

            if(config.SSID.replace("\"", "").equals(ssid)){
                System.out.println("Attempting to connect to " + ssid);
                return testConnection(config);
            }
        }

        return false;
    }

    public boolean testConnection(WifiConfiguration config){
        return sWifiManager.enableNetwork(config.networkId, true);
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

                            context.unregisterReceiver(this);
                            // if the user was previously connect to 'A' and just tested connection 'B', disconnect them from 'B'
                            // and connect them back to 'A'.
                            if(!prevNetwork.replace("\"", "").equals(targetNetwork.replace("\"", ""))){
                                manager.disconnect(); // succeeded, close this connection

                                // connect back to original network..
                                manager.enableNetwork(getPrevNetworkId(manager, prevNetwork), true);
                            }

                            mOnTestConnectionCompleteListener.onTestConnectionComplete(true);
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

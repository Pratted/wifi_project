package com.example.eric.wishare.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("ParcelCreator")
public class WiConfiguration extends WifiConfiguration implements Parcelable {
    // the network id for the database, NOT android
    private String mNetworkID = "";

    public WiConfiguration(String ssid, String password, String network_id) {
        SSID = ssid;
        preSharedKey = password;
        mNetworkID = network_id;
    }

    public WiConfiguration(String ssid, String password) {
        SSID = ssid;
        preSharedKey = password;
    }

    public WiConfiguration(WifiConfiguration config, String password) {
        SSID = config.SSID;
        preSharedKey = password;
    }

    public WiConfiguration(WifiConfiguration config, String password, String network_id) {
        SSID = config.SSID;
        preSharedKey = password;
        mNetworkID = network_id;
    }

    public WiConfiguration(Parcel source) {
        SSID = source.readString();
        preSharedKey = source.readString();
    }

    public static final Creator<WiConfiguration> CREATOR = new Creator<WiConfiguration>() {
        @Override
        public WiConfiguration createFromParcel(Parcel source) {
            return new WiConfiguration(source);
        }

        @Override
        public WiConfiguration[] newArray(int size) {
            return new WiConfiguration[size];
        }
    };

    public String getSSIDNoQuotes() {
        return SSID.replace("\"", "");
    }

    public String getPassword() {
        return preSharedKey;
    }

    public void setPassword(String password) {
        preSharedKey = password;
    }

    public void setSSID(String ssid) {
        SSID = ssid;
    }

    public String getNetworkID(){return mNetworkID;}

    public void setNetworkId(String networkId){
        mNetworkID = networkId;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(preSharedKey);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("ssid", SSID);
            json.put("pwd", preSharedKey);
        } catch (JSONException e) {

        }
        return json;
    }

    public WiConfiguration(JSONObject json){
        try{
            SSID = json.getString("ssid");
            preSharedKey = json.getString("pwd");
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public WifiConfiguration configure(){
        WifiConfiguration wiConfiguration = new WifiConfiguration();
        wiConfiguration.SSID = "\"".concat(SSID.replace("\"", "")).concat("\"");
        wiConfiguration.preSharedKey = "\"".concat(preSharedKey.replace("\"", "")).concat("\"");
        return wiConfiguration;
    }

    public ContentValues toContentValues(){
        ContentValues vals = new ContentValues();
        vals.put("SSID", SSID);
        vals.put("password", preSharedKey);
        return vals;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof WiConfiguration) {
            WiConfiguration that = (WiConfiguration) o;
            return this.SSID.equals(that.SSID) && this.preSharedKey.equals(that.preSharedKey);
        }
        return false;
    }
}

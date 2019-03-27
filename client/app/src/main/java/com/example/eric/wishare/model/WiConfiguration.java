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

    private String mPassword;
    private String mNetworkID;

    public WiConfiguration(String ssid, String password, String network_id) {
        SSID = ssid;
        mPassword = password;
        mNetworkID = network_id;
    }

    public WiConfiguration(String ssid, String password) {
        SSID = ssid;
        mPassword = password;
    }

    public WiConfiguration(WifiConfiguration config, String password) {
        SSID = config.SSID;
        mPassword = password;
    }

    public WiConfiguration(WifiConfiguration config, String password, String network_id) {
        SSID = config.SSID;
        mPassword = password;
        mNetworkID = network_id;
    }

    public WiConfiguration(Parcel source) {
        SSID = source.readString();
        mPassword = source.readString();
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

    public String getSSID() {
        return SSID.replace("\"", "");
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setSSID(String ssid) {
        SSID = ssid;
    }

    public String getNetworkID(){return mNetworkID;}

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(mPassword);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("ssid", SSID);
            json.put("pwd", mPassword);
        } catch (JSONException e) {

        }
        return json;
    }

    public ContentValues toContentValues(){
        ContentValues vals = new ContentValues();
        vals.put("SSID", SSID);
        vals.put("password", mPassword);
        return vals;
    }
}

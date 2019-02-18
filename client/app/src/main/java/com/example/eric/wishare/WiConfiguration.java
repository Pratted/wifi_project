package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.Serializable;

@SuppressLint("ParcelCreator")
public class WiConfiguration extends WifiConfiguration implements Serializable {

    private String mSSID;
    private String mPassword;

    public WiConfiguration(String mSSID, String mPassword) {
        this.mSSID = mSSID;
        this.mPassword = mPassword;
    }
//
//    public WiConfiguration(WiConfiguration config) {
//        this.mSSID = config.getSSID();
//        this.mPassword = config.getPassword();
//    }

//    public WiConfiguration(Parcel source) {
//        System.out.println("SOURCE.READSTRING(SSID = ): " + source.readString());
//        this.mSSID = source.readString();
//        this.mPassword = source.readString();
//    }
//
//    public static final Creator<WiConfiguration> CREATOR = new Creator<WiConfiguration>() {
//        @Override
//        public WiConfiguration createFromParcel(Parcel source) {
//            return new WiConfiguration(source);
//        }
//
//        @Override
//        public WiConfiguration[] newArray(int size) {
//            return new WiConfiguration[size];
//        }
//    };

    public String getSSID() {
        return mSSID.replace("\"", "");
    }

    public String getPassword() {
        return mPassword;
    }


//    @Override
//    public int describeContents() {
//        return super.describeContents();
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        super.writeToParcel(dest, flags);
//        dest.writeString(mSSID);
//        dest.writeString(mPassword);
//    }
}

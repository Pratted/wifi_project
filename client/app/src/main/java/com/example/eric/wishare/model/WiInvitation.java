package com.example.eric.wishare.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class WiInvitation implements Parcelable {
    public String networkName;
    public String sender;
    public String expires;
    public String timeLimit;
    public String dataLimit;
    private WiConfiguration mConfiguration;

    public WiConfiguration getWiConfiguration() {
        return mConfiguration;
    }

    public WiInvitation(String networkName, String sender, String expires, String timeLimit, String dataLimit) {
        this.networkName = networkName;
        this.sender = sender;
        this.expires = expires;
        this.timeLimit = timeLimit;
        this.dataLimit = dataLimit;
    }

    public WiInvitation(String networkName, String sender, String expires, String dataLimit) {
        this.networkName = networkName;
        this.sender = sender;
        this.expires = expires;
        this.timeLimit = "";
        this.dataLimit = dataLimit;
    }

    public ContentValues toContentValues(){
        ContentValues vals = new ContentValues();
        vals.put("ssid", networkName);
        vals.put("sender", sender);
        vals.put("expires", expires);
        vals.put("data_limit", 10);
        return vals;
    }

    private WiInvitation(){}

    public static WiInvitation fromCursor(Cursor cur){
        WiInvitation invitation = new WiInvitation();

        invitation.networkName = cur.getString(cur.getColumnIndex("ssid"));
        invitation.sender = cur.getString(cur.getColumnIndex("sender"));
        invitation.expires = cur.getString(cur.getColumnIndex("expires"));
        invitation.dataLimit = String.valueOf(cur.getInt(cur.getColumnIndex("data_limit")));

        return invitation;
    }

    public WiInvitation(JSONObject json) {
        try {
            this.networkName = json.getString("network_name");
            this.sender = json.getString("sender");
            //String phone = json.getString("owner");
            // Look up owner by phone number
            this.expires = json.getString("expires");
            this.dataLimit = json.getString("data_limit");
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
    }

    public String getSender(){
        return sender;
    }
    public String getNetworkName(){
        return networkName;
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof WiInvitation)) return false;

        WiInvitation rhs = (WiInvitation) obj;

        return networkName.equals(rhs.networkName) && sender.equals(rhs.sender) && expires.equals(rhs.expires);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("network_name", networkName);
            json.put("sender", sender);
            json.put("expires", expires);
            json.put("data_limit", dataLimit);

        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(networkName);
        dest.writeString(sender);
        dest.writeString(expires);
        dest.writeString(timeLimit);
        dest.writeString(dataLimit);
        dest.writeParcelable(mConfiguration, flags);
    }

    protected WiInvitation(Parcel in) {
        networkName = in.readString();
        sender = in.readString();
        expires = in.readString();
        timeLimit = in.readString();
        dataLimit = in.readString();
        mConfiguration = in.readParcelable(WiConfiguration.class.getClassLoader());
    }

    public static final Creator<WiInvitation> CREATOR = new Creator<WiInvitation>() {
        @Override
        public WiInvitation createFromParcel(Parcel in) {
            return new WiInvitation(in);
        }

        @Override
        public WiInvitation[] newArray(int size) {
            return new WiInvitation[size];
        }
    };
}

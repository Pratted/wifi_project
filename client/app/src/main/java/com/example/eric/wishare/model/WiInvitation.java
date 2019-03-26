package com.example.eric.wishare.model;

import org.json.JSONException;
import org.json.JSONObject;

public class WiInvitation {
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

    public WiInvitation(JSONObject json) {
        try {
            this.networkName = json.getString("network_name");
            //String phone = json.getString("owner");
            // Look up owner by phone number
            this.expires = json.getString("expires");
            this.dataLimit = json.getString("data_limit");
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
    }

    public String getOwner(){
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
}

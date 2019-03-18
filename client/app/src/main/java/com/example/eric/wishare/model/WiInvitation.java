package com.example.eric.wishare.model;

public class WiInvitation {
    public String networkName;
    public WiContact owner;
    public String expires;
    public String timeLimit;
    public String dataLimit;
    private WiConfiguration mConfiguration;

    public WiConfiguration getWiConfiguration() {
        return mConfiguration;
    }

    public WiInvitation(String networkName, WiContact owner, String expires, String timeLimit, String dataLimit) {
        this.networkName = networkName;
        this.owner = owner;
        this.expires = expires;
        this.timeLimit = timeLimit;
        this.dataLimit = dataLimit;
    }

    public WiInvitation() {}

    public WiContact getOwner(){
        return owner;
    }
    public String getNetworkName(){
        return networkName;
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof WiInvitation)) return false;

        WiInvitation rhs = (WiInvitation) obj;

        return networkName.equals(rhs.networkName) && owner.equals(rhs.owner) && expires.equals(rhs.expires);
    }
}

package com.example.eric.wishare;

public class WiInvitation {
    public String networkName;
    public String owner;
    public String expires;
    public String timeLimit;
    public String dataLimit;
    private WiConfiguration mConfiguration;

    public WiConfiguration getWiConfiguration() {
        return mConfiguration;
    }

    public WiInvitation(String networkName, String owner, String expires, String timeLimit, String dataLimit) {
        this.networkName = networkName;
        this.owner = owner;
        this.expires = expires;
        this.timeLimit = timeLimit;
        this.dataLimit = dataLimit;
    }

    public WiInvitation() {}

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof WiInvitation)) return false;

        WiInvitation rhs = (WiInvitation) obj;

        return networkName.equals(rhs.networkName) && owner.equals(rhs.owner) && expires.equals(rhs.expires);
    }
}

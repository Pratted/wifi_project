package com.example.eric.wishare;

public class WiInvitation {
    public String networkName;
    public String owner;
    public String expires;

    public WiInvitation(String networkName, String owner, String expires) {
        this.networkName = networkName;
        this.owner = owner;
        this.expires = expires;
    }

    public WiInvitation(){

    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof WiInvitation)) return false;

        WiInvitation rhs = (WiInvitation) obj;

        return networkName.equals(rhs.networkName) && owner.equals(rhs.owner) && expires.equals(rhs.expires);
    }
}

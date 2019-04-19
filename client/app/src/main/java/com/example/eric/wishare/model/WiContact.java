package com.example.eric.wishare.model;

import android.content.ContentValues;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import com.example.eric.wishare.WiNetworkManager;
import com.example.eric.wishare.WiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiContact implements Parcelable{
    private String name;
    private String phone;
    private Integer mDataUsage;
    private Integer mExpiresIn;

    // invitations sent to the client that they havent yet acknowledged
    private Map<String, WiInvitation> mPendingInvitations;

    // all the networks this client has access too
    private Map<String, WiConfiguration> mPermittedNetworks;

    public WiContact(String name, String phone){
        this.name = name;
        this.phone = phone;
        init();
    }

    public WiContact(Parcel in) {
        name = in.readString();
        phone = in.readString();
        mDataUsage = in.readInt();
        mExpiresIn = in.readInt();

        init();
    }

    private void init() {
        mPermittedNetworks = new HashMap<>();
        mPendingInvitations = new HashMap<>();

        Random r = new Random();
        mDataUsage = r.nextInt(100);
        mExpiresIn = r.nextInt(2000);

        if(mDataUsage % 2 == 1) mDataUsage = -1;
        if(mExpiresIn % 2 == 1) mExpiresIn = -1;
    }

    public boolean hasPendingInvitation(String ssid){
        return mPendingInvitations.containsKey(ssid);
    }

    public boolean hasAccessTo(String ssid){
        return mPermittedNetworks.containsKey(ssid);
    }

    public List<WiConfiguration> getPermittedNetworks(){
        return new ArrayList<>(mPermittedNetworks.values());
    }

    public List<WiInvitation> getPendingInvitations(){
        return new ArrayList<>(mPendingInvitations.values());
    }

    public static final Creator<WiContact> CREATOR = new Creator<WiContact>() {
        @Override
        public WiContact createFromParcel(Parcel in) {
            return new WiContact(in);
        }

        @Override
        public WiContact[] newArray(int size) {
            return new WiContact[size];
        }
    };

    public void invite(WiInvitation invitation){
        mPendingInvitations.put(invitation.getNetworkName(), invitation);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return WiUtils.formatPhoneNumber(phone);
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getDataUsage() {
        return this.mDataUsage;
    }

    public void setDataUsage(Integer mDataUsage) {
        this.mDataUsage = mDataUsage;
    }

    public Integer getExpiresIn() {
        return this.mExpiresIn;
    }

    public void setExpiresIn(Integer mExpiresIn) {
        this.mExpiresIn = mExpiresIn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeInt(mDataUsage);
        dest.writeInt(mExpiresIn);
    }

    @Override
    public String toString() {
        return name;
    }

    public ContentValues toContentValues(){
        ContentValues vals = new ContentValues();
        vals.put("name", name);
        vals.put("phone", phone);
        return vals;
    }

    public void grantAccess(WiConfiguration config) {
        if(hasPendingInvitation(config.SSID))
            mPendingInvitations.remove(config.SSID);
        mPermittedNetworks.put(config.getSSID(), config);

    }

    public void revokeAccess(String SSID) {
        mPermittedNetworks.remove(SSID);
    }

    public void removePendingInvitation(String ssid) {
        mPendingInvitations.remove(ssid);
    }
}

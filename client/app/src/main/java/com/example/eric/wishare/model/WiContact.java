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
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiContact implements Parcelable{
    private String name;
    private String phone;
    private Integer mDataUsage;
    private Integer mExpiresIn;
    private List<WiConfiguration> mInvitedNetworks;

    public WiContact(String name, String phone){
        this.name = name;
        this.phone = phone;
        init();
    }

    protected WiContact(Parcel in) {
        name = in.readString();
        phone = in.readString();
        mDataUsage = in.readInt();
        mExpiresIn = in.readInt();

        init();
    }

    private void init() {
        this.mInvitedNetworks = new ArrayList<>();

        Random r = new Random();
        mDataUsage = r.nextInt(100);
        mExpiresIn = r.nextInt(2000);

        if(mDataUsage % 2 == 1) mDataUsage = -1;
        if(mExpiresIn % 2 == 1) mExpiresIn = -1;
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

    public void addToInvitedNetworks(WiConfiguration config) {
        if(mInvitedNetworks != null)
            this.mInvitedNetworks.add(config);
        else System.out.println("HOW IS THIS NULL??????");
    }

    public void updateInvitedNetworks(Context c) {
        for(WiConfiguration config : WiNetworkManager.getInstance(c).getConfiguredNetworks()) {
            if(!this.mInvitedNetworks.contains(config)) {
                this.mInvitedNetworks.add(config);
            }
        }
    }

    public List<WiConfiguration> getInvitedNetworks() {
        return this.mInvitedNetworks;
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
}

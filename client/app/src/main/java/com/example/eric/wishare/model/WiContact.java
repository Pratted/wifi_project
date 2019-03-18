package com.example.eric.wishare.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

public class WiContact implements Parcelable{
    private String name;
    private String phone;
    private Integer mDataUsage;
    private Integer mExpiresIn;

    public WiContact(String name, String phone){
        this.name = name;
        this.phone = phone;

        Random r = new Random();
        mDataUsage = r.nextInt(100);
        mExpiresIn = r.nextInt(2000);

        if(mDataUsage % 2 == 1) mDataUsage = -1;
        if(mExpiresIn % 2 == 1) mExpiresIn = -1;
    }

    protected WiContact(Parcel in) {
        name = in.readString();
        phone = in.readString();
        mDataUsage = in.readInt();
        mExpiresIn = in.readInt();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getDataUsage() {
        return mDataUsage;
    }

    public void setDataUsage(Integer mDataUsage) {
        this.mDataUsage = mDataUsage;
    }

    public Integer getExpiresIn() {
        return mExpiresIn;
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
}

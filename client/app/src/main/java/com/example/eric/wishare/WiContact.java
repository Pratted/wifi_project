package com.example.eric.wishare;

import android.os.Parcel;
import android.os.Parcelable;

public class WiContact implements Parcelable{
    public String name;
    public String phone;

    public WiContact(String name, String phone){
        this.name = name;
        this.phone = phone;
    }

    protected WiContact(Parcel in) {
        name = in.readString();
        phone = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
    }

    @Override
    public String toString() {
        return name;
    }
}

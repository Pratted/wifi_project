package com.example.eric.wishare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class WiPermissions {
    private Context mContext;
    private static WiPermissions sInstance;

    public static final String CONTACT = Manifest.permission.READ_CONTACTS;
    public static final String PHONE = Manifest.permission.READ_PHONE_STATE;
    public static final String LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private WiPermissions(Context context){
        mContext = context;
    }

    public static WiPermissions getInstance(Context context){
        if(sInstance == null){
            sInstance = new WiPermissions(context.getApplicationContext());
        }
        return sInstance;
    }

    public boolean hasPermission(String permission){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public boolean hasAllPermissions(){
        return hasPermission(CONTACT) && hasPermission(PHONE) && hasPermission(LOCATION);
    }
}

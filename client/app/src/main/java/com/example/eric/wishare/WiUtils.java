package com.example.eric.wishare;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiUtils {

    public static String getDevicePhone(){
        return WiSharedPreferences.getString("phone", "");
    }

    public static String getDeviceToken() {
        return WiSharedPreferences.getString("token", "");
    }

    public static boolean isDeviceRegistered(){
        return WiSharedPreferences.getBoolean("registered", false);
    }

    public static boolean isFreshInstall(Context context){
        return WiSharedPreferences.getBoolean("fresh_install", false);
    }


    public static String formatPhoneNumber(String phone){
        String revised = "";

        /***********************************************************************************
         Source - https://stackoverflow.com/a/16702965
         ************************************************************************************/
        Pattern regex = Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
        Matcher matcher = regex.matcher(phone);

        if(matcher.matches()){
            revised = matcher.group(2) + "-" + matcher.group(3) + "-" + matcher.group(4);
        }
        return revised;
    }
}

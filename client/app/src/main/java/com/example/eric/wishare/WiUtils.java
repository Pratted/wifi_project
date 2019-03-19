package com.example.eric.wishare;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiUtils {

    public static String getDevicePhone(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("phone", "");
    }

    public static String getDeviceToken(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("token", "");
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

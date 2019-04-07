package com.example.eric.wishare;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.eric.wishare.WiUtils.CurrentActivity.ACTIVITY_MAIN;

public class WiUtils {

    public enum CurrentActivity {ACTIVITY_MAIN, ACTIVITY_NETWORK, ACTIVITY_CONTACT}

    public static String ACTIVITY_MAIN = "ACTIVITY_MAIN";
    public static String ACTIVITY_NETWORK = "ACTIVITY_NETWORK";
    public static String ACTIVITY_CONTACT = "ACTIVITY_CONTACT";

    public static String getDevicePhone(){
        return WiSharedPreferences.getString("phone", "");
    }

    public static String getDeviceToken() {
        return WiSharedPreferences.getString("token", "");
    }

    public static CurrentActivity getCurrentActivity() {
        return CurrentActivity.valueOf(WiSharedPreferences.getString("current_activity", ACTIVITY_MAIN));
    }

    public static boolean isDeviceRegistered(){
        return WiSharedPreferences.getBoolean("registered", false);
    }

    public static boolean isFreshInstall(Context context){
        return WiSharedPreferences.getBoolean("fresh_install", false);
    }

    public static void setCurrentActivity(CurrentActivity activity){
        WiSharedPreferences.putString("current_activity", activity.toString());
    }

    public static String getDateTime(){
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        );
        Date date = new Date();
        return formatter.format(date);
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

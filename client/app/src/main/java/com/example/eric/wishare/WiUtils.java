package com.example.eric.wishare;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiUtils {
    private static String currentActivity = "ACTIVITY_MAIN";

    public static boolean sendInvitationsToSelf() {
        return WiSharedPreferences.getBoolean(WiSharedPreferences.KEY_SEND_INVITATIONS_TO_SELF, false);
    }

    public final static String ACTIVITY_MAIN = "ACTIVITY_MAIN";
    public final static String ACTIVITY_NETWORK = "ACTIVITY_NETWORK";
    public final static String ACTIVITY_CONTACT = "ACTIVITY_CONTACT";
    public final static String ACTIVITY_SETTINGS = "ACTIVITY_SETTINGS";

    public static String getDevicePhone(){
        return WiSharedPreferences.getString("phone", "");
    }

    public static String getDeviceToken() {
        return WiSharedPreferences.getString("token", "");
    }

    public static void setCurrentActivity(String currentActivity){
        WiUtils.currentActivity = currentActivity;
    }

    public static String getCurrentActivity(){
        return currentActivity;
    }

    private static long random10DigitNumber(){
        Random r = new Random();
        int upper = Integer.MAX_VALUE;
        int lower = 1000000000;

        return r.nextInt(upper - lower) + lower;
    }

    public static boolean isWifiManagerEnabled(){
        return WiSharedPreferences.getBoolean(WiSharedPreferences.KEY_WIFI_MANAGER_ENABLED, true);
    }

    public static boolean isDemoEnabled(){
        return WiSharedPreferences.getBoolean(WiSharedPreferences.KEY_DEMO_MODE, false);
    }

    public static String randomPhoneNumber(){
        return formatPhoneNumber(String.valueOf(random10DigitNumber()));
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

    public static boolean isDatabaseEnabled() {
        return WiSharedPreferences.getBoolean(WiSharedPreferences.KEY_DATABASE_ENABLED, true);
    }

    public static int randomBetween(int min, int max){
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
}

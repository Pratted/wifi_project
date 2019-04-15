package com.example.eric.wishare;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WiSharedPreferences {
    private static Map<String, Object> mReadOnly;
    private static Map<String, Object> mBuffer;
    private static SharedPreferences.Editor mEditor;
    private static String PREFS_FILE = "wishare.prefs";
    private static boolean initialized = false;

    private WiSharedPreferences() {}

    public static String KEY_DATABASE_ENABLED = "db_enabled";
    public static String KEY_SEND_INVITATIONS_TO_SELF = "send_invitations_to_self";

    public synchronized static void putString(String key, String value) {
        mBuffer.put(key, value);
        mEditor.putString(key, value);
    }

    public synchronized static void putInteger(String key, Integer value){
        mBuffer.put(key, value);
        mEditor.putInt(key, value);
    }

    public synchronized static void putBoolean(String key, Boolean value){
        mBuffer.put(key, value);
        mEditor.putBoolean(key, value);
    }

    public synchronized static String getString(String key, String def) {
        if(mBuffer.containsKey(key)) return (String) mBuffer.get(key);
        if(mReadOnly.containsKey(key)) return (String) mReadOnly.get(key);

        return def;
    }

    public synchronized static Integer getInteger(String key, Integer def){
        if(mBuffer.containsKey(key)) return (Integer) mBuffer.get(key);
        if(mReadOnly.containsKey(key)) return (Integer) mReadOnly.get(key);

        return def;
    }

    public synchronized static Boolean getBoolean(String key, Boolean def){
        if(mBuffer.containsKey(key)) return (Boolean) mBuffer.get(key);
        if(mReadOnly.containsKey(key)) return (Boolean) mReadOnly.get(key);

        return def;
    }

    // when data is saved, overwrite the data in readonly with the buffer
    public synchronized static void save(){
        for(String key: mBuffer.keySet()){
            mReadOnly.put(key, mBuffer.get(key));
        }

        mBuffer.clear();
        mEditor.apply();
    }

    public synchronized static void initialize(Context context){
        if(initialized) return;

        mReadOnly = new HashMap<>();
        mBuffer = new HashMap<>();

        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();

        for(String key: map.keySet()){
            mReadOnly.put(key, map.get(key));
        }

        mEditor = context.getApplicationContext().getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit();
        initialized = true;
    }
}

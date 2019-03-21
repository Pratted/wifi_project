package com.example.eric.wishare;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WiSharedPreferences {

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private WeakReference<Context> mContext;
    private static WiSharedPreferences mWiSharedPreferences;
    private Map<String, String> keyValue;

    private WiSharedPreferences(Context context) {
        keyValue = new HashMap<>();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPrefs.edit();
//        mEditor.apply();
    }

    public void putString(String name, String token) {
        mEditor.putString(name, token);
        keyValue.put(name, token);
        mEditor.apply();
    }

    public String getString(String name) {
        return keyValue.get(name);
    }

    public String getFromSharedPrefs(String name) {
        return mPrefs.getString(name, "");
    }

    public static WiSharedPreferences getInstance(Context c) {
        if(mWiSharedPreferences == null) {
            mWiSharedPreferences = new WiSharedPreferences(c.getApplicationContext());
        }
        return mWiSharedPreferences;
    }
}

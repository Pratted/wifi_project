package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.model.WiContact;

import java.security.Permission;
import java.util.ArrayList;

import static com.example.eric.wishare.WiPermissions.CONTACT;
import static com.example.eric.wishare.WiPermissions.LOCATION;
import static com.example.eric.wishare.WiPermissions.PHONE;

public class WiSplash extends Activity {
    private WiPermissions mPermissions;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int result: grantResults){
            if(result != PackageManager.PERMISSION_GRANTED){
                new MaterialDialog.Builder(this)
                        .title("Cannot start WiShare")
                        .content("WiShare needs Phone, Contact and Location permissions to start. Please restart the app and allow the permissions to proceed")
                        .canceledOnTouchOutside(false)
                        .positiveText("Ok")
                        .show();
                return;
            }
        }

        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String phone = manager.getLine1Number();

        if(phone.isEmpty()){
            new MaterialDialog.Builder(this)
                    .title("WiShare needs your phone number")
                    .content("WiShare was unable to find your phone number. Please input it manually.")
                    .canceledOnTouchOutside(false)
                    .input("123-456-7890", "", false, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            String phone = input.toString();
                            phone = WiUtils.formatPhoneNumber(phone);
                            editor.putString("phone", phone);
                            editor.commit();

                            startWiShare();
                        }
                    })
                    .show();
        }
        else{
            phone = WiUtils.formatPhoneNumber(phone);
            editor.putString("phone", phone);

            Log.d("Shit", "Saving Phone: " + phone);
            editor.commit();

            startWiShare();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        mPermissions = WiPermissions.getInstance(this);

        if(!mPermissions.hasAllPermissions()){
            final ArrayList<String> request = new ArrayList<>();
            String description = "";

            if(!mPermissions.hasPermission(PHONE)){
                request.add(PHONE);
                description += "WiShare needs access to your phone number to communicate with contacts\n";
            }
            if(!mPermissions.hasPermission(CONTACT)){
                request.add(CONTACT);
                description += "Contact: WiShare needs your contact list to send and receive invitations\n";
            }
            if(!mPermissions.hasPermission(LOCATION)){
                request.add(LOCATION);
                description += "Location: WiShare needs location permission to add WifiNetworks to your device\n";
            }

            new MaterialDialog.Builder(this)
                    .title("WiShare is requesting Phone, Contact and Location permission")
                    .content(description)
                    .positiveText("Ok")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request.size() > 0) {
                                requestPermissions(request.toArray(new String[request.size()]), 5);
                            }
                        }
                    })
                    .canceledOnTouchOutside(false)
                    .show();
        }
        else{
            startWiShare(200);
        }
    }

    public void startWiShare(int delay){
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(WiSplash.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, delay);
    }

    public void startWiShare(){
        startWiShare(0);
    }
}
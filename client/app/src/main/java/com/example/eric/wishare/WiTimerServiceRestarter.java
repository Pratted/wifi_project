package com.example.eric.wishare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class WiTimerServiceRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(WiTimerService.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, WiTimerService.class));
        } else {
            context.startService(new Intent(context, WiTimerService.class));
        }
    }
}

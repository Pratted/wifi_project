package com.example.eric.wishare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WiTimerService extends Service {
    private final String TAG = "WiTimerService";
    private Map<String, CountDownTimer> mTimers;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String ssid = intent.getStringExtra("ssid");
            String time = intent.getStringExtra("timeout");

            Log.d(TAG, "Message receieved. Mtimers == null" + (mTimers == null));

            if(mTimers != null && !mTimers.containsKey(ssid)){
                Log.d(TAG, "Adding timer...");
                CountDownTimer timer = createTimer(WiUtils.fromDateTime(time), ssid);
                timer.start();
                mTimers.put(ssid, timer);
                Log.d(TAG, "Timer added!");
            }
        }
    };

    private void init(){
        //LocalBroadcastManager.getInstance(this).
        //        registerReceiver(mMessageReceiver, new IntentFilter("timer"));

        if(mTimers == null) {
            mTimers = new HashMap<>();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    public WiTimerService(Context context){
        super();
        Log.d(TAG, "Constructor called");

        init();
    }

    public WiTimerService(){
        Log.d(TAG, "Default Constructor called");

        init();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate() called");

        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    public void initializeTimers(){
        Map<String, Date> timeouts = WiSQLiteDatabase.getInstance(this).loadTimeouts();

        for(final String ssid: timeouts.keySet()){
            Date future = timeouts.get(ssid);
            CountDownTimer timer = createTimer(future, ssid);
            timer.start();
            mTimers.put(ssid, timer);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private CountDownTimer createTimer(Date timeout, final String ssid){
        Date now = new Date();
        long diff = timeout.getTime()- now.getTime();

        return new CountDownTimer(diff, diff / 2) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Log.d(TAG, ssid + " timer has expired!!");

                if(mTimers != null && mTimers.get(ssid) != null){
                    Log.d(TAG, "Removing Timer");
                    mTimers.remove(ssid);
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("WiTimerService", "ondestroy!");
        Intent broadcastIntent = new Intent(this, WiTimerServiceRestarter.class);
        sendBroadcast(broadcastIntent);
    }
}

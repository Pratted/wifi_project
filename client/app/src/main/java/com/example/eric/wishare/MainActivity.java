package com.example.eric.wishare;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkList;

    private Button btnShowNotification;

    private WiMyInvitationsButton btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiManageContactsDialog mContactListDialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // contact permission accepted..
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mContactListDialog = new WiManageContactsDialog(this, btnManageContacts);

            mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
                @Override
                public void onContactSelected(WiContact contact) {
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
            });

            //need the contact list loaded before showing the dialog. do this SYNCHRONOUSLY
            mContactListDialog.loadContacts();
            mContactListDialog.refresh(this);
            //mContactListDialog.show();

            mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
            mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

//        NotificationChannel channel = null;
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            channel = new NotificationChannel("ChanID", "name",
//            NotificationManager.IMPORTANCE_HIGH);
//            channel.setDescription("desc");
//
//            final NotificationManager nm = (NotificationManager)
//            this.getSystemService(Context.NOTIFICATION_SERVICE);
//            nm.createNotificationChannel(channel);
//            final Notification notification = new NotificationCompat.Builder(this, "ChanID")
//                    .setSmallIcon(R.drawable.ic_wifi_black_48dp)
//                    .setContentTitle("Notification")
//                    .setContentText("This is a notification")
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setContentIntent(resultPendingIntent)
//                    .build();
//
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    nm.notify(1, notification);
//                    System.out.println("IN RUN");
//                }
//            }, 5000);
//        } else {
//
//            Intent intent = new Intent(this, MainActivity.class);
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            @SuppressWarnings("deprecation") final NotificationCompat.Builder b = new NotificationCompat.Builder(this);
//
//            b.setAutoCancel(true)
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setWhen(System.currentTimeMillis())
//                    .setSmallIcon(R.drawable.ic_wifi_black_48dp)
//                    .setContentTitle("Notification")
//                    .setContentText("This is a notification")
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setContentIntent(contentIntent)
//                    .setContentInfo("Info");
//
//
////            final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//////            notificationManager.notify(1, b.build());
////
////            Handler handler = new Handler();
////            handler.postDelayed(new Runnable() {
////                @Override
////                public void run() {
////                    notificationManager.notify(1, b.build());
////                    System.out.println("IN RUN");
////                }
////            }, 5000);
////        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        btnShowNotification = findViewById(R.id.btn_show_notification);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);






        mConfiguredNetworkList = findViewById(R.id.configured_network_list);

        mInvitationListDialog = new WiInvitationListDialog(this, btnMyInvitations);
        mInvitationListDialog.add(new WiInvitation("belkin-622", "Eric Pratt", "Never", "127 hours", "10GB"));
        mInvitationListDialog.add(new WiInvitation("belkin-048", "Joseph Vu", "2/28/2019", "36 hours", "5GB"));
        mInvitationListDialog.add(new WiInvitation("home-255", "Aditya Khandkar", "3/15/2019", "Never", "None"));
        mInvitationListDialog.add(new WiInvitation("home-200", "Jacob Fullmer", "3/15/2019", "24 hours", "3GB"));


        /**
         need contact permission to build the ContactListDialog
         if contact permission is not granted, the user will be prompted on Manage Contacts button click
         if the user grants permission, the callback onPermissionResult() will construct the WiContactListDialog
         **/
        if(WiContactList.hasContactPermissions(this)){
            mContactListDialog = new WiManageContactsDialog(this, btnManageContacts);
            mContactListDialog.loadContactsAsync(); // start loading the contacts asynchronously.

            mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
                @Override
                public void onContactSelected(WiContact contact){
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
            });

            mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
            mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());
        }
        else{
            // if there are no permissions, make onClick for the button request permissions...
            btnManageContacts.setOnClickListener(requestContactPermissions());
            btnAddNetwork.setOnClickListener(requestContactPermissions());

        }




        btnShowNotification.setOnClickListener(sendNotification());





    }
    private View.OnClickListener sendNotification(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){
                WiNotification notification = new WiNotification(MainActivity.this, "Title", "Description");
                notification.show();
            }
        };
    }

    private View.OnClickListener requestContactPermissions(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 87);
                }
            }
        };
    }

    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEntered(){
        return new WiAddNetworkDialog.OnPasswordEnteredListener() {
            @Override
            public void OnPasswordEntered(WiConfiguration config) {
                mConfiguredNetworkList.addView(config);
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();

//        mAddNetworkDialog.refresh(this);
        mInvitationListDialog.refresh(this);

        if(mContactListDialog != null) {
            mContactListDialog.refresh(this);
        }
    }

    private void plzFirebase(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        Log.d(TAG, "The token is: " + token);
//                Toast.makeText(MainActivity.this, "The token is: " + token , Toast.LENGTH_SHORT).show();

                    }
                });
    }
}

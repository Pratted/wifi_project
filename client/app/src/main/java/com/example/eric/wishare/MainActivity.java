package com.example.eric.wishare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
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

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkList;

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
            mContactListDialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        mConfiguredNetworkList = findViewById(R.id.configured_network_list);

        mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
        mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());

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
        }
        else{
            // if there are no permissions, make onClick for the button request permissions...
            btnManageContacts.setOnClickListener(requestContactPermissions());
        }
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

        mAddNetworkDialog.refresh(this);
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

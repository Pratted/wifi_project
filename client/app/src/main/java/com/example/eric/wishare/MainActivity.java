package com.example.eric.wishare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
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
    private ScrollView mNetworkScrollView;
    private LinearLayout mScrollView;

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

        mNetworkScrollView = findViewById(R.id.scroll_network_list);

        mScrollView = new LinearLayout(this);
        mScrollView.setOrientation(LinearLayout.VERTICAL);
        mNetworkScrollView.addView(mScrollView);

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
    }

    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEntered(){
        return new WiAddNetworkDialog.OnPasswordEnteredListener() {
            @Override
            public void OnPasswordEntered(WiConfiguration config) {
                LayoutInflater inflater = getLayoutInflater();
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_configured_network_list_item, null);

                // random # of users
                int users = config.hashCode() % 5;
                if(users < 0) users *= -1;

                ((TextView) layout.findViewById(R.id.tv_network_name)).setText(config.getSSID());
                ((TextView) layout.findViewById(R.id.tv_active_users)).setText(users + " active user(s)");

                if(users % 2 == 0)
                    ((ImageView) layout.findViewById(R.id.iv_configured_status)).setImageResource(R.drawable.ic_check_green_24dp);
                //((ImageView) layout.findViewById(R.id.iv_configured_status))
                mScrollView.addView(layout);
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

package com.example.eric.wishare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private ScrollView mNetworkScrollView;
    private ArrayList<WiInvitation> mInvitations;
    private LinearLayout mScrollView;

    private TextView tvNumberOfInvites;

    private Button btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiContactListDialog mContactListDialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // contact permission accepted..
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mContactListDialog = new WiContactListDialog(this);

            mContactListDialog.setOnContactSelectedListener(new WiContactListDialog.OnContactSelectedListener() {
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

        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        tvNumberOfInvites = findViewById(R.id.tv_number_of_invites);

        mNetworkScrollView = findViewById(R.id.scroll_network_list);
        // ScrollView can only have 1 child
        // Adding linear layout as a child solves the problem
        mScrollView = new LinearLayout(this);
        mScrollView.setOrientation(LinearLayout.VERTICAL);

        mNetworkScrollView.addView(mScrollView);

        mAddNetworkDialog = new WiAddNetworkDialog(this);
        mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());

        mInvitations = new ArrayList<>();
        mInvitations.add(new WiInvitation("belkin-622", "Eric Pratt", "Never", "127 hours", "10GB"));
        mInvitations.add(new WiInvitation("belkin-048", "Joseph Vu", "2/28/2019", "36 hours", "5GB"));
        mInvitations.add(new WiInvitation("home-255", "Aditya Khandkar", "3/15/2019", "Never", "None"));
        mInvitations.add(new WiInvitation("home-200", "Jacob Fullmer", "3/15/2019", "24 hours", "3GB"));

        mInvitationListDialog = new WiInvitationListDialog(this, mInvitations, tvNumberOfInvites);

        /**
         need contact permission to build the ContactListDialog
         if contact permission is not granted, the user will be prompted on Manage Contacts button click
         if the user grants permission, the callback onPermissionResult() will construct the WiContactListDialog
         **/
        if(WiContactList.hasContactPermissions(this)){
            mContactListDialog = new WiContactListDialog(this);
            mContactListDialog.loadContactsAsync(); // start loading the contacts asynchronously.

            mContactListDialog.setOnContactSelectedListener(new WiContactListDialog.OnContactSelectedListener() {
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

        btnAddNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddNetworkDialog.show();
            }
        });

        btnManageContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!WiContactList.hasContactPermissions(MainActivity.this)){
                    WiContactList.requestContactPermissions(MainActivity.this);
                }
                else{
                    mContactListDialog.show();
                }
            }
        });

        btnMyInvitations.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mInvitationListDialog.show();
            }
        });

        tvNumberOfInvites.setText(mInvitations.size() + "");
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

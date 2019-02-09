package com.example.eric.wishare;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private ScrollView mNetworkScrollView;
    private ArrayList<WiInvitation> mInvitations;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiContactListDialog mContactListDialog;

    private MaterialDialog.ListCallback onContactSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence contactName) {
                Toast.makeText(MainActivity.this, "You selected " + contactName, Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    private TextView tvNumberOfInvites;

    private Button btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        tvNumberOfInvites = findViewById(R.id.tv_number_of_invites);

        //mContactList = new WiContactList(this);
        mInvitations = new ArrayList<>();


        /*
        mNetworkScrollView = findViewById(R.id.scroll_network_list);
        // ScrollView can only have 1 child
        // Adding linear layout as a child solves the problem
        LinearLayout scrollLayout = new LinearLayout(this);
        scrollLayout.setOrientation(LinearLayout.VERTICAL);
        mNetworkScrollView.addView(scrollLayout);

        // Get the phone's configured Wifi networks.
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(MainActivity.this.WIFI_SERVICE);
        final List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();
        TextView temp;
        int counter = 0;
        for(WifiConfiguration item : wifiList) {
            System.out.println(counter++ + item.SSID);
            temp = new TextView(this);
            temp.setText(item.SSID.replace("\"", ""));
            temp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollLayout.addView(temp);
        }
        */

        mInvitations.add(new WiInvitation("belkin-622", "Eric Pratt", "Never", "127 hours", "10GB"));
        mInvitations.add(new WiInvitation("belkin-048", "Joseph Vu", "2/28/2019", "36 hours", "5GB"));
        mInvitations.add(new WiInvitation("home-255", "Aditya Khandkar", "3/15/2019", "Never", "None"));
        mInvitations.add(new WiInvitation("home-200", "Jacob Fullmer", "3/15/2019", "24 hours", "3GB"));

        mAddNetworkDialog = new WiAddNetworkDialog(this);
        mContactListDialog = new WiContactListDialog(this);
        mInvitationListDialog = new WiInvitationListDialog(this, mInvitations, tvNumberOfInvites);

        mContactListDialog.setOnContactSelectedListener(new WiContactListDialog.OnContactSelectedListener() {
            @Override
            public void onContactSelected(WiContact contact) {
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        mAddNetworkDialog.refresh(this);
        mContactListDialog.refresh(this);
        mInvitationListDialog.refresh(this);

        btnAddNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddNetworkDialog.show();
            }
        });

        btnManageContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WiContactList.requestContactPermissions(MainActivity.this);
                mContactListDialog.show();
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

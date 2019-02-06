package com.example.eric.wishare;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private ScrollView mNetworkScrollView;
    private ArrayList<String> mUnreadInvitations;

    private MaterialDialog.ListCallback onNetWorkSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                Toast.makeText(MainActivity.this, "You entered " + input, Toast.LENGTH_LONG).show();
                            }}).show();
            }
        };
    }

    private MaterialDialog.ListCallback onContactSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence contactName) {
                Toast.makeText(MainActivity.this, "You selected " + contactName, Toast.LENGTH_LONG).show();
            }
        };
    }

    private MaterialDialog.ListCallback onInviteSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Name")
                        .negativeText("Decline")
                        .positiveText("Accept")
                        .show();
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNetworkScrollView = findViewById(R.id.scroll_network_list);

        findViewById(R.id.btn_add_network).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> networks = new ArrayList<>();
                networks.add("Hyuntaes crib");
                networks.add("Eric Home");
                networks.add("Foo");
                networks.add("Bar");
                networks.add("Club");

                new MaterialDialog.Builder(MainActivity.this)
                    .title("Select Network")
                    .items(networks)
                    .itemsCallback(onNetWorkSelect())
                    .negativeText("Cancel")
                    .show();
            }
        });

        findViewById(R.id.btn_manage_contacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Manage Contacts")
                        .itemsCallback(onContactSelect())
                        .negativeText("Cancel")
                        .show();
            }
        });

        mUnreadInvitations = new ArrayList<>();
        mUnreadInvitations.add("Invitation onto 'Eric's crib' from Eric Pratt");
        mUnreadInvitations.add("Invitation onto 'Hyuntae's hangout from Hyuntae Na");

        findViewById(R.id.btn_my_invitations).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        new MaterialDialog.Builder(MainActivity.this)
                                .title("My Invitations")
                                .items(mUnreadInvitations)
                                .negativeText("Close")
                                .itemsCallback(onInviteSelect())
                                .show();
                    }
                }
        );

        ((TextView) findViewById(R.id.tv_number_of_invites)).setText(mUnreadInvitations.size() + "");


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

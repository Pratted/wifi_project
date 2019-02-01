package com.example.eric.wishare;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasContactPermissions() {
        return checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestContactPermissions(){
        if(!hasContactPermissions()){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS} , 87);

            if(!hasContactPermissions()){
                new MaterialDialog.Builder(this)
                        .content("WiShare needs access to your contacts for this feature")
                        .positiveText("Ok")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 87);
                            }
                        })
                        .show();
            }
        }
    }

    private ArrayList<String> fetchContacts(){
        ArrayList<String> contacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,null,null);

        if((cur != null ? cur.getCount() : 0) > 0){
            while(cur.moveToNext()){
                contacts.add(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            }
        }

        return contacts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                if(!hasContactPermissions())
                    requestContactPermissions();

                ArrayList<String> contacts = fetchContacts();

                new MaterialDialog.Builder(MainActivity.this)
                        .title("Manage Contacts")
                        .items(contacts)
                        .itemsCallback(onContactSelect())
                        .negativeText("Cancel")
                        .show();
            }
        });

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
                Toast.makeText(MainActivity.this, "The token is: " + token , Toast.LENGTH_SHORT).show();

            }
        });

    }
}

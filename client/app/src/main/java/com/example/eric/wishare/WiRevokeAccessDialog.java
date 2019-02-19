package com.example.eric.wishare;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

public class WiRevokeAccessDialog extends WiDialog{

    private ArrayList<String> networksToAdd = new ArrayList<>();

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Select networks revoke this contact from")
                .items(networksToAdd)
                .positiveText("Revoke")
                .negativeText("Cancel")
                .build();

    }

    public WiRevokeAccessDialog(Context context, Button btnRevokeAccess) {
        super(context);
        networksToAdd.add("Home");
        networksToAdd.add("Joe's crib");
        networksToAdd.add("Jim's house");

        btnRevokeAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiRevokeAccessDialog.this.show();
            }
        });
    }
}

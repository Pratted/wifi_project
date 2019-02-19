package com.example.eric.wishare;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;

import java.util.ArrayList;

public class WiAddContactToNetworkDialog extends WiDialog {

    private ArrayList<String> networksToAdd = new ArrayList<>();

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Select networks to add this contact to")
                .items(networksToAdd)
                .positiveText("Invite")
                .negativeText("Cancel")
                .build();

    }


    public WiAddContactToNetworkDialog(Context context, Button btnAddContactToNetwork) {
        super(context);

        networksToAdd.add("Home");
        networksToAdd.add("Joe's crib");
        networksToAdd.add("Jim's house");

        btnAddContactToNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiAddContactToNetworkDialog.this.show();
            }
        });




    }
}

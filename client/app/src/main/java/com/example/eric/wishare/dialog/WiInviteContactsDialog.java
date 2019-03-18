package com.example.eric.wishare.dialog;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.model.WiContact;

import java.util.ArrayList;

public class WiInviteContactsDialog extends WiDialog {

    private ArrayList<WiContact> mContacts = new ArrayList<>();

    @Override
    public MaterialDialog build() {
        return new MaterialDialog.Builder(context.get())
                .title("Select Contacts")
                .items(mContacts)
                .itemsCallbackMultiChoice(null, ignore())
                .onPositive(onInviteClick())
                .positiveText("Invite")
                .negativeText("Cancel")
                .build();
    }

    private MaterialDialog.ListCallbackMultiChoice ignore(){
        return new MaterialDialog.ListCallbackMultiChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                return false;
            }
        };
    }

    private MaterialDialog.SingleButtonCallback onInviteClick(){
        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Integer indices[] = dialog.getSelectedIndices();

                for(Integer i: indices){
                    System.out.println("You invited " + mContacts.get(i).getName());
                }

                new WiCreateInvitationDialog(context.get()).show();
            }
        };
    }

    public WiInviteContactsDialog(Context context){
        super(context);
    }

    public void addContact(WiContact contact){
        mContacts.add(contact);
    }
}

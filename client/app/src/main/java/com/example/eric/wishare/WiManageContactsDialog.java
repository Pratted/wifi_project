package com.example.eric.wishare;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

public class WiManageContactsDialog extends WiDialog{
    private WiContactList mContactList;
    private OnContactSelectedListener mOnContactSelectedListener;

    interface OnContactSelectedListener{
        void onContactSelected(WiContact contact);
    }

    public WiManageContactsDialog(Context context, Button btnManageContacts){
        super(context);

        mContactList = new WiContactList(context);
        mContactList.setOnContactListReadyListener(onContactListReady());

        btnManageContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("HEREEEEEEEEEEEEEEEEEe");
                WiManageContactsDialog.this.show();
            }
        });
    }

    private WiContactList.OnContactListReadyListener onContactListReady(){
        return new WiContactList.OnContactListReadyListener() {
            @Override
            public void onContactListReady(ArrayList<WiContact> contacts) {
                build();
            }
        };
    }

    private MaterialDialog.ListCallback onContactClicked(){
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                mOnContactSelectedListener.onContactSelected(mContactList.getWiContacts().get(position));
            }
        };
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener){
        mOnContactSelectedListener = listener;
    }

    public MaterialDialog build(){
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<WiContact> contacts = mContactList.getWiContacts();

        for (WiContact contact : contacts) {
            strings.add(contact.getName() + " " + contact.getPhone());
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select a Contact")
                .items(strings)
                .itemsCallback(onContactClicked())
                .negativeText("Cancel")
                .build();
    }

    public void loadContacts(){
        mContactList.load();
    }

    public void loadContactsAsync(){
        mContactList.loadAsync(context.get());
    }
}

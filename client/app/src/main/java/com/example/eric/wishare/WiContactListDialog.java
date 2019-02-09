package com.example.eric.wishare;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WiContactListDialog implements WiDialog{
    private MaterialDialog mDialog;
    private WiContactList mContactList;
    private WeakReference<Context> mContext;

    private OnContactSelectedListener mOnContactSelectedListener;

    interface OnContactSelectedListener{
        void onContactSelected(WiContact contact);
    }

    public WiContactListDialog(Context context){
        mContext = new WeakReference<>(context);
        mContactList = new WiContactList(context);

        mContactList.setOnContactListReadyListener(onContactListReady());

        // contacts already retrieved. used cached array list...
        if(mContactList.size() > 0) {
            build(mContactList.getWiContacts());
        }

        // otherwise load asynchronously. list will populate in callback onContactListReady()
        else {
            mContactList.loadAsync(context);
        }
    }

    private WiContactList.OnContactListReadyListener onContactListReady(){
        return new WiContactList.OnContactListReadyListener() {
            @Override
            public void onContactListReady(ArrayList<WiContact> contacts) {
                build(contacts);
            }
        };
    }

    private MaterialDialog.ListCallback onContactClicked(){
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                mOnContactSelectedListener.onContactSelected(new WiContact("Eric", "6107370292"));
            }
        };
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener){
        mOnContactSelectedListener = listener;
    }

    private void build(ArrayList<WiContact> contacts){
        ArrayList<String> strings = new ArrayList<>();

        for (WiContact contact : contacts) {
            strings.add(contact.name + " " + contact.phone);
        }

        mDialog = new MaterialDialog.Builder(mContext.get())
                .title("Manage Contacts")
                .items(strings)
                .itemsCallback(onContactClicked())
                .negativeText("Cancel")
                .build();
    }

    public void show(){
        mDialog.show();
    }

    @Override
    public void refresh(Context context) {
        mContext = new WeakReference<>(context);
    }
}

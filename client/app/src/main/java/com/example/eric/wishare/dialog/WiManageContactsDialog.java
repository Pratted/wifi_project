package com.example.eric.wishare.dialog;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.model.WiContact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class WiManageContactsDialog extends WiDialog{
    private WiContactList mContactList;
    private OnContactSelectedListener mOnContactSelectedListener;
    private ArrayList<WiContact> mContacts;
    private LinearLayout mCustomLayout;


    public interface OnContactSelectedListener{
        void onContactSelected(WiContact contact);
    }

    public WiManageContactsDialog(final Context context){
        super(context);
        mContactList = WiContactList.getInstance(context);
        mContactList.setOnContactListReadyListener(onContactListReady());
        mCustomLayout = new LinearLayout(context);
        mCustomLayout.setOrientation(LinearLayout.VERTICAL);
    }

    private WiContactList.OnContactListReadyListener onContactListReady(){
        return new WiContactList.OnContactListReadyListener() {
            @Override
            public void onContactListReady(HashMap<String, WiContact> contacts) {
                build();
            }
        };
    }

    private MaterialDialog.ListCallback onContactClicked(){
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                mOnContactSelectedListener.onContactSelected(mContacts.get(position));
            }
        };
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener){
        mOnContactSelectedListener = listener;
    }

    public MaterialDialog build(){
        mCustomLayout.removeAllViews();
        mContacts = new ArrayList(mContactList.getWiContacts().values());

        // sort alphabetically if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mContacts.sort(new Comparator<WiContact>() {
                @Override
                public int compare(WiContact left, WiContact right) {
                    return left.getName().compareTo(right.getName());
                }
            });
        }

        for(final WiContact contact: mContacts){
            WiContactListItem item = new WiContactListItem(context.get(), contact.getName());

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnContactSelectedListener.onContactSelected(contact);
                    dismiss();
                }
            });

            mCustomLayout.addView(item);
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select a Contact")
                .customView(mCustomLayout, true)
                .negativeText("Cancel")
                .build();
    }

    public class WiContactListItem extends LinearLayout {

        public WiContactListItem(Context context, String name) {
            super(context);

            inflate(getContext(), R.layout.layout_contact_list_item, this);

            ((TextView) findViewById(R.id.tv_contact_name)).setText(name);
        }
    }
}

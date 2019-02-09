package com.example.eric.wishare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WiAddNetworkDialog implements WiDialog {
    private WeakReference<Context> mContext;
    private MaterialDialog mDialog;
    private WiNetworkManager mManager;
    private ArrayList<String> mNetworks;

    public WiAddNetworkDialog(final Context context){
        mContext = new WeakReference<>(context);

        mNetworks = new ArrayList<>();
        mNetworks.add("Hyuntaes crib");
        mNetworks.add("Eric Home");
        mNetworks.add("Foo");
        mNetworks.add("Bar");
        mNetworks.add("Club");

        mDialog = new MaterialDialog.Builder(context)
                .title("Select Network")
                .items(mNetworks)
                .itemsCallback(onNetWorkSelect())
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

    private MaterialDialog.ListCallback onNetWorkSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                new MaterialDialog.Builder(mContext.get())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                Toast.makeText(mContext.get(), "You entered " + input, Toast.LENGTH_LONG).show();
                            }}).show();
            }
        };
    }
}

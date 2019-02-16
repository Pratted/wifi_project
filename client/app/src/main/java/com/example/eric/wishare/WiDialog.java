package com.example.eric.wishare;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

abstract class WiDialog {

    protected MaterialDialog dialog;
    protected MaterialDialog.Builder builder;
    protected WeakReference<Context> context;

    // override this method to build the dialog
    public abstract void build();

    public WiDialog(Context context){
        refresh(context);
    }

    public void show(){
        //build();
        dialog.show();
    }

    public void refresh(Context context){
        this.context = new WeakReference<>(context);
        builder = new MaterialDialog.Builder(context);
    }
}

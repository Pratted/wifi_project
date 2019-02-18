package com.example.eric.wishare;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

abstract class WiDialog {
    protected WeakReference<Context> context;

    // this method builds a specific dialog.
    // each subclass should implement this differently
    public abstract MaterialDialog build();

    public WiDialog(Context context){
        refresh(context);
    }

    public void show(){
        build().show();
    }

    public void refresh(Context context){
        this.context = new WeakReference<>(context);
    }
}

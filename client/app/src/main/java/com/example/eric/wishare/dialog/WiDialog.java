package com.example.eric.wishare.dialog;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

abstract public class WiDialog {
    protected WeakReference<Context> context;
    private MaterialDialog mDialog;

    /*****************************************************
     this method builds a specific dialog.
     each subclass should implement this differently

     For example:

     @Override
     public MaterialDialog build(){
        return new MaterialDialog.Builder(context.get())
            .title("blah blah")
            ... your stuff here
            .build();
     }
    ******************************************************/
    public abstract MaterialDialog build();

    public WiDialog(Context context){
        refresh(context);
    }

    public void show(){
        mDialog = build();
        mDialog.show();
    }

    public void dismiss(){
        mDialog.dismiss();
    }

    public void refresh(Context context){
        this.context = new WeakReference<>(context);
    }
}

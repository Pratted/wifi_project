package com.example.eric.wishare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment")
public class WiPermittedContactsFragment extends Fragment {

    interface OnReadyListener{
        void onReady(View view);
    }

    OnReadyListener mOnReadyListener;

    public void setOnReadyListener(OnReadyListener listener){
        mOnReadyListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(
                R.layout.tabbed_view_permitted_contacts, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        mOnReadyListener.onReady(getView());
    }
}

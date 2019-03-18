package com.example.eric.wishare.view;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.example.eric.wishare.R;

import java.util.ArrayList;

public abstract class WiPage extends LinearLayout{
    private LinearLayout mHeaders;
    private LinearLayout mItems;
    private int mCheckBoxVisibilities;

    private CheckBox mHeaderSelectAll;
    private ArrayList<CheckBox> mCheckBoxes;

    public WiPage(Context context) {
        super(context);

        mCheckBoxes = new ArrayList<>();

        init();

        mHeaderSelectAll = findViewById(R.id.cb_select_all);
        mHeaders = findViewById(R.id.headers);
        mItems = findViewById(R.id.items);

        setCheckBoxVisibilities(INVISIBLE);

        if(mHeaderSelectAll != null){
            mHeaderSelectAll.setOnCheckedChangeListener(onSelectAll());
        }
    }

    protected abstract void init();

    protected abstract void refresh();

    public void setHeaderVisibility(int visibility){
        if(mHeaders != null){
            mHeaders.setVisibility(visibility);
        }
    }

    public int getCheckBoxVisibilties(){
        return mCheckBoxVisibilities;
    }

    public int getSelectedItemCount(){
        int qty = 0;
        for(CheckBox checkBox: mCheckBoxes){
            qty += checkBox.isChecked() ? 1 : 0;
        }

        return qty;
    }


    public void addListItem(View view){
        mItems.addView(view);
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_select);

        if(checkbox != null) mCheckBoxes.add(checkbox);
    }

    public void removeListItem(View view){
        CheckBox cb = view.findViewById(R.id.cb_select);
        if(cb != null){
            mCheckBoxes.remove(cb);
        }

        mItems.removeView(view);
    }

    public void removeAllItems(){
        mCheckBoxes.clear();
        mItems.removeAllViews();
    }

    private CompoundButton.OnCheckedChangeListener onSelectAll(){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(CheckBox checkBox: mCheckBoxes){
                    checkBox.setChecked(isChecked);
                }
            }
        };
    }

    public void setCheckBoxVisibilities(int visibility){
        if(mHeaderSelectAll != null){
            mHeaderSelectAll.setVisibility(visibility);
            mHeaderSelectAll.setChecked(false);
        }

        mCheckBoxVisibilities = visibility;

        for(CheckBox cb: mCheckBoxes){
            cb.setVisibility(visibility);
        }
    }
}

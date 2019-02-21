package com.example.eric.wishare;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItems;

public class WiTabbedScrollView extends LinearLayout {

    private WiTabbedScrollViewPager mViewPager;
    private SmartTabLayout mViewPagerTab;

    public WiTabbedScrollView(Context context){
        super(context);

        init();
    }

    public WiTabbedScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_smart_tab, this);

        ViewPagerItemAdapter adapter = new ViewPagerItemAdapter(ViewPagerItems.with(getContext())
                .add("Permitted Contacts", R.layout.tabbed_view_permitted_contacts)
                .add("idfk", R.layout.tabbed_view_permitted_contacts)
                .create());

        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(adapter);

        mViewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        mViewPagerTab.setViewPager(mViewPager);
    }
}



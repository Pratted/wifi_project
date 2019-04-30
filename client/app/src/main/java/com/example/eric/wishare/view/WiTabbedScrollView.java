package com.example.eric.wishare.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiPagerAdapter;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WiTabbedScrollView extends LinearLayout {

    private WiTabbedScrollViewPager mViewPager;
    private WiPagerAdapter mPagerAdapter;
    private WiContactList mContactList;
    private WeakReference<Context> mContext;

    private WiPermittedContactsView mPermittedContactsView;
    private WiInvitableContactsView mInvitableContactsView;

    private Button mLhs;
    private Button mRhs;

    public WiTabbedScrollView(Context context){
        super(context.getApplicationContext());
        mContext = new WeakReference<>(context.getApplicationContext());
        init();
    }

    public WiTabbedScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = new WeakReference<>(context.getApplicationContext());
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_tabbed_scroll_view, this);
        mLhs = findViewById(R.id.btn_lhs);
        mRhs = findViewById(R.id.btn_rhs);

        mContactList = WiContactList.getInstance(mContext.get().getApplicationContext());

        mViewPager = findViewById(R.id.view_pager);
    }

    public void setWiConfiguration(WiConfiguration config){
        mPermittedContactsView = new WiPermittedContactsView(getContext(), mLhs, mRhs, config);
        mInvitableContactsView = new WiInvitableContactsView(getContext(), mLhs, mRhs, config);

        for(WiContact contact: mContactList.getWiContacts().values()){
            if(contact.hasAccessTo(config.SSID)){
                mPermittedContactsView.addPermittedContact(contact);
            }
            else{
                mInvitableContactsView.add(contact);
            }
        }

        mPagerAdapter = new WiPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);

        mInvitableContactsView.sortName(true);

        mPagerAdapter.addView(mPermittedContactsView);
        mPagerAdapter.notifyDataSetChanged();

        mPagerAdapter.addView(mInvitableContactsView);
        mPagerAdapter.notifyDataSetChanged();

        final TabLayout mTabs = findViewById(R.id.tab_layout);
        mTabs.setupWithViewPager(mViewPager);

        System.out.println(mTabs.getTabCount());
        mTabs.getTabAt(0).setText("Permitted Contacts");
        mTabs.getTabAt(1).setText("Invite Contacts");

        mTabs.addOnTabSelectedListener(onTabSelected());

        mPermittedContactsView.sort(WiPermittedContactsView.COL_NAME); //descending order
        mPermittedContactsView.sort(WiPermittedContactsView.COL_NAME); //ascending order

        mPermittedContactsView.display();

        mPermittedContactsView.setOnInviteContactsButtonClickedListener(new WiPermittedContactsView.OnInviteContactsButtonClickedListener() {
            @Override
            public void onInviteContactsButtonClicked() {
                mTabs.getTabAt(1).select();
            }
        });
    }

    private TabLayout.OnTabSelectedListener onTabSelected(){
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) mPermittedContactsView.display();
                if(tab.getPosition() == 1) mInvitableContactsView.display();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }

    public void filter(String searchString) {
        mPermittedContactsView.filter(searchString);
        mInvitableContactsView.filter(searchString);
    }
}


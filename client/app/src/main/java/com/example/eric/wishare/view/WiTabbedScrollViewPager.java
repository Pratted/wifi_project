package com.example.eric.wishare.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

public class WiTabbedScrollViewPager extends ViewPager {

    public WiTabbedScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public WiTabbedScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    private void init(){
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new WiTabbedScrollViewPager.WiScroller(getContext()));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class WiScroller extends Scroller {
        public WiScroller(Context context) {
            super(context, new DecelerateInterpolator());

            init();
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy);
        }
    }
}
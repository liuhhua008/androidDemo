package com.liu008.myapplication.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 重写了canScroll方法，解决ViewPager和地图横向滑动冲突
 */
public class ScrollViewPager extends ViewPager  {


    public ScrollViewPager(Context context) {
        super(context);
    }

    public ScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (Math.abs(dx) > 50) {
            return super.canScroll(v, checkV, dx, x, y);
        } else {
            return true;
        }
    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_UP) {
//            requestDisallowInterceptTouchEvent(false);
//        } else {
//            requestDisallowInterceptTouchEvent(true);//告诉父View不要拦截我的事件
//        }
//
//        return false;
//    }

}

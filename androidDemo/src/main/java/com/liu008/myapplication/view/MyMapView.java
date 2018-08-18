package com.liu008.myapplication.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MyMapView extends FrameLayout {
    //滑动测速工具类
    private VelocityTracker mTracker;


    public MyMapView(Context context) {
        super(context);
    }

    public MyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Log.i("xxxxxxxxxxxxxxxx","11111");
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            Log.i("xxxxxxxxxxxxxxxx","222222");
//            getParent().requestDisallowInterceptTouchEvent(true);//请求父控件不拦截触摸事件
//            Log.i("xxxxxxxxxxxxxxxx","333333");
//        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
//            getParent().requestDisallowInterceptTouchEvent(false);
//        }
//
//        return super.dispatchTouchEvent(ev);
//    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getTracker(ev);
         switch (ev.getAction()){
             case MotionEvent.ACTION_DOWN:
                 if(mTracker==null){
                     mTracker = VelocityTracker.obtain();
                 }else{
                     mTracker.clear();
                 }
                 mTracker.addMovement(ev);
                 //请求父控件不拦截触摸事件,这样才有机会做下一部判断
                 getParent().requestDisallowInterceptTouchEvent(true);
                 break;
                 case MotionEvent.ACTION_MOVE:

                     mTracker.addMovement(ev);
                     mTracker.computeCurrentVelocity(1000);
                     int xSpeed = (int)Math.abs(mTracker.getXVelocity());
                     int ySpeed = (int)Math.abs(mTracker.getYVelocity());


                     //快速滑动
                     if (ySpeed >= 5000 || xSpeed >= 5000){

                         //让父控件去响应
                         getParent().requestDisallowInterceptTouchEvent(false);
                     }else {
                         getParent().requestDisallowInterceptTouchEvent(true);
                     }
                     break;
             case MotionEvent.ACTION_UP:
                 cancelTracker();
                 getParent().requestDisallowInterceptTouchEvent(false);
                 break;
         }
        return super.dispatchTouchEvent(ev);
    }


    private void cancelTracker(){
        if(mTracker!=null){
            mTracker.recycle();
            mTracker = null;
        }
    }


    private void getTracker(MotionEvent event){
        if(mTracker==null){
            mTracker = VelocityTracker.obtain();
            mTracker.addMovement(event);
        }
    }



}




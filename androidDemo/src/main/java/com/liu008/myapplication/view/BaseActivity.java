package com.liu008.myapplication.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;

import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.zhy.autolayout.AutoLayoutActivity;
import com.zhy.autolayout.config.AutoLayoutConifg;

/**
 * Created by 008 on 2018/3/27.
 * 1.可能实显所有子类activity都能有底部浮动栏
 */

public class BaseActivity extends AppCompatActivity  {
    /**
     * windowManager对象
     */
    private WindowManager windowManager;
    /**
     * 根视野
     */
    private FrameLayout mContentContainer;
    /**
     * 浮动视野
     */
    private View mFloatView;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //子类activity全部加入集合，便于退出
        try {
            MyApplication.instance.activityList.add(this);
        } catch (Exception e) {
            // TODO: handle exception
        }
        //AutoLayoutConifg.getInstance().useDeviceSize();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.colorPrimary));

        Log.i("info", "baseactivity_onCreate-执行了:1 ");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //拿到除顶层窗口对象，对像ChildAt（0）为标题栏，1为下面全部内容
        ViewGroup mDecorView= (ViewGroup) getWindow().getDecorView();
        //在子类Activity中去把标记为"folat_tag_frameLayout"的FrameLayout拿出来作为容器
        mContentContainer = (FrameLayout) ((ViewGroup)mDecorView.
                getChildAt(0)).findViewWithTag("folat_tag_frameLayout");
               // findViewById(R.id.folat_framelayout);
        //获得用来装入容器的View对象，在这里你可以指定你自己真正用来显示的布局文件。
        mFloatView= LayoutInflater.from(getBaseContext()).inflate(R.layout.float_pindao_layout,null);
        //设置容量内View的摆放位置
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity= Gravity.BOTTOM;
        mContentContainer.addView(mFloatView, layoutParams);
        Log.i("info", "baseactivity_onPostCreate-执行了: ");
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    /***
     * 重点，设置这个可以实现前进Activity时候的无动画切换
     * @param intent
     */
    @Override
    public void startActivity(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);//设置切换没有动画，用来实现活动之间的无缝切换
        super.startActivity(intent);
    }

    /**
     *  重点，在这里设置按下返回键，或者返回button的时候无动画
     */
    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(0, 0);//设置返回没有动画
    }



}

package com.liu008.myapplication.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.liu008.myapplication.R;

/**
 * Created by 008 on 2018/3/27.
 * 1.可能实显所有子类activity都能有底部浮动栏
 */

public class BaseActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    //------------------以下内容为沉浸式状态栏实际部分----------------------------------------------------------

    public <T extends View> T bindView(int id){
        return (T)findViewById(id);
    }

    public static void setColor(Activity activity,int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View StatusView = createStatusView(activity,color);
            // 添加statusView到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(StatusView);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content))
                    .getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     * @param activity 需要设置的activity
     * @param color 状态栏的颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏的高度
        int resourceId = activity.getResources()
                .getIdentifier("status_bar_height","dimen","android");
        int statusBarHeight = activity.getResources()
                .getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高度的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);

        return statusView;
    }

    /***********************我是分割线***************************/

    /**
     * 使状态栏透明
     * 适用于图片作为背景的界面，此时需要图片填充到状态栏
     * @param activity 需要设置的activity
     */
    public static void setTranslucent(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup)((ViewGroup)activity
                    .findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

}

package com.liu008.myapplication;

import android.app.Application;
import android.content.Context;

import com.mob.MobSDK;

import org.xutils.x;

/**
 * Created by 008 on 2018/3/23.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
        x.Ext.init(this); //初始化xUtils
        MobSDK.init(this);//初始化短信平台
    }

    public static  Context getmContext() {
        return mContext;
    }
}

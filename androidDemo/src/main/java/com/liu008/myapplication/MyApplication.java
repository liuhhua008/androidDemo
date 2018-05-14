package com.liu008.myapplication;

import android.app.Application;

import com.mob.MobSDK;

import org.xutils.x;

/**
 * Created by 008 on 2018/3/23.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this); //初始化xUtils
        MobSDK.init(this);//初始化短信平台

    }
}

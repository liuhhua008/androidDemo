package com.liu008.myapplication;

import android.app.Application;
import android.content.Context;

import com.mob.MobSDK;
import com.zhy.autolayout.config.AutoLayoutConifg;

import org.xutils.x;

/**
 * Created by 008 on 2018/3/23.
 */

public class MyApplication extends Application {
    public static String getAccess_jwt() {
        return access_jwt;
    }

    public static void setAccess_jwt(String access_jwt) {
        MyApplication.access_jwt = access_jwt;
    }

    private static String access_jwt;


    @Override
    public void onCreate() {
        super.onCreate();
        //屏幕适配
        AutoLayoutConifg.getInstance().useDeviceSize();
        x.Ext.init(this); //初始化xUtils
        MobSDK.init(this);//初始化短信平台
        access_jwt=getApplicationContext().getSharedPreferences("userInfo",Context.MODE_PRIVATE).getString("access_token",null);
    }




}

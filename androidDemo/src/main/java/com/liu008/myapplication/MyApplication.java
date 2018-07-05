package com.liu008.myapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Process;

import com.mob.MobSDK;
import com.zhy.autolayout.config.AutoLayoutConifg;

import org.xutils.x;

import java.util.ArrayList;

import io.rong.imkit.RongIM;

/**
 * Created by 008 on 2018/3/23.
 */

public class MyApplication extends Application {
    public static MyApplication instance;
    //存放activity的集合
    public ArrayList<Activity> activityList = new ArrayList<Activity>();

    //内存中保存一份JWT
    private static String access_jwt;
    //获取JWT token值
    public static String getAccess_jwt() {
        return access_jwt;
    }
    //设置jwt值
    public static void setAccess_jwt(String access_jwt) {
        MyApplication.access_jwt = access_jwt;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //屏幕适配
        AutoLayoutConifg.getInstance().useDeviceSize();
        //初始化融云SDK
        RongIM.init(this);
        x.Ext.init(this); //初始化xUtils
        MobSDK.init(this);//初始化短信平台
        //从文件中读取JWT值到内存
        access_jwt=getApplicationContext().getSharedPreferences("userInfo",Context.MODE_PRIVATE).getString("access_token",null);
    }

    public void exit() {
        try {
            // 断开与openfire连接
            //xmppConnection.disconnect();
            finishActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finishActivity() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        // 结束掉进程
        //Process.killProcess(Process.myPid());
    }


}

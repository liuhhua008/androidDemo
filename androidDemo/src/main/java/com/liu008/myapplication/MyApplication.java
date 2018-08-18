package com.liu008.myapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.liu008.myapplication.entity.UserBasicInfo;
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
    private static UserBasicInfo userBasicInfo;
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

    public static UserBasicInfo getUserBasicInfo() {
        return userBasicInfo;
    }

    public static void setUserBasicInfo(UserBasicInfo userBasicInfo) {
        MyApplication.userBasicInfo = userBasicInfo;
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
        access_jwt = getApplicationContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("access_token", null);



        //有用户登录过了，才去获取用户信息
//        if (StringUtils.isNotBlank(access_jwt)){
//            //判断并获取权限
//            if (PermissionUtil.getExternalStoragePermissions(getApplicationContext(),200))
//                    //联网获取用户基础信息
//            UserUtils.getSaveUserBasicInfo(getApplicationContext(), mHandler);
//         }




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

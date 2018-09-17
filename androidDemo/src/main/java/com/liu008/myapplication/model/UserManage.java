package com.liu008.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.entity.UserBasicInfo;
import com.liu008.myapplication.manager.DataCallback;
import com.liu008.myapplication.manager.DataManager;
import com.liu008.myapplication.utils.UserUtils;

import java.io.File;

import io.rong.imkit.utils.StringUtils;
import okhttp3.Call;


/**
 * 保存用户信息的管理类
 * Created by libin
 */

public class UserManage {

    SharedPreferences.Editor editor;
    //全局模式下内存中的userinfo
    private static UserBasicInfo userBasicInfo;

    private static volatile UserManage instance;

    private UserManage() {
    }

    /**
     * 懒汉式双重检测锁
     *
     * @return
     */
    public static UserManage getInstance() {
        if (instance == null) {
            synchronized (UserManage.class) {
                if (instance == null) {
                    instance = new UserManage();
                }
            }
        }
        return instance;
    }


    public UserBasicInfo getUserBasicInfo() {
        return userBasicInfo;
    }

    public void setUserBasicInfo(UserBasicInfo userBasicInfo) {
        this.userBasicInfo = userBasicInfo;
    }

    /**
     * 保存自动登录的用户信息,主要是token信息。
     */

    public void saveUserInfo(Context context, AccessToken accessToken) {
        //Context.MODE_PRIVATE表示SharePrefences的数据只有自己应用程序能访问。
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        editor = sp.edit();
        //更新JWT全局变量
        MyApplication.setAccess_jwt(accessToken.getAccess_token());
        editor.putString("user_id", accessToken.getUser_id());
        editor.putString("access_token", accessToken.getAccess_token());
        editor.putString("refresh_type", accessToken.getRefresh_token());
        editor.putString("token_type", accessToken.getToken_type());
        editor.putLong("expires_in", accessToken.getExpires_in());
        editor.commit();
    }

    /**
     * 删除用户登录信息
     *
     * @param context
     */
    public void deleteUserInfo(Context context) {
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 24) {
            context.deleteSharedPreferences("userInfo");
            //不搞下面这个，在程序没有完全退出死透的情况下删除了也会重生。
            editor.clear();
            editor.commit();
            Toast.makeText(context, "帐号已退出", Toast.LENGTH_LONG).show();
        } else {
            File file = new File("/data/data/" + context.getPackageName().toString() + "/shared_prefs", "userInfo.xml");
            if (file.exists()) {
                file.delete();
                editor.clear();
                editor.commit();
                Toast.makeText(context, "帐号已退出", Toast.LENGTH_LONG).show();
            }
        }
        //更新JWT全局变量
        MyApplication.setAccess_jwt("");
    }

    /**
     * 获取用户信息model
     *
     * @param context
     * @param
     * @param
     */
    public boolean getUserfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
//        UserInfo userInfo=new UserInfo();
//        userInfo.setUserName(sp.getString("USER_NAME",""));
//        userInfo.setPassWord(sp.getString("PASSWORD",""));
        String token = sp.getString("access_token", null);
        if (token == null || "".equals(token)) {
            //如果没有本地token返回false，跳转登录页
            return false;
        } else {
            return true; //准备去主页
        }

    }


    public String getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userID = sp.getString("user_id", null);
        return userID;
    }

    /**
     * 获取详细个人信息
     *
     * @param callback
     */
    public UserBasicInfo getCurrentUserinfo(Context context, DataCallback callback) {
        //1.先从内存里拿
        if (this.userBasicInfo != null) {
            callback.onSucess(this.userBasicInfo);
            return this.userBasicInfo;
        } else if (UserUtils.getBaseUserInfoForSP(context) != null) {
            //2.从本地文件里拿
            callback.onSucess(UserUtils.getBaseUserInfoForSP(context));
            return UserUtils.getBaseUserInfoForSP(context);
        } else {
            //3.走一次网络获取流程
            UserUtils.getSaveUserBasicInfo(context, callback);
        }
        return null;
    }

    public UserBasicInfo getCurrentUserinfo(){
        return getCurrentUserinfo(MyApplication.instance);
    }

    public UserBasicInfo getCurrentUserinfo(Context context) {
        //1.先从内存里拿
        if (this.userBasicInfo != null) {
            return this.userBasicInfo;
        } else if (UserUtils.getBaseUserInfoForSP(context) != null) {
            //2.从本地文件里拿
            return UserUtils.getBaseUserInfoForSP(context);
        } else {
            //3.走一次网络获取流程
            UserUtils.getSaveUserBasicInfo(context, new DataCallback() {
                @Override
                public void onSucess(Object object) {

                }

                @Override
                public void failed() {

                }
            });
        }
        return null;
    }

    public void saveBsuserInfo2sp(Context context, UserBasicInfo userInfo) {
        synchronized (this.userBasicInfo) {
            SharedPreferences sp = context.getSharedPreferences("userBasicInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("userId", userInfo.getUserId());
            editor.putString("userName", userInfo.getUserName());
            editor.putString("nickName", userInfo.getNickName());
            editor.putString("headImageUrl", userInfo.getHeadImageUrl());
            editor.putString("rongToken", userInfo.getRongToken());
            editor.commit();
        }

    }
}

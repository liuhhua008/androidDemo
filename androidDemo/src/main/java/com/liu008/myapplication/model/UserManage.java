package com.liu008.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * 保存用户信息的管理类
 * Created by libin
 */

public class UserManage {
    private static UserManage instance;
    private UserManage(){}

    public static UserManage getInstance(){
        if (instance==null){
            instance = new UserManage();
        }
        return instance;
    }
    /**
     * 保存自动登录的用户信息,不安全的做法有待改进token机制
     */
    public void saveUserInfo(Context context, String username, String password){
        //Context.MODE_PRIVATE表示SharePrefences的数据只有自己应用程序能访问。
        SharedPreferences sp=context.getSharedPreferences("userInfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("USER_NAME",username);
        editor.putString("PASSWORD",password);
        editor.commit();
    }

    /**
     * 获取用户信息model
     *
     * @param context
     * @param
     * @param
     */
    public UserInfo getUserfo(Context context){
        SharedPreferences sp=context.getSharedPreferences("userInfo",Context.MODE_PRIVATE);
        UserInfo userInfo=new UserInfo();
        userInfo.setUserName(sp.getString("USER_NAME",""));
        userInfo.setPassWord(sp.getString("PASSWORD",""));
        return userInfo;
    }
    /**
     * userInfo中是否有数据
     */
    public boolean hasUserInfo(Context context){
        UserInfo userInfo=getUserfo(context);
        if (userInfo!=null){
            if (!(TextUtils.isEmpty(userInfo.getUserName()))&&!(TextUtils.isEmpty(userInfo.getPassWord()))){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
}

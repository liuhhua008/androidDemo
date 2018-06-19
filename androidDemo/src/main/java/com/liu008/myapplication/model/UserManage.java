package com.liu008.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.liu008.myapplication.MyApplication;

import java.io.File;

/**
 * 保存用户信息的管理类
 * Created by libin
 */

public class UserManage  {
    SharedPreferences.Editor editor;
    private static UserManage instance;
    private UserManage(){}

    public static UserManage getInstance(){
        if (instance==null){
            instance = new UserManage();
        }
        return instance;
    }
    /**
     * 保存自动登录的用户信息,主要是token信息。
     */
    public void saveUserInfo(Context context, AccessToken accessToken){
        //Context.MODE_PRIVATE表示SharePrefences的数据只有自己应用程序能访问。
        SharedPreferences sp=context.getSharedPreferences("userInfo",Context.MODE_PRIVATE);

        editor=sp.edit();
        //更新JWT全局变量
        MyApplication.setAccess_jwt(accessToken.getAccess_token());
        editor.putString("access_token",accessToken.getAccess_token());
        editor.putString("refresh_type",accessToken.getRefresh_token());
        editor.putString("token_type",accessToken.getToken_type());
        editor.putLong("expires_in",accessToken.getExpires_in());
        editor.commit();
    }

    /**
     * 删除用户登录信息
     * @param context
     */
    public void deleteUserInfo(Context context){
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >=24) {
            context.deleteSharedPreferences("userInfo");
            //不搞下面这个，在程序没有完全退出死透的情况下删除了也会重生。
            editor.clear();
            editor.commit();
            Toast.makeText(context, "帐号已退出", Toast.LENGTH_LONG).show();
        }else{
            File file= new File("/data/data/"+context.getPackageName().toString()+"/shared_prefs","userInfo.xml");
            if(file.exists())
            {
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
    public boolean getUserfo(Context context){
        SharedPreferences sp=context.getSharedPreferences("userInfo",Context.MODE_PRIVATE);
//        UserInfo userInfo=new UserInfo();
//        userInfo.setUserName(sp.getString("USER_NAME",""));
//        userInfo.setPassWord(sp.getString("PASSWORD",""));
        String token=sp.getString("access_token",null);
        if (token==null){
            //如果没有本地token返回false，跳转登录页
            return false;
        }else{
            return true; //准备去主页
        }

    }
    /**
     * userInfo中是否有数据
     */
//    public boolean hasUserInfo(Context context){
//        UserInfo userInfo=getUserfo(context);
//        if (userInfo!=null){
//            if (!(TextUtils.isEmpty(userInfo.getUserName()))&&!(TextUtils.isEmpty(userInfo.getPassWord()))){
//                return true;
//            }else {
//                return false;
//            }
//        }
//        return false;
//    }
}

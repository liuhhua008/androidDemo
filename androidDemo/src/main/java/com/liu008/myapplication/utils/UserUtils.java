package com.liu008.myapplication.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.entity.IMuserInfo;
import com.liu008.myapplication.entity.UserBasicInfo;
import com.liu008.myapplication.http.HttpResponseCallBack;
import com.liu008.myapplication.manager.DataCallback;
import com.liu008.myapplication.model.UserManage;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;


import io.rong.imlib.model.UserInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 008 on 2018/5/13.
 */

public class UserUtils {
        /**
     * 获取用户信息用于外部调用
     * @return
     */
    public static UserBasicInfo getUserInfo(){
        UserBasicInfo userBasicInfo = UserManage.getInstance().getCurrentUserinfo(MyApplication.instance);
        return userBasicInfo;
    }
    final public static int UPDATE_USERINFO_NICKNAME = 1;

    /**
     * 字串MD5加密
     *
     * @param inStr
     * @return
     */
    public static String getMD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }

    /**
     * post请求后台注册和用户登录
     */
    public static void postRequest(Object requestBody, final Handler mHandler, String url) {
        //创建一个OkHttpClient对象
        final OkHttpClient okHttpClient = new OkHttpClient();
        //把传入的对象转成JSON字串
        String json = JSON.toJSONString(requestBody);
        //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
        RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        //发起请求
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        //将服务器响应的参数response.body().string())发送到hanlder中，并更新ui
                        mHandler.obtainMessage(1, response.body().string()).sendToTarget();

                    } else {
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(2);
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 获取用户基础信息
     *
     * @param context
     * @return
     */
    public static void getUserBasicInfo(final Context context, final Handler mHandler) {
        UserBasicInfo userBasicInfo = new UserBasicInfo();
        //1.定义一个OkhttpClient
        OkHttpClient client = ApiHttpClient.getInstance(context);
        String url = MyConstant.APPSERVER_URL + "user/getUserBasicInfo/" + UserManage.getInstance().getUserId(context);
        Request request = new Request.Builder().url(url).build();
        //定义一个call，利用okhttpclient的newcall方法来创建对象。因为Call是一个接口不能利用构造器实例化。
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //把从服务端返回的信息通过此方法中携带过来的handler把消息和数据传回ui线程
                mHandler.obtainMessage(200, response.body().string()).sendToTarget();
            }
        });
    }

    /**
     * 获取并保存用户基础信息
     *
     * @param context
     * @param callback 回调接口
     */
    public static void getSaveUserBasicInfo(final Context context, final DataCallback callback) {

        Log.d("getSaveUserBasicInfo", "getSaveUserBasicInfo被执行了");
        final OkHttpClient client = ApiHttpClient.getInstance(context);
        String url = MyConstant.APPSERVER_URL + "user/getUserBasicInfo/" + UserManage.getInstance().getUserId(context);
        final Request request = new Request.Builder().url(url).build();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    final String responseText = response.body().string().toString();
                    if (response.code() == 200) {
                        UserBasicInfo userBasicInfo = null;
                        userBasicInfo = JSON.parseObject(responseText, UserBasicInfo.class);
                        //在内存中存储一份用户基础信息
                        UserManage.getInstance().setUserBasicInfo(userBasicInfo);
                        //在本地文件中也存储一份
                        UserManage.getInstance().saveBsuserInfo2sp(context, userBasicInfo);
                        //回调通知成功
                        callback.onSucess(userBasicInfo);
                    }
                } catch (Exception e) {
                    callback.failed();
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 从本地文件获取用户基础信息
     *
     * @param context
     * @return
     */
    public static UserBasicInfo getBaseUserInfoForSP(Context context) {
        UserBasicInfo userBasicInfo = new UserBasicInfo();
        try {
            SharedPreferences sp = context.getSharedPreferences("userBasicInfo", Context.MODE_PRIVATE);
            userBasicInfo.setUserId(sp.getString("userId", null));
            userBasicInfo.setUserName(sp.getString("userName", null));
            userBasicInfo.setNickName(sp.getString("nickName", null));
            userBasicInfo.setHeadImageUrl(sp.getString("headImageUrl", null));
            userBasicInfo.setRongToken(sp.getString("rongToken", null));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //既然拿到了就把内存里也放一份
        UserManage.getInstance().setUserBasicInfo(userBasicInfo);
        return userBasicInfo;
    }

    /**
     * 根据递交过来的USERID给融云SDK提供用户基础信息
     *
     * @param userid
     * @param callback
     * @return 由于是异步执行，所以返回NULL。信息将会在回调方法中进行处理
     */
    public static UserInfo getImUserInfo(String userid, final Callback callback) {
        //Uri.parse( MyApplication.getUserBasicInfo().getHeadImageUrl());
        String url = MyConstant.APPSERVER_URL + "user/getImuserInfo";
        Map paras = new HashMap();
        paras.put("userid", userid);
        httpPostByjwt(url, paras, callback);
        return null;
    }

    public static IMuserInfo getContactUserInfo(String userid, final Callback callback) {
        //Uri.parse( MyApplication.getUserBasicInfo().getHeadImageUrl());
        String url = MyConstant.APPSERVER_URL + "user/getImuserInfo";
        Map paras = new HashMap();
        paras.put("userid", userid);
        httpPostByjwt(url, paras, callback);
        return null;
    }

    //生成随机用户名昵称，数字和字母组成,
    public static String getStringRandom(int length, String phone) {
        String p = phone.substring(phone.length() - 3, phone.length());
        String val = "";
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (random.nextInt(26) + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return "路人" + p + val;
    }

    /**
     * 更新除头像图片外的个人信息
     *
     * @param context
     * @param mHandler
     * @param requestBody
     */
    public static void upDateUserinfo(final Activity context, final Handler mHandler, final Object requestBody) {
        //把传入的对象转成JSON字串
        String json = JSON.toJSONString(requestBody);
        final OkHttpClient client = ApiHttpClient.getInstance(context);
        String url = MyConstant.APPSERVER_URL + "user/updateUserinfo";
        //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
        RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        //创建请求
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        //开启线程
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.code() == 200 && response.body().string().equals("ok")) {//成功返回
                        mHandler.sendEmptyMessage(1);//通知UI成功
                    } else {
                        mHandler.sendEmptyMessage(0);//通知UI失败
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mHandler.sendEmptyMessage(0);//通知UI失败
                }

            }
        });
    }

    /**
     * 通用的带JWT认证信息的HTTP请求
     *
     * @param url      访问地址
     * @param params   请求的数据
     * @param callback 回调接口方法
     */
    public static void httpPostByjwt(String url, Map<String, String> params, final Callback callback) {
        final OkHttpClient client = ApiHttpClient.getInstance(MyApplication.instance);
        FormBody.Builder builder = new FormBody.Builder();
        //添加参数
        if (params != null && params.keySet().size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }

        final Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

}

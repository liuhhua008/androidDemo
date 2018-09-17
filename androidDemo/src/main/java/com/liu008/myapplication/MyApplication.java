package com.liu008.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.liu008.myapplication.entity.UserBasicInfo;
import com.liu008.myapplication.listener.IMonReceiveMessageListener;
import com.liu008.myapplication.manager.DataCallback;
import com.liu008.myapplication.manager.DataManager;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.NToast;
import com.liu008.myapplication.utils.PermissionUtil;
import com.liu008.myapplication.utils.UserUtils;
import com.mob.MobSDK;
import com.zhy.autolayout.config.AutoLayoutConifg;

import org.apache.commons.lang3.StringUtils;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 008 on 2018/3/23.
 */

public class MyApplication extends Application {
    public static MyApplication instance;
    //存放activity的集合
    public ArrayList<Activity> activityList = new ArrayList<Activity>();

    //内存中保存一份JWT
    private static String access_jwt;



    static Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                Toast.makeText(instance, "联网失败", Toast.LENGTH_SHORT).show();
//                //从上次本地保存的文件中取用户信息到内存
//                setUserBasicInfo(UserUtils.getBaseUserInfoForSP(instance));
//                connect(getUserBasicInfo().getRongToken());
            } else if (msg.what == 200) {
               //联网成功拿到用户信息息
                if (UserManage.getInstance().getUserBasicInfo()!= null) {
                    //如果融云容云TOKEN为空
                    if ("error".equals(UserManage.getInstance().getUserBasicInfo().getRongToken()) || UserManage.getInstance().getUserBasicInfo().getRongToken() == null) {
                        Log.d("MyApplication", "获取融云token失败");
                    } else {
                        //连接融云IM服务器
                        connect(UserManage.getInstance().getUserBasicInfo().getRongToken());
                    }
                }
            }
        }
    };

    public static Handler getHandler() {
        return handler;
    }


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
        DataManager.instance();

        //从文件中读取JWT值到内存
        if (getApplicationContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("access_token", null)!=null){
            access_jwt = getApplicationContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("access_token", null);
        }
        //有用户登录过了，才去获取用户信息
        if (StringUtils.isNotBlank(MyApplication.getAccess_jwt())){
           //联网获取用户基础信息
            UserManage.getInstance().getCurrentUserinfo(this, new DataCallback() {
                @Override
                public void onSucess(Object object) {
                    handler.sendEmptyMessage(200);
                }
                @Override
                public void failed() {
                    Log.i("MyApplication","未能获取登录用户基础信息");
                }
            });
//           UserUtils.getSaveUserBasicInfo(getApplicationContext(), handler);

    }

        // 设置用户信息的提供者，供 RongIM 调用获取用户名称和头像信息。
        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
            //2.自己实现此方法来完成用户信息的提取工作
            @Override
            public UserInfo getUserInfo(String userId) {
                return UserUtils.getImUserInfo(userId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //3.获取失败
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NToast.longToast(getApplicationContext(),"融云联系人信息获取失败");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        //5.拿到用户信息回调融云，更新用户信息
                        final String s = response.body().string();
                        if (s.isEmpty()){
                            onFailure(call,new IOException());
                            return;
                        }
                        //4.获取成功
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Map maps = (Map) JSON.parse(s);
                                UserInfo userInfo= new UserInfo(
                                        (String)maps.get("userId"),
                                        (String) maps.get("nickName"),
                                        Uri.parse((String) maps.get("headImageUrl"))
                                );
                                RongIM.getInstance().refreshUserInfoCache(userInfo);
                            }
                        });
                    }
                });
            }
        }, true);//这里为true代表信息缓存到本地
        //注册融云消息监听
        RongIM.setOnReceiveMessageListener(new IMonReceiveMessageListener());
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

    /**
     * 连接服务器，在整个应用程序全局，只需要调用一次，需在 {@link //#init(Context)} 之后调用。</p>
     * 如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
     * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。</p>
     *
     * @param token 从服务端获取的用户身份令牌（Token）。
     *              //@param callback 连接回调。
     * @return RongIM  客户端核心类的实例。
     */
    private static void connect(String token) {

        if (instance.getApplicationInfo().packageName.equals(getCurProcessName(instance))) {

            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                /**
                 * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                 *                  2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                 */
                @Override
                public void onTokenIncorrect() {
                    Log.d("MyApplication", "--客户端成功连接失败" );
                }

                /**
                 * 连接融云成功
                 * @param userid 当前 token 对应的用户 id
                 */
                @Override
                public void onSuccess(String userid) {
                    Log.d("MyApplication", "--客户端成功连接融云IM" + userid);

                }

                /**
                 * 连接融云失败
                 * @param errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Log.d("MyApplication", "--客户端连接融云IM失败:" + errorCode);
                }
            });
        }
    }

    /**
     * 获得当前进程的名字
     *
     * @param context
     * @return 进程号
     */
    static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


}

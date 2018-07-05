package com.liu008.myapplication.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.R;
import com.liu008.myapplication.receiver.ShowNotificationReceiver;
import com.liu008.myapplication.utils.ResidentNotificationHelper;

/**
 * PushService的工作很简单，启动后发一个广播在通知栏显示通知，然后常驻在后台
 */
public class PushService extends Service {
    private static final String TAG =PushService.class.getSimpleName() ;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //把服务启动成前台服务，并弹出自定义通知栏
        System.out.println("PushService 执行了Oncreate");
        startForeground(110,
                ResidentNotificationHelper.sendResidentNoticeType0(this, "Test", "008号频道正在....", R.mipmap.ic_discovery_gray) );
    }

    /**
     * 服务已经启动过了，再通过startService进入该服务，将不执行onCreat方式直接执行
     * 这个方法。主要是用来给service发送命令，执行service中的方法
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("PushService 执行了onStartCommand");
        String command = intent.getStringExtra("command");
        if ("closeService".equals(command)) {
            onDestroy();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }
}



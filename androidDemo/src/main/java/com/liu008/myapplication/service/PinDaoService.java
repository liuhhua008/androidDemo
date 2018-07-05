package com.liu008.myapplication.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.R;
import com.liu008.myapplication.view.PingDaoActivity;

public class PinDaoService extends Service {
    public static final String NOTICE_ID_KEY = "NOTICE_ID";
    public static final String ACTION_CLOSE_NOTICE = "cn.campusapp.action.closenotice";
    public static final int NOTICE_ID_TYPE_0 = R.string.app_name;
    private static final String TAG =PinDaoService.class.getSimpleName() ;

    @Override
    public void onCreate() {

      // Intent notifyIntent = new Intent(this, PingDaoActivity.class);
       // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent,0);
//
//        Notification notification = new Notification.Builder(this).
//                setSmallIcon(R.mipmap.ic_launcher).
//                setContentTitle("通知").
//                setContentText("我是服务的通知").
//                setContentIntent(pendingIntent).
//                setDefaults(Notification.DEFAULT_ALL). // 设置用手机默认的震动或声音来提示
//                build();

        // 设置为前台服务,在系统状态栏显示// 参数一：唯一的通知标识；参数二：通知消息。
       // startForeground(1, notification);

        //自定义通知栏布局
       // RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
               // R.layout.notification_layout);// 获取remoteViews（参数一：包名；参数二：布局资源）
//        Notification notification = new Notification.Builder(this)
//                //.setContentIntent(pendingIntent)
//                .setContent(remoteViews)// 设置自定义的Notification内容
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .build();
//
//        //Notification notification = builder.getNotification();// 获取构建好的通知--.build()最低要求在
//        // API16及以上版本上使用，低版本上可以使用.getNotification()。
//        notification.defaults = Notification.DEFAULT_SOUND;//设置为默认的声音
//        startForeground(110, notification);// 开始前台服务
//       NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setClass(this,PingDaoActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        // 点击跳转到主界面
//        PendingIntent intent_go = PendingIntent.getActivity(this, 5, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.tv_notification, intent_go);
//        startForeground(110, builder.build());
//        RemoteViews remoteViews =new RemoteViews(this.getPackageName(),R.layout.notification_layout);
//        remoteViews.setTextViewText(R.id.tv_notification, "这里显示频道信息");
//        remoteViews.setImageViewResource(R.id.iv_notification,R.mipmap.ic_delete);
//        Intent intent = new Intent(this, PingDaoActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        int requestCode = (int) SystemClock.uptimeMillis();
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.iv_notification, pendingIntent);
        // 点击跳转到频道页
       // remoteViews.setOnClickPendingIntent(R.id.tv_notification, getClickPendingIntent(1));
        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //-------------点击关闭-------
//        int requestCode1 = (int) SystemClock.uptimeMillis();
//        Intent intent1 = new Intent(ACTION_CLOSE_NOTICE);
//        intent1.putExtra(NOTICE_ID_KEY, NOTICE_ID_TYPE_0);
//        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, requestCode1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.close_iv, pendingIntent1);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        Notification notification = builder.build();


//        if(android.os.Build.VERSION.SDK_INT >= 16) {
//            notification = builder.build();
//            notification.bigContentView = remoteViews;
//        }
//        notification.contentView = remoteViews;
//        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(NOTICE_ID_TYPE_0, notification);
        //notificationManager.notify(110,notification);
        //startForeground(110, notification);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand()");
//        Notification.Builder builder=new Notification.Builder(this.getApplicationContext());
//        Intent nfIntent = new Intent(this, MainActivity.class);
//        builder.setContentIntent(PendingIntent.getActivity(this,0,nfIntent,0))
//                //设置下拉列表中的图标（大图标）
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_discovery_gray))
//                .setContentTitle("下拉列表中的Title")
//                .setSmallIcon(R.mipmap.ic_more_gray)
//                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                .setPriority(Notification.PRIORITY_HIGH)//高优先级用于重要的通信内容，例如短消息或者聊天，这些都是对用户来说比较有兴趣的。
//                .setContentText("要显示的内容")
//                .setWhen(System.currentTimeMillis());//设置该通知的时间
//        Notification notification=builder.build();
//        notification.defaults=Notification.DEFAULT_SOUND;//设置为默认的声音

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 创建RemoteViews,3.0之后版本使用
     *
     * @return
     */
//    public RemoteViews getRemoteView() {
////        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);
////        remoteViews.setTextViewText(R.id.tv_notification, "这里显示频道信息");
////        remoteViews.setImageViewResource(R.id.iv_notification,R.mipmap.ic_delete);
////        // 点击跳转到频道页
////        remoteViews.setOnClickPendingIntent(R.id.tv_notification, getClickPendingIntent(1));
////        return remoteViews;
//    }
    /**
     * 获取点击自定义通知栏上面的按钮或者布局时的延迟意图
     *
     * @param what 要执行的指令
     * @return
     */
    public PendingIntent getClickPendingIntent(int what) {
        Intent intent = new Intent(this, PingDaoActivity.class);
        intent.putExtra("cmd", what);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent clickIntent = PendingIntent.getActivity(this, what, intent, flag );
        return clickIntent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }
}

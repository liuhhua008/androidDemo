package com.liu008.myapplication.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.R;
import com.liu008.myapplication.view.PingDaoActivity;

/**
 * Created by kris on 16/4/14.
 * 常驻通知帮助类
 */
public class ResidentNotificationHelper {
    public static final String NOTICE_ID_KEY = "NOTICE_ID";
    public static final String ACTION_NOTIFICATION_NOTICE = "com.liu008.action.notice";
    public static final String ACTION_NOTIFICATION_CLOSE_NOTICE = "com.liu008.action.closenotice";
    public static final int NOTICE_ID_TYPE_0 = R.string.app_name;//打开activity
    public static final int NOTICE_ID_TYPE_1 = R.string.app_name+110;//关闭服务和activity

    /**
     * 生成频道栏服务时，使用此方式生成系统通知栏
     * @param context
     * @param title
     * @param content
     * @param res
     * @return
     */
    public static Notification sendResidentNoticeType0(Context context, String title, String content, @DrawableRes int res){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        //加载自定义布局通知栏
        RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.notification_layout);
        //设置通知栏最左边的图片
        remoteViews.setImageViewResource(R.id.iv_notification_bt,R.mipmap.ic_discovery_gray);
        //设置通知栏中间的文字为传入的字串
        remoteViews.setTextViewText(R.id.tv_notification,content);
        //设置右边的删除图片
        remoteViews.setImageViewResource(R.id.iv_notification_close,R.mipmap.ic_delete);
        //为通知栏中间的文字设置点击事件监听，这里点击发送广播
        Intent intent = new Intent(ACTION_NOTIFICATION_NOTICE);
        intent.putExtra(NOTICE_ID_KEY, NOTICE_ID_TYPE_0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int requestCode = (int) SystemClock.uptimeMillis();
        //设置成广播待激发
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.tv_notification, pendingIntent);

        Intent intent1 = new Intent(ACTION_NOTIFICATION_NOTICE);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra(NOTICE_ID_KEY,NOTICE_ID_TYPE_1);
        int requestCode1 = (int) SystemClock.uptimeMillis();
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, requestCode1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_notification_close, pendingIntent1);

        //设置来通知时的显示图标
        builder.setSmallIcon(res);


        Notification notification = builder.build();


        if(android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
            notification.bigContentView = remoteViews;
        }
        notification.contentView = remoteViews;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.notify(NOTICE_ID_TYPE_0, notification);
        return notification;
    }

}

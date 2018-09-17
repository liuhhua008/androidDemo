package com.liu008.myapplication.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import io.rong.imkit.RongContext;
import io.rong.imlib.model.Conversation;

public class SystemUtil {
    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                Log.i("NotificationLaunch",
                        String.format("the %s is running, isAppAlive return true", packageName));
                return true;
            }
        }
        Log.i("NotificationLaunch",
                String.format("the %s is not running, isAppAlive return false", packageName));
        return false;
    }

    /**
     * 通过隐式意图打开频道栏中的聊天界面
     * @param context
     * @param targetUserId
     * @param title
     */
    public static void startPingDaoChat(Context context, String targetUserId, String title) {
        if (context != null && !TextUtils.isEmpty(targetUserId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("pingdao").appendPath(Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetUserId).appendQueryParameter("title", title).build();
                context.startActivity(new Intent("android.intent.action.VIEW", uri));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}

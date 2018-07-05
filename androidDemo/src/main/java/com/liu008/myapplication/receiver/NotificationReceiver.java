package com.liu008.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.service.PushService;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.ResidentNotificationHelper;
import com.liu008.myapplication.utils.SystemUtil;
import com.liu008.myapplication.view.PingDaoActivity;

import java.lang.reflect.Method;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getIntExtra(ResidentNotificationHelper.NOTICE_ID_KEY, 0)) {
            case ResidentNotificationHelper.NOTICE_ID_TYPE_0:
                collapseStatusBar(context);
                //判断app进程是否存活
                if (SystemUtil.isAppAlive(context, "com.liu008.myapplication")) {
                    //如果存活的话，就直接启动DetailActivity，但要考虑一种情况，就是app的进程虽然仍然在
                    //但Task栈已经空了，比如用户点击Back键退出应用，但进程还没有被系统回收，如果直接启动
                    //DetailActivity,再按Back键就不会返回MainActivity了。所以在启动
                    //DetailActivity前，要先启动MainActivity。
                    Log.i("NotificationReceiver", "the app process is alive");
                    Intent mainIntent = new Intent(context, MainActivity.class);
                    //将MainAtivity的launchMode设置成SingleTask, 或者在下面flag中加上Intent.FLAG_CLEAR_TOP,
                    //如果Task栈中有MainActivity的实例，就会把它移到栈顶，把在它之上的Activity都清理出栈，
                    //如果Task栈不存在MainActivity实例，则在栈顶创建
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Intent detailIntent = new Intent(context, PingDaoActivity.class);
                    //加点实验数据
                    detailIntent.putExtra("testInfo", "这是app进程存在，直接启动Activity的");

                    Intent[] intents = {mainIntent, detailIntent};

                    context.startActivities(intents);
                } else {
                    //如果app进程已经被杀死，先重新启动app，将DetailActivity的启动参数传入Intent中，参数经过
                    //SplashActivity传入MainActivity，此时app的初始化已经完成，在MainActivity中就可以根据传入             //参数跳转到DetailActivity中去了
                    Log.i("NotificationReceiver", "the app process is dead");
                    Intent launchIntent = context.getPackageManager().
                            getLaunchIntentForPackage("com.liu008.myapplication");
                    launchIntent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    Bundle args = new Bundle();
                    args.putString("testInfo", " 这是app进程不存在，先启动应用再启动Activity的");
                    launchIntent.putExtra(MyConstant.EXTRA_BUNDLE, args);
                    context.startActivity(launchIntent);
                }
                break;
            case ResidentNotificationHelper.NOTICE_ID_TYPE_1:
                //给service发送命令，执行关闭服务
                Intent intent1 = new Intent(context, PushService.class);
                intent1.putExtra("command", "closeService");
                context.startService(intent1);
                break;
        }

    }

    /**
     * 收起通知栏
     *
     * @param context
     */
    public  void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;

            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }

    }
}


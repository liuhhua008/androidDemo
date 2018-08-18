package com.liu008.myapplication.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * 判断当前网络是否真的可上网
 */
public class NetUtils {
    /**
     * 只判断网络是否打开
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * 网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean checkOnlineState(Context context) {
        if (!isNetworkConnected(context)) {
            return false;
        } else {
            //判断是否为6.0以上版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                            .getSystemService(CONNECTIVITY_SERVICE);
                    NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(mConnectivityManager.getActiveNetwork());
                    Log.i("Avalible", "NetworkCapalbilities:" + networkCapabilities.toString());
                    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

            } else {
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process p = runtime.exec("ping -c 3 www.baidu.com");
                    int ret = p.waitFor();
                    Log.i("Avalible", "Process:" + ret);
                    return ret == 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
    }
}

package com.liu008.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.UserUtils;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {



    private String getConnectionType(int type){
        String connType="";
        if (type==ConnectivityManager.TYPE_MOBILE){
            connType="移动网络";
        }else if(type==ConnectivityManager.TYPE_WIFI){
            connType="WIFI网络";
        }
        return connType;
    }

    /**
     * 收到网络状态变化信息
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
            int wifiState =intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
            Log.e("TAG", "wifiState:" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    //WIFI关闭了
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    //关闭中
                    break;
            }
        }
        // 监听wifi的连接状态即是否连上了一个有效无线路由
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                // 获取联网状态的NetWorkInfo对象
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                //获取的State对象则代表着连接成功与否等状态
                NetworkInfo.State state = networkInfo.getState();
                //判断网络是否已经连接
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                Log.e("TAG", "isConnected:" + isConnected);
                if (isConnected) {

                } else {

                }
            }
        }
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent
                    .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI
                            || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.i("TAG", getConnectionType(info.getType()) + "连上");
                        //执行获取用户基础信息
                        UserManage.getInstance().getCurrentUserinfo();
                        //UserUtils.getSaveUserBasicInfo(context, MyApplication.getHandler());
                    }
                } else {
                    Log.i("TAG", getConnectionType(info.getType()) + "断开");
                }
            }
        }
    }
}

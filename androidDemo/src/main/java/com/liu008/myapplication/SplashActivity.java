package com.liu008.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.view.LoginActivity;
import com.liu008.myapplication.view.RegisterActivity;

/**
 * 启动页，app刚打开时的activity
 * create by linbin
 */
public class SplashActivity extends AppCompatActivity {
    private static final int GO_HOME=0;//去主页
    private static final int GO_LOGIN = 1;//去登录页
    private static final int GO_REGISTER = 2;//去注册页
    /**
     * 跳转判断
     */
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GO_HOME://去主页
                    Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case GO_LOGIN://去登录页
                    Intent intent1=new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent1);
                    finish();
                    break;
                case GO_REGISTER://去注册页
                    Intent intent2=new Intent(SplashActivity.this, RegisterActivity.class);
                    startActivity(intent2);
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
        //mHandler.sendEmptyMessageDelayed(GO_REGISTER, 2000);

        if (UserManage.getInstance().hasUserInfo(this))//自动登录判断，SharePrefences中有数据，则跳转到主页，没数据则跳转到登录页
        {
            mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
        } else {
            mHandler.sendEmptyMessageAtTime(GO_LOGIN, 2000);
        }
    }
}

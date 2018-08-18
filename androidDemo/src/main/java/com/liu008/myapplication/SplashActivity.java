package com.liu008.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.activity.LoginActivity;
import com.liu008.myapplication.activity.RegisterActivity;

/**
 * 启动页，app刚打开时的activity
 * create by linbin
 */
public class SplashActivity extends AppCompatActivity {
    private static final int GO_HOME=0;//去主页
    private static final int GO_LOGIN = 1;//去登录页
    private static final int GO_REGISTER = 2;//去注册页
    private static final int GO_HOME2 = 3;//去主页，同时带点参数过去
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
                case GO_HOME2://去主页,带参数
                    Intent intent3=new Intent(SplashActivity.this,MainActivity.class);
                    intent3.putExtra(MyConstant.EXTRA_BUNDLE,getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE));
                    startActivity(intent3);
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
        //先判断是否是刚点的返回桌面后再进的
        if ((getIntent().getFlags()&Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)!=0){
            finish();
            return;
            //再判断一下是不是APP已经被KILL了用户从点击通知栏进入的
        }else if (getIntent().getBundleExtra(MyConstant.EXTRA_BUNDLE)!=null){
            mHandler.sendEmptyMessageDelayed(GO_HOME2, 2000);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //无title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏
        setContentView(R.layout.activity_splash);
        //setContentView(R.layout.activity_splash);
        //mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
        //mHandler.sendEmptyMessageDelayed(GO_REGISTER, 2000);

        if (UserManage.getInstance().getUserfo(this))//自动登录判断，SharePrefences中有数据，则跳转到主页，没数据则跳转到登录页
        {
            mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
        } else {
            mHandler.sendEmptyMessageAtTime(GO_LOGIN, 2000);
        }
    }
}

package com.liu008.myapplication.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.R;
import com.liu008.myapplication.model.UserInfo;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.MyUtils;
import com.liu008.myapplication.utils.ResultMsg;
import com.liu008.myapplication.utils.ResultStatusCode;

import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;



public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    String APPKEY="24dc6a3dc0e00";
    String APPSECRETE="548b4951254e46d6038aefe50e61ee71";

    // 手机号输入框
    private EditText inputPhoneEt;

    // 密码输入框
    private EditText inputPasswordEt;

    // 验证码输入框
    private EditText inputCodeEt;

    // 获取验证码按钮
    private Button requestCodeBtn;

    // 注册按钮
    private Button commitBtn;
    //
    int i = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        inputPhoneEt = (EditText) findViewById(R.id.etPhone);
        inputPasswordEt = (EditText) findViewById(R.id.etPwd);
        inputCodeEt = (EditText) findViewById(R.id.etVerifyCode);
        requestCodeBtn = (Button) findViewById(R.id.btnSendCode);
        commitBtn = (Button) findViewById(R.id.btnRegister);
        requestCodeBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        // 启动短信验证sdk

        EventHandler eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                //这里是将短信平台的状态发送给自定义的handler去处理
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View v) {
        String phoneNums = inputPhoneEt.getText().toString();
        switch (v.getId()) {
            //按下获取验证码
            case R.id.btnSendCode:
                // 1. 通过规则判断手机号
                if (!judgePhoneNums(phoneNums)) {
                    return;
                } // 2. 通过sdk发送短信验证
                SMSSDK.getVerificationCode("86", phoneNums);

                // 3. 把按钮变成不可点击，并且显示倒计时（正在获取）
                requestCodeBtn.setClickable(false);
                requestCodeBtn.setText("重新发送(" + i + ")");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-9);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-8);
                    }
                }).start();
                break;
            //按下注册按按钮
            case R.id.btnRegister:
                //将收到的验证码和手机号提交再次核对,就是将手机号和验证码发给短信平台进行验证，
                // 验证结果会触发消息事件，反应到handler不带what标记的事件，并且还带了一个消息对象。
                SMSSDK.submitVerificationCode("86", phoneNums, inputCodeEt
                        .getText().toString());
                //createProgressBar();
                break;
            case R.id.ivSeePwd:
                if (inputPasswordEt.getTransformationMethod() == HideReturnsTransformationMethod.getInstance()) {
                    inputPasswordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    inputPasswordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                inputPasswordEt.setSelection(inputPasswordEt.getText().toString().trim().length());
                break;
        }
    }

    /**
     *
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -9) {//发送短信中，正在倒计时
                requestCodeBtn.setText("重新发送(" + i + ")");
            } else if (msg.what == -8) {//倒计时结束准备再来一次
                requestCodeBtn.setText("获取验证码");
                requestCodeBtn.setClickable(true);
                i = 30;
            } else if (msg.what== 1){//来自APP后台注册请求返回信息
                //从Message对象中取出body的json数据，并转成ResultMsg对象
                ResultMsg resultMsg= JSON.parseObject(msg.obj.toString(),ResultMsg.class);
                if (resultMsg.getErrcode()== ResultStatusCode.OK.getErrcode()){
                    //注册成功，准备跳转到登录页
                    Toast.makeText(getApplicationContext(), "注册成功！",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else if (resultMsg.getErrcode()== ResultStatusCode.USERALREADY_REGISTERED.getErrcode()){
                    //该手机号码已经注册过了
                    Toast.makeText(getApplicationContext(), "该号码已被注册！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Toast.makeText(getApplicationContext(), "发生异常！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {//如果不带what标记，那么这是验证短信码回来的结果消息
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 短信注册成功后，返回MainActivity,然后提示
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                        //Toast.makeText(getApplicationContext(), "提交验证码成功",Toast.LENGTH_SHORT).show();
                        //这里需要加判断，向APP的后台服务器发送注册
                        register(handler);
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "正在获取验证码",Toast.LENGTH_SHORT).show();
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                }else if (result == SMSSDK.RESULT_ERROR){
                    Toast.makeText(getApplicationContext(), "验证码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public void register(Handler mHandler){
        UserInfo userInfo=new UserInfo();
        userInfo.setUserName(inputPhoneEt.getText().toString());
        String passWord=inputPasswordEt.getText().toString();
        passWord= MyUtils.getMD5(passWord+"LaoSiJi");
        userInfo.setPassWord(passWord);
        userInfo.setClientId(MyConstant.AUDIENCE_CLIENTID);
        MyUtils.postRequest(userInfo,mHandler,MyConstant.APPSERVER_URL+"oauth/register");
    }

    /**
     * 判断手机号码是否合理
     *
     * @param phoneNums
     */
    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11)
                && isMobileNO(phoneNums)) {
            return true;
        }
        Toast.makeText(this, "手机号码输入有误！",Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 判断一个字符串的位数
     * @param str
     * @param length
     * @return
     */
    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length ? true : false;
        }
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobileNums) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);
    }

    /**
     * progressbar
     */
    private void createProgressBar() {
        FrameLayout layout = (FrameLayout) findViewById(android.R.id.content);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        ProgressBar mProBar = new ProgressBar(this);
        mProBar.setLayoutParams(layoutParams);
        mProBar.setVisibility(View.VISIBLE);
        layout.addView(mProBar);
    }

    @Override
    protected void onDestroy() {
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }
}

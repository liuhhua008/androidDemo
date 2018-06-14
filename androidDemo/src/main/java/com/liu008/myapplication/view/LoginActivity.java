package com.liu008.myapplication.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.R;
import com.liu008.myapplication.model.AccessToken;
import com.liu008.myapplication.model.UserInfo;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.MyUtils;
import com.liu008.myapplication.utils.ResultMsg;
import com.liu008.myapplication.utils.ResultStatusCode;
import com.zhy.autolayout.*;
/**
 * 登录页面
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * 用户名
     */
    private EditText edt_username;

    /**
     * 密码
     */
    private EditText edt_password;
    private TextView btn_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        edt_username = (EditText) findViewById(R.id.login_input_name);
        edt_password = (EditText) findViewById(R.id.login_input_password);
        btn_login = (TextView) findViewById(R.id.login_btn);
        btn_login.setOnClickListener(mOnClickListener);
        findViewById(R.id.tv_ToRegister).setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.login_btn://登录
                    String userName = edt_username.getText().toString();
                    String userPwd = edt_password.getText().toString();

                    if ("".equals(userName) || "".equals(userPwd)) {
                        Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                        break;
                    }else{
                        login();
                        break;
                    }

                case R.id.tv_ToRegister://注册
                    Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent1);
                    break;
            }

        }
    };

    private void login() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(edt_username.getText().toString());
        String passWord = edt_password.getText().toString();
        passWord = MyUtils.getMD5(passWord + "LaoSiJi");
        userInfo.setPassWord(passWord);
        userInfo.setClientId(MyConstant.AUDIENCE_CLIENTID);
        MyUtils.postRequest(userInfo, mHandler, MyConstant.APPSERVER_URL+"oauth/token");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ResultMsg resultMsg = JSON.parseObject(msg.obj.toString(), ResultMsg.class);
                //登录成功
                if (resultMsg.getErrcode() == ResultStatusCode.OK.getErrcode()) {
                    AccessToken accessToken = JSON.parseObject(resultMsg.getP2pdata().toString(),AccessToken.class);
//                   JSONObject json= (JSONObject) resultMsg.getP2pdata();
//                   // jsonObject=jsonObject.getJSONObject("p2pdata");
//                    AccessToken accessToken = JSONObject.toJavaObject(json,AccessToken.class);
                    //把token信息保存到本地
                    UserManage.getInstance().saveUserInfo(LoginActivity.this, accessToken);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);//跳转到主页
                    startActivity(intent);
                    finish();
                } else if (resultMsg.getErrcode() == ResultStatusCode.INVALID_PASSWORD.getErrcode()) {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}

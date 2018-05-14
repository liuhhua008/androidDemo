package com.liu008.myapplication.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liu008.myapplication.MainActivity;
import com.liu008.myapplication.R;
import com.liu008.myapplication.model.UserManage;

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
    private TextView tv_register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        edt_username = (EditText) findViewById(R.id.edt_username);
        edt_password = (EditText) findViewById(R.id.edt_password);
        tv_register= (TextView) findViewById(R.id.tv_register);
        tv_register.setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_login).setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btn_login://登录
                    String userName = edt_username.getText().toString();
                    String userPwd = edt_password.getText().toString();
                    //这里还没有判断就直接给保存并跳转主页了。
                    //向服务器发送验证
                    //根据返回码判断是否成功，如果成功执行下面带码，不成功就要带显示Toast一下
                    UserManage.getInstance().saveUserInfo(LoginActivity.this, userName, userPwd);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);//跳转到主页
                    startActivity(intent);
                    finish();
                    break;
                case R.id.tv_register://注册
                    Intent intent1=new Intent(LoginActivity.this,RegisterActivity.class);
                    startActivity(intent1);
            }

        }
    };
}

package com.liu008.myapplication.activity.me;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.entity.UserBasicInfo;
import com.liu008.myapplication.utils.NToast;
import com.liu008.myapplication.utils.PermissionUtil;
import com.liu008.myapplication.utils.ToolUtils;
import com.liu008.myapplication.utils.UserUtils;
import com.liu008.myapplication.view.CustomProgress;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MeinfoActivity extends AppCompatActivity implements View.OnClickListener {
    private CommonTitleBar titleBar;
    private ImageView ivHead;
    private RelativeLayout rlNickname;
    private RelativeLayout rlHead;
    private RelativeLayout rlGender;
    private RelativeLayout rlArea;
    private TextView tvNickname;
    private TextView tvId;
    private TextView tvGender;
    private TextView tvArea;

    UserBasicInfo userBasicInfo;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://提交成功
                    UserUtils.getSaveUserBasicInfo(MeinfoActivity.this,mHandler);//这里还会返回200的消息，不过没什么必要处理
                    CustomProgress.dimiss();
                    NToast.longToast(MeinfoActivity.this, "提交成功");
                    break;
                case 0://失败
                    CustomProgress.dimiss();
                    NToast.longToast(MeinfoActivity.this, "提交失败");
                case 200://修改之后又从服务器下载了最新用户数据，成功
                    initData();
                    onResume();
                    default:
                        break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meinfo);
        initview();
        initData();
        setListener();
    }

    private void initData() {
        //先从本地读取信息，没有再从服务器上获取个人信息并显示
        if (MyApplication.getUserBasicInfo() != null) {
            userBasicInfo = MyApplication.getUserBasicInfo();
        } else if (UserUtils.getBaseUserInfoForSP(this) != null) {
            userBasicInfo = UserUtils.getBaseUserInfoForSP(this);
        }
        //显示头像
        Glide.with(this).load(userBasicInfo.getHeadImageUrl()).apply(new RequestOptions().error(R.mipmap.img_error)).into(ivHead);
        tvNickname.setText(userBasicInfo.getNickName());
        tvId.setText(userBasicInfo.getUserName());
    }

    private void initview() {
        titleBar = findViewById(R.id.meinfo_titlebar);
        ivHead = findViewById(R.id.iv_meinfo_head);
        rlNickname = findViewById(R.id.rl_meinfo_nickname);
        rlHead = findViewById(R.id.rl_meinfo_head);
        rlGender = findViewById(R.id.rl_meinfo_gender);
        rlArea = findViewById(R.id.rl_meinfo_area);
        tvNickname = findViewById(R.id.tv_meinfo_nickname);
        tvId = findViewById(R.id.tv_meinfo_id);
        tvGender = findViewById(R.id.tv_meinfo_gender);
        tvArea = findViewById(R.id.tv_meinfo_area);
    }

    private void setListener() {
        rlHead.setOnClickListener(this);
        rlNickname.setOnClickListener(this);
        rlGender.setOnClickListener(this);
        titleBar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                switch (action) {
                    case CommonTitleBar.ACTION_LEFT_BUTTON://返回键
                        try {
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_meinfo_head://点击头像栏进入显示大图的activity
                openBigImageHead();
                break;
            case R.id.rl_meinfo_nickname://点击修改昵称
                openEditDalog(R.id.rl_meinfo_nickname, "修改昵称");
                break;
        }
    }

    private void openEditDalog(int id, String titleNmae) {
        TextView title = new TextView(this);
        title.setText(titleNmae);
        title.setHeight(ToolUtils.dip2px(60));
        title.setTextSize(16);
        title.setPadding(10, 10, 10, 10);
        TextPaint tp = title.getPaint();
        tp.setFakeBoldText(true);
        title.setBackgroundResource(R.color.darkgray);
        title.setGravity(Gravity.CENTER);
        LayoutInflater factory = LayoutInflater.from(this);
        View v = factory.inflate(R.layout.userinfoedit, null);
        final EditText editText = v.findViewById(R.id.rename_edit);
        editText.setText(tvNickname.getText().toString());
        editText.setSelection(editText.getText().length());
        //AlertDialog dialog;
        AlertDialog.Builder buidler = new AlertDialog.Builder(this);
        switch (id) {
            case R.id.rl_meinfo_nickname://修改昵称
                buidler.setCustomTitle(title);
                buidler.setView(v);
                buidler.setPositiveButton("提交", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Map<String,Object> m=new HashMap<>();
                        m.put("type",UserUtils.UPDATE_USERINFO_NICKNAME);
                        m.put("id",userBasicInfo.getUserId());
                        m.put("data",editText.getText().toString());
                        //开启转动提示
                        CustomProgress.show(MeinfoActivity.this,"提交中...",false,null);
                        //启动HTTP提交修改
                        UserUtils.upDateUserinfo(MeinfoActivity.this,mHandler,m);
                    }
                });
                buidler.setNegativeButton("取消", null);
                buidler.show();
                break;
        }
    }

    private void openBigImageHead() {
        Intent intent = new Intent(this, ImageHeadActivity.class);
        intent.putExtra("uri", userBasicInfo.getHeadImageUrl());
        startActivity(intent);
    }
}

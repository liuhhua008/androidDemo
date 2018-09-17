package com.liu008.myapplication.activity.me;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.activity.AddFriendActivity;
import com.liu008.myapplication.entity.CNitemInfo;
import com.liu008.myapplication.entity.IMuserInfo;
import com.liu008.myapplication.manager.DataManager;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.NToast;
import com.liu008.myapplication.utils.ToolUtils;
import com.liu008.myapplication.utils.UserUtils;
import com.liu008.myapplication.view.CustomProgress;
import com.liu008.myapplication.view.RoundImageView;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rong.imkit.RongIM;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ContactInfoActivity extends AppCompatActivity {
    private CommonTitleBar titleBar;
    private RoundImageView headImage;
    private TextView tvName;
    private TextView tvNickName;
    private Button bt01;
    private Button bt02;
    private Intent intent;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    //服务器成功接收后，要在本地通知列表缓存中加入条目
                    DataManager.instance().updateCNlistForCNitem((CNitemInfo) msg.obj);
                    //CustomProgress.show(ContactInfoActivity.this,"已发送",false,null);
                    this.sendEmptyMessageDelayed(-200,800);
                    break;
                case 0:
                    CustomProgress.dimiss();
                    NToast.longToast(ContactInfoActivity.this, "连接失败");
                    break;
                case -200:
                    CustomProgress.dimiss();
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        titleBar = (CommonTitleBar) findViewById(R.id.contactinfo_titlebar);
        headImage=findViewById(R.id.iv_contactinfo_head);
        tvName=findViewById(R.id.tv_contactinfo_zh);
        tvNickName=findViewById(R.id.tv_contactinfo_nc);
        bt01=findViewById(R.id.bt_contactionfo_permain);
        bt02=findViewById(R.id.bt_contactionfo_white);
    }
    private void initData() {
        intent=getIntent();
        tvNickName.setText(intent.getStringExtra("nickname"));
        tvName.setText(intent.getStringExtra("name"));
        Glide.with(this).load(getIntent().getStringExtra("imageUri"))
                .apply(new RequestOptions().error(R.mipmap.img_error))
                .into(headImage);
        //查看关系标志，0为此人为自己 2：没有关系 1：有过关系
        switch (intent.getIntExtra("relation",0)){
            case 0 :
                setSendMessage(intent.getStringExtra("uid"),intent.getStringExtra("nickname"));
                break;
            case 2:
                setAddFriend();
                break;
            case 1:
                //已经是好友
                if (intent.getStringExtra("statusCode").equals("aa")){
                    setSendMessage(intent.getStringExtra("uid"),intent.getStringExtra("nickname"));
                    break;
                }else if (intent.getStringExtra("statusCode").equals("bc")){
                    //我请求加别人，要等对方同意
                    setWait();
                    break;
                }else if (intent.getStringExtra("statusCode").equals("cb")){
                    //我被请求
                    choose();
                    break;
                }else {
                    //其它情况都再给一次机会让其可以再发请求加好友
                    setAddFriend();
                    break;
                }
        }

    }

    /**
     * 选择通过还是拒绝
     */
    private void choose() {
        bt01.setText("通过验证");
        bt01.setVisibility(View.VISIBLE);
        bt02.setText("拒绝加为好友");
        bt02.setVisibility(View.VISIBLE);
        bt01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            CustomProgress.show(ContactInfoActivity.this,"提交中...",false,null);
            sendAgree();
            }
        });
    }

    /**
     * 同意加为好友
     */
    private void sendAgree() {
        final Map<String,String> map = new HashMap();
        map.put("uid", UserManage.getInstance().getUserBasicInfo().getUserId());
        map.put("fid", intent.getStringExtra("uid"));
        UserUtils.httpPostByjwt(MyConstant.APPSERVER_URL + "user/agreefriend", map, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mHandler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String s = response.body().string();
                        if (s != null) {
                            Message msg = new Message();
                            msg.what = 200;
                            //这里要将item部分信息提供过去,这里要符合CNitemInfo数据规范
                            CNitemInfo itemInfo=new CNitemInfo();
                            itemInfo.setSourceId((String) map.get("fid"));//对方的ID
                            itemInfo.setTargetId((String) map.get("uid"));
                            itemInfo.setMessage((String) map.get("msg"));
                            itemInfo.setRelation(1);//服务器那边已经写入关系了
                            itemInfo.setStatusCode("aa");//站在自已角度看，
                            itemInfo.setName(intent.getStringExtra("name"));
                            itemInfo.setNickname(intent.getStringExtra("nickname"));
                            itemInfo.setPortraitUri(intent.getStringExtra("imageUri"));
                            msg.obj = itemInfo;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
        );
    }

    /**
     * 正在等待对对验证通过
     */
    private void setWait() {
        bt02.setVisibility(View.VISIBLE);
        bt02.setText("等待对方验证通过");
        bt02.setEnabled(false);
    }

    /**
     * 设置按键形式为加好友
     */
    private void setAddFriend() {
        bt01.setText("加为好友");
        bt01.setVisibility(View.VISIBLE);
        bt01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            openEditDalog("附加留言");
            }
        });
    }

    /**
     * 设置按键形式为发消息
     */
    private void setSendMessage(final String uid,final  String name) {
        bt01.setText("发送消息");
        bt01.setVisibility(View.VISIBLE);
        bt01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RongIM.getInstance().startPrivateChat(ContactInfoActivity.this, uid, name);
            }
        });
    }


    private void setListener() {
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
                }
            }
        });
    }

    private void openEditDalog(String titleNmae) {
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
//        editText.setText(tvNickname.getText().toString());
//        editText.setSelection(editText.getText().length());
        //AlertDialog dialog;
        AlertDialog.Builder buidler = new AlertDialog.Builder(this);
                buidler.setCustomTitle(title);
                buidler.setView(v);
                buidler.setPositiveButton("提交", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //开启转动提示
                        CustomProgress.show(ContactInfoActivity.this,"提交中...",false,null);
                        //启动HTTP提交修改
                        Map<String,String> map = new HashMap();
                        map.put("uid", UserUtils.getUserInfo().getUserId());
                        map.put("fid", intent.getStringExtra("uid"));
                        map.put("msg",editText.getText().toString());
                        sendAfriendHttp(map);
                    }
                });
                buidler.setNegativeButton("取消", null);
                buidler.show();

    }

    /**
     * 向服务器发送请求消息
     */
    void sendAfriendHttp(final Map<String,String> map){
        UserUtils.httpPostByjwt(MyConstant.APPSERVER_URL + "user/addfriend", map, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mHandler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String s = response.body().string();
                        if (s != null) {
                            Message msg = new Message();
                            msg.what = 200;
                            //这里要将item部分信息提供过去,这里要符合CNitemInfo数据规范
                            CNitemInfo itemInfo=new CNitemInfo();
                            itemInfo.setSourceId((String) map.get("fid"));//对方的ID
                            itemInfo.setTargetId((String) map.get("uid"));
                            itemInfo.setMessage((String) map.get("msg"));
                            itemInfo.setRelation(1);//服务器那边已经写入关系了
                            itemInfo.setStatusCode("bc");//站在自已角度看，
                            itemInfo.setName(intent.getStringExtra("name"));
                            itemInfo.setNickname(intent.getStringExtra("nickname"));
                            itemInfo.setPortraitUri(intent.getStringExtra("imageUri"));
                            msg.obj = itemInfo;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
        );
    }
}

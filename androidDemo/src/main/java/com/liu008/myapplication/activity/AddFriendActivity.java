package com.liu008.myapplication.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.activity.me.ContactInfoActivity;
import com.liu008.myapplication.adapter.AddFrendAdapter;
import com.liu008.myapplication.adapter.SortAdapter;
import com.liu008.myapplication.entity.CNitemInfo;
import com.liu008.myapplication.entity.IMuserInfo;
import com.liu008.myapplication.entity.UserBasicInfo;
import com.liu008.myapplication.manager.DataManager;
import com.liu008.myapplication.model.UserInfo;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.sortlist.SortModel;
import com.liu008.myapplication.utils.ApiHttpClient;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.NToast;
import com.liu008.myapplication.utils.ResultMsg;
import com.liu008.myapplication.utils.ResultStatusCode;
import com.liu008.myapplication.utils.UserUtils;
import com.liu008.myapplication.view.CustomProgress;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFriendActivity extends AppCompatActivity {
    private CommonTitleBar titleBar;
    private ListView listView;
    private List<CNitemInfo> mSourceDateList=new ArrayList<CNitemInfo>();
    private AddFrendAdapter adapter;
    TextView dalog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        mSourceDateList=DataManager.instance().getContactNotificationData();
        if (mSourceDateList!=null){
            adapter.updateListView(mSourceDateList);
        }
    }

    private void initView() {
        titleBar = (CommonTitleBar) findViewById(R.id.addfriend_titlebar);
        titleBar.getCenterSearchEditText().setHint("根据手机号码查找");
        dalog = (TextView) findViewById(R.id.tv_addfriend_dalog);
        listView = (ListView) this.findViewById(R.id.addfriend_list);
        adapter = new AddFrendAdapter(this, mSourceDateList);
        listView.setAdapter(adapter);
    }

    private void setListener() {
        titleBar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                switch (action) {
                    case CommonTitleBar.ACTION_LEFT_BUTTON://返回键
                        if (dalog.getVisibility()==View.VISIBLE){
                            dalog.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        }
                        try {
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case CommonTitleBar.ACTION_SEARCH_DELETE://输入框清除
                        titleBar.getCenterSearchEditText().clearComposingText();
                        dalog.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        break;
                    case CommonTitleBar.ACTION_RIGHT_TEXT://查询键
                        dalog.setVisibility(View.GONE);
                        if (judgePhoneNums(titleBar.getCenterSearchEditText().getText().toString())) {
                            searchFriend(titleBar.getCenterSearchEditText().getText().toString());
                        }
                        break;
                }
            }
        });

        /**
         * 点击显示出来的好友条目，跳转到详细信息页。
         * 个人信息用intent发过去
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CNitemInfo itemInfo=(CNitemInfo) adapter.getItem(position);
                IMuserInfo info=new IMuserInfo();
                info.setUserId(itemInfo.getSourceId());
                info.setName(itemInfo.getName());
                info.setNickname(itemInfo.getNickname());
                info.setPortraitUri(itemInfo.getPortraitUri());
                info.setRelation(itemInfo.getRelation());
                info.setStatusCode(itemInfo.getStatusCode());
                startContactInfoActivity(info);
            }
        });
    }

    //执行后台异步任务去服务器查找
    private void searchFriend(String s) {
        new SeachFrendAsncTask().execute(s);
    }

    /**
     * Params:启动任务时输入的参数类型.
     * Progress:后台任务执行中返回进度值的类型.
     * Result:后台任务执行完成后返回结果的类型
     */
    private class SeachFrendAsncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            dalog.setVisibility(View.GONE);
            CustomProgress.show(AddFriendActivity.this, "正在查找...", true, null);
        }

        @Override
        protected String doInBackground(String... strings) {
            //去后台网络查找好友数据
            try {
            UserBasicInfo u= UserUtils.getUserInfo();
            OkHttpClient client = ApiHttpClient.getInstance(AddFriendActivity.this);
            String url = MyConstant.APPSERVER_URL + "user/searchUser/" + strings[0];

            RequestBody body = new FormBody.Builder().add("uid", u.getUserId()).build();
            Request request = new Request.Builder().url(url).post(body).build();

                Response response = client.newCall(request).execute();//同步方法，会有超时产生
                if (response.code() == 200) {
                    String s = response.body().string().toString();
                    return s;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("error") || s == null) {
                //Toast.makeText(AddFriendActivity.this,"网络访问异常",Toast.LENGTH_SHORT).show();
                dalog.setVisibility(View.VISIBLE);
                dalog.setText("网络异常");
                CustomProgress.dimiss();
                return;
            }
            ResultMsg resultMsg = JSON.parseObject(s, ResultMsg.class);//第一层取结果码
            //如果有结果
            if (resultMsg.getErrcode() == ResultStatusCode.OK.getErrcode()) {
                //第二层解析取个人信息
                IMuserInfo userInfo = JSON.parseObject(resultMsg.getP2pdata().toString(), IMuserInfo.class);
//                mSourceDateList = new ArrayList<IMuserInfo>();
//                mSourceDateList.add(userInfo);
//                adapter.updateListView(mSourceDateList);
//                dalog.setVisibility(View.GONE);
                CustomProgress.dimiss();
                //查找用户直接打开详细信息页
                startContactInfoActivity(userInfo);
                return;
            } else if (resultMsg.getErrcode() == ResultStatusCode.FINDUSER_NOFIND.getErrcode()) {
                //Toast.makeText(AddFriendActivity.this,"未发现联系人",Toast.LENGTH_SHORT).show();
//                if (mSourceDateList != null) {
//                    mSourceDateList.clear();
//                    adapter.updateListView(mSourceDateList);
//                }
                listView.setVisibility(View.GONE);
                dalog.setVisibility(View.VISIBLE);
                dalog.setText("没有该用户");
                CustomProgress.dimiss();
                return;
            }
            CustomProgress.dimiss();
        }
    }

    /**
     * 跳转到详细信息页。
     * 个人信息用intent发过去
     * @param userinfo
     */
    private void  startContactInfoActivity(IMuserInfo userinfo){
        Intent intent=new Intent(AddFriendActivity.this, ContactInfoActivity.class);
        intent.putExtra("uid",userinfo.getUserId());
        intent.putExtra("name",userinfo.getName());
        intent.putExtra("nickname",userinfo.getNickname());
        intent.putExtra("imageUri",userinfo.getPortraitUri());
        intent.putExtra("relation",userinfo.getRelation());
        intent.putExtra("statusCode",userinfo.getStatusCode());
        startActivity(intent);
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
        Toast.makeText(this, "手机号码输入有误！", Toast.LENGTH_SHORT).show();
        return false;
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
     * 判断一个字符串的位数
     *
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

    @Override
    public void onBackPressed() {
        if (dalog.getVisibility()==View.VISIBLE){
            dalog.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
    }
}

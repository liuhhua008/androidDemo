package com.liu008.myapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu008.myapplication.model.AccessToken;
import com.liu008.myapplication.utils.ApiHttpClient;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.ResultMsg;


import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 008 on 2018/3/22.
 */

public class BaseFragment extends Fragment {
    public static BaseFragment newInstance(String info){
        Bundle args = new Bundle();
        BaseFragment fragment=new BaseFragment();
        args.putString("info",info);
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_base,null);
        TextView tvInfo=(TextView) view.findViewById(R.id.textView);
        TextView tvApi=(TextView) view.findViewById(R.id.textViewApi);
        tvInfo.setText(getArguments().getString("info"));
        tvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从底部弹出一个提示条目
                //Snackbar.make(v, "Don't click me.please!.", Snackbar.LENGTH_SHORT).show();
                FrameLayout mContentContainer;
                //拿到除顶层窗口对象，对像ChildAt（0）为标题栏，1为下面全部内容
                ViewGroup mDecorView= (ViewGroup) getActivity().getWindow().getDecorView();
                //在子类Activity中去把标记为"folat_tag_frameLayout"的FrameLayout拿出来作为容器
                mContentContainer = (FrameLayout) ((ViewGroup)mDecorView.
                        getChildAt(0)).findViewWithTag("folat_tag_frameLayout");
                mContentContainer.setVisibility(View.VISIBLE);
            }
        });
        tvApi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final OkHttpClient client= ApiHttpClient.getInstance(getContext());
                RequestBody body=new FormBody.Builder().add("role","Manager").build();
                final Request request=new Request.Builder().url(MyConstant.APPSERVER_URL+"user/getusers")
                        .post(body).build();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            Response response = client.newCall(request).execute();
                            ResultMsg resultMsg = JSON.parseObject(response.body().string(), ResultMsg.class);
                            //JSONObject jsonObject= JSON.parseObject(response.body().string());
                            String s="code:"+resultMsg.getErrcode()+"  内容："+resultMsg.getErrmsg();
                            //打印返回值代码
                            System.out.println(s);
                            if (resultMsg.getP2pdata()!=null ){

                                //AccessToken accessToken = JSON.parseObject(resultMsg.getP2pdata().toString(), AccessToken.class);
                                System.out.println(JSON.toJSONString(resultMsg.getP2pdata(),true));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }}).start();

            }
        });
        return view;
    }
}

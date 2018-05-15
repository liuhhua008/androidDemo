package com.liu008.myapplication.utils;

import com.liu008.myapplication.interceptor.TokenInterceptor;

import okhttp3.OkHttpClient;

/**
 * 用来创建带拦截器的OKHTTP客户端对象，方便API的拦截操作，试用户无感。
 * Created by 008 on 2018/5/15.
 */

public class ApiHttpClient  {

    public static OkHttpClient getInstance(){
       OkHttpClient client =new OkHttpClient();
        client.networkInterceptors().add(new TokenInterceptor());
        return client;
    }
}

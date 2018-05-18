package com.liu008.myapplication.utils;

import android.content.Context;

import com.liu008.myapplication.interceptor.TokenInterceptor;

import okhttp3.OkHttpClient;

/**
 * 用来创建带拦截器的OKHTTP客户端对象，方便API的拦截操作，试用户无感。
 * Created by 008 on 2018/5/15.
 */

public class ApiHttpClient extends OkHttpClient {


    public static OkHttpClient getInstance(Context context){
      // return new ApiHttpClient(context).newBuilder().addNetworkInterceptor(new TokenInterceptor(context)).build();
       return  new OkHttpClient.Builder().addInterceptor(new TokenInterceptor(context))
               .build();
    }
}

package com.liu008.myapplication.interceptor;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自动刷新token的拦截器
 * Created by 008 on 2018/5/15.
 */

public class TokenInterceptor implements Interceptor {
    private static final String TAG = "TokenInterceptor";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        Log.d(TAG, "response.code=" + response.code());
        //根据和服务端的约定判断token过期
        if (isTokenExpired(response)) {
            Log.d(TAG, "自动刷新Token,然后重新请求数据");
            //同步请求方式，获取最新的Token
            String newToken = getNewToken();
            //使用新的Token，创建新的请求
            Request newRequest = chain.request()
                    .newBuilder()
                    .header("Authorization", "bearer " + newToken)
                    .build();
            //重新请求
            return chain.proceed(newRequest);
        }
        return response;
    }

    /**
     * 根据Response，判断Token是否失效
     *
     * @param response
     * @return
     */
    private boolean isTokenExpired(Response response) {
        //查看服务器返回的HTTP头信息，用以确定api-jwt是否过期
        String auth=response.header("Authorization");
        if ("expires".equals(auth)) {
            return true;//过期
        }
        return false;
    }

    /**
     * 同步请求方式，获取最新的Token
     *
     * @return api-jwt
     */
    private String getNewToken() throws IOException {
        // 通过访问服务器refreshAccessToken接口，用本地的refresh-jwt获取新accessToken，同时保存到本地并且返回用来认证
        //的API-JWT，如果refresh-jwt也过期的话。提示用户”长期未登录，重新登录"

        String newToken = "";
        return newToken;
    }
}

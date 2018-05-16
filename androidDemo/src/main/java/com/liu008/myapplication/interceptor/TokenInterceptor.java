package com.liu008.myapplication.interceptor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.model.AccessToken;
import com.liu008.myapplication.model.UserInfo;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.ResultMsg;
import com.liu008.myapplication.utils.ResultStatusCode;
import com.liu008.myapplication.view.LoginActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 自动给需要API验证的http请求加上jwt验证信息，并完成自己刷新token的拦截器
 * Created by 008 on 2018/5/15.
 */

public class TokenInterceptor implements Interceptor {
    private static final String TAG = "TokenInterceptor";
    @Override
    public Response intercept(Chain chain) throws IOException {
        //统一对http请求加上jwt验证头
        Request request = chain.request();
        SharedPreferences sp= MyApplication.getmContext().getSharedPreferences("userInfo",Context.MODE_PRIVATE);
        request=request.newBuilder().header("Authorization", "bearer " +sp.getString("access_token",null)).build();
        Response response = chain.proceed(request);
        Log.d(TAG, "response.code=" + response.code());
        //根据和服务端的约定判断token过期
        if (isTokenExpired(response)) {
            Log.d(TAG, "自动刷新Token,然后重新请求数据");
            String refreshJwt=sp.getString("refresh_type",null);

            //使refreshJwt去获取新的token
            Request newRequest=chain.request().newBuilder()
                    .url(MyConstant.APPSERVER_URL+"oauth/refreshToken")
                    .header("Authorization", "bearer " + refreshJwt)
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),MyConstant.AUDIENCE_CLIENTID))
                    .build();
            //发请求刷新token请求并接收响应结果
           Response refreshResponse=chain.proceed(newRequest);
           ResultMsg resultMsg = JSON.parseObject(refreshResponse.body().string(), ResultMsg.class);
           if (isTokenExpired(refreshResponse)){
               Log.d(TAG, "你已经很久没有登录了，refresh-jwt都过期了");
               return refreshResponse;
           }else if (resultMsg.getErrcode()== ResultStatusCode.OK.getErrcode()){
               //如果获取到了新token
               AccessToken accessToken = JSON.parseObject(resultMsg.getP2pdata().toString(),AccessToken.class);
               //保存到本地
               UserManage.getInstance().saveUserInfo(MyApplication.getmContext(), accessToken);
               //重新用新的jwt访问api并返回给客户端
               return chain.proceed(request.newBuilder().header("Authorization", "bearer " +accessToken.getAccess_token()).build());
           }

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


}

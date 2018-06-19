package com.liu008.myapplication.interceptor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.liu008.myapplication.MyApplication;
import com.liu008.myapplication.R;
import com.liu008.myapplication.model.AccessToken;
import com.liu008.myapplication.model.UserInfo;
import com.liu008.myapplication.model.UserManage;
import com.liu008.myapplication.utils.ApiHttpClient;
import com.liu008.myapplication.utils.MyConstant;
import com.liu008.myapplication.utils.ResultMsg;
import com.liu008.myapplication.utils.ResultStatusCode;
import com.liu008.myapplication.view.LoginActivity;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 自动给需要API验证的http请求加上jwt验证信息，并完成自己刷新token的拦截器
 * Created by 008 on 2018/5/15.
 */

public class TokenInterceptor implements Interceptor {
    private Context context;

    private static final String TAG = "TokenInterceptor";

    public TokenInterceptor(Context context){
        this.context = context;
    }

    /**
     * 统一对http请求加上jwt验证头
     * @param chain
     * @return
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        RequestBody requestBody = request.body();
        //统一加JWT标记--------------------------------------------------------------------
        //SharedPreferences sp= MyApplication.getmContext().getSharedPreferences("userInfo",Context.MODE_PRIVATE);
        //request=request.newBuilder().header("Authorization", "bearer " +sp.getString("access_token",null)).build();
        String jwt =MyApplication.getAccess_jwt();
        request=request.newBuilder().header("Authorization", "bearer " +MyApplication.getAccess_jwt()).build();
        Response response = chain.proceed(request);
        if (response.code()== 401){
            context.startActivity(new Intent(context,LoginActivity.class));
            UserManage.getInstance().deleteUserInfo(context);
            Looper.prepare();
            Toast.makeText(context, "验证信息出错，请重新登录。", Toast.LENGTH_LONG).show();
            Looper.loop();// 进入loop中的循环，查看消息队
            return response;
        }
        //Log.d(TAG, "response.code=" + response.body().string());
        //1.首选判断是否已经被下线
        if (isUserCodeError(response)){
            Log.d(TAG, "您已经下线，请重新登录");
            //跳转到登录页
            context.startActivity(new Intent(context,LoginActivity.class));
            UserManage.getInstance().deleteUserInfo(context);
            Looper.prepare();
            Toast.makeText(context, "您已经下线，请重新登录。", Toast.LENGTH_LONG).show();
            Looper.loop();// 进入loop中的循环，查看消息队
            return response;
        }
        //2.JWT如果过期,发请求刷新token请求并接收响应结果----------------------------------------------------------------------
        if (isTokenExpired(response)) {
            Log.d(TAG, "自动刷新Token,然后重新请求数据");
            String refreshJwt = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("refresh_type", null);
            //使refreshJwt去获取新的token
            Request newRequest = chain.request().newBuilder()
                    .url(MyConstant.APPSERVER_URL + "oauth/refreshToken")
                    .header("Authorization", "bearer " + refreshJwt)
                    .post(new FormBody.Builder().add("clientId", MyConstant.AUDIENCE_CLIENTID).build())
                    .build();
            //Log.d(TAG, newRequest.body().toString());
            Response refreshResponse = chain.proceed(newRequest);
            MediaType mediaType=response.body().contentType();
            String content= refreshResponse.body().string();
            ResultMsg resultMsg = JSON.parseObject(content, ResultMsg.class);
            //如果刷新jwt也过期的话,返回告之要重新登录
            if (isTokenExpired(refreshResponse)) {
                Log.d(TAG, "你已经很久没有登录了，refresh-jwt都过期了");
                //跳转到登录页
                context.startActivity(new Intent(context,LoginActivity.class));
                UserManage.getInstance().deleteUserInfo(context);
                Looper.prepare();
                Toast.makeText(context, "长时间未登录，请重新登录。", Toast.LENGTH_LONG).show();
                Looper.loop();// 进入loop中的循环，查看消息队

                return refreshResponse.newBuilder().body(ResponseBody.create(mediaType, content)).build();
            } else if (resultMsg.getErrcode() == ResultStatusCode.OK.getErrcode()) {
                Log.d(TAG, "换证成功了");
                //如果获取到了新token
                AccessToken accessToken = JSON.parseObject(resultMsg.getP2pdata().toString(), AccessToken.class);
                //保存到新JWT
                UserManage.getInstance().saveUserInfo(context, accessToken);
                //重新用新的jwt访问api并返回给客户端，这里用的依然是原始的request请求，只是更换了HTTP头部
                Response response2 = chain.proceed(request.newBuilder()
                        .post(requestBody)
                        .header("Authorization", "bearer " + accessToken.getAccess_token()).build());
                return response2;
            }
            //其它原因的话直接返回结果,其实这里要再处理一下才好.
            //跳转到登录页
            context.startActivity(new Intent(context,LoginActivity.class));
            UserManage.getInstance().deleteUserInfo(context);
            Looper.prepare();
            Toast.makeText(context, "验证信息出错，请重新登录。", Toast.LENGTH_LONG).show();
            Looper.loop();// 进入loop中的循环，查看消息队

            return refreshResponse.newBuilder().body(ResponseBody.create(mediaType, content)).build();


        }else {
            //一切OK直接返回
            Log.d(TAG, "jwt验证没有问题,直接返回原始请求数据");
            return response.newBuilder().build();
        }


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

        private boolean isUserCodeError(Response response){
            String auth=response.header("Authorization");
            if ("userCodeError".equals(auth)) {
                return true;
            }
            return false;
        }


}

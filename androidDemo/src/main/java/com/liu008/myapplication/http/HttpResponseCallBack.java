package com.liu008.myapplication.http;

/**
 * 返回的接口
 */
public interface HttpResponseCallBack {
    void response(String response);//处理服务器返回成功
    void error(Exception e);//处理异常
}

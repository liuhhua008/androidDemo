package com.liu008.myapplication.utils;

/**
 * Created by 008 on 2018/5/13.
 */

public class ResultMsg {
    private int errcode;
    private String errmsg;
    private Object p2pdata;

    public ResultMsg() {
    }

    public ResultMsg(int ErrCode, String ErrMsg, Object P2pData)
    {
        this.errcode = ErrCode;
        this.errmsg = ErrMsg;
        this.p2pdata = P2pData;
    }
    public int getErrcode() {
        return errcode;
    }
    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }
    public String getErrmsg() {
        return errmsg;
    }
    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
    public Object getP2pdata() {
        return p2pdata;
    }
    public void setP2pdata(Object p2pdata) {
        this.p2pdata = p2pdata;
    }
}

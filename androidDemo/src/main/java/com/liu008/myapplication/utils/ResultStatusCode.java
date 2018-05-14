package com.liu008.myapplication.utils;

/**
 * Created by 008 on 2018/5/13.
 */

public enum ResultStatusCode {
    OK(0,"OK"),
    SYSTEM_ERR(30001,"System error"),
    PERMISSION_DENIED(40001,"permission_denied"),
    INVALID_CLIENTID(50001,"invalid_clientid"),
    INVALID_PASSWORD(50002,"invalid_password"),
    INVALID_TOKEN(60001,"invalid_token"),
    USERALREADY_REGISTERED(70001,"Already registered");

    private int errcode;
    private String errmsg;
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
    private ResultStatusCode(int Errode, String ErrMsg)
    {
        this.errcode = Errode;
        this.errmsg = ErrMsg;
    }
}

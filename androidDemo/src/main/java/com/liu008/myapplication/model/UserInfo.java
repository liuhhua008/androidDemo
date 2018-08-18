package com.liu008.myapplication.model;

/**
 * 用户信息model
 * Created by 008 on 2018/3/23.
 */

public class UserInfo {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private  String passWord;
    private String clientId;
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}

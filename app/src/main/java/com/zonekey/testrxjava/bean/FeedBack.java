package com.zonekey.testrxjava.bean;

/**
 * Created by xu.wang
 * Date on 2017/6/28 11:01
 * 智客助手反馈提交的对象
 */

public class FeedBack {
    private String content;
    private String appid;
    private String code;
    private String loginname;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }
}

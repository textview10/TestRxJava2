package com.zonkey.mobileteach_lib.interf;

/**
 * Created by xu.wang
 * Date on 2016/11/11 10:52
 */
public interface OnTcpSendMessageListner {
    void success(short mainCmd, short subCmd, byte appCodeCmd, String body);
    void error(Exception e);
}

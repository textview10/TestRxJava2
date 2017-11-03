package com.zonkey.mobileteach_lib.interf;

/**
 * Created by xu.wang
 * Date on 2016/11/11 10:14
 */
public interface OnTcpSendFileListener {
    void onFailure(Exception e);
    void onProgress(long totalSize, int progress, String fileName);
    void Success();
}

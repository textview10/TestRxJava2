package com.zonkey.mobileteach_lib.net.util;

import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.SocketConstant;
import com.zonkey.mobileteach_lib.net.client.UdpUtil;
import com.zonkey.mobileteach_lib.net.server.UdpServer;


/**
 * Created by xu.wang
 * Date on 2017/4/11 10:29
 * 发送心跳指令的线程
 */
public class HeartBeatThread extends Thread {
    private String TAG = getClass().getSimpleName().toString();
    private boolean isDebug = false;
    private boolean isSend = true;  //是否发送
    public boolean isSetHeartBeat = true;
    private static final int TIME_FOR_SEND_HEART_BEAT = 3000;   //发送心跳信息的时间间隔
    public int count = 0;
    private UdpServer udpServer;
    private UdpUtil udpUtil;

    public HeartBeatThread(UdpServer udpServer) {
        this.udpServer = udpServer;
//        if (MobileTeach.CurrentApp == MobileTeach.App_MobileTeach_Student) {
//            isSetHeartBeat = false;
//        }
    }

    @Override
    public void run() {
        super.run();
        while (isSend && isSetHeartBeat) {
            if (count > 4) {
                count = 0;
//                Log.e(TAG, "与服务器断开连接, 时间" + DateUtil.getTextDate());
                Message msg = new Message();
                msg.what = SocketConstant.APP_QUIT_LOGIN_COMMAND;
                udpServer.mHandler.sendMessage(msg);
                stopHeartBeat();
            }
            //向服务器发送心跳包
            if (MobileTeach.pc_udp_port != 0) sendHeartbeatPackage();
            SystemClock.sleep(TIME_FOR_SEND_HEART_BEAT);
        }
    }

    //停止线程
    public void stopHeartBeat() {
        isSend = false;
    }

    //接收到心跳信息
    public void acceptHeartBeat() {
        if (isDebug) Log.e(TAG, "收到心跳消息");
        count = 0;
    }

    //发送心跳指令
    public void sendHeartbeatPackage() {
        if (isDebug) Log.e(TAG, "发送心跳消息");
        if (udpUtil == null) udpUtil = new UdpUtil();
        udpUtil.sendMessage(MobileTeachApi.NetworkLayer.Main_Cmd, MobileTeachApi.NetworkLayer.Request_AskHeartBeat);
        count += 1;
    }
}

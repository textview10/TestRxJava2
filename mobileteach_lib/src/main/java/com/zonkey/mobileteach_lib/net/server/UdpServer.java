package com.zonkey.mobileteach_lib.net.server;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.UdpSocketInstance;
import com.zonkey.mobileteach_lib.net.SocketConstant;
import com.zonkey.mobileteach_lib.net.bean.ReceiverAddress;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.net.util.HeartBeatThread;
import com.zonkey.mobileteach_lib.util.ByteUtil;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.util.ThreadPoolManager;

import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Created by xu.wang
 * Date on 2016/11/22 10:25
 * 接收udp端口消息的server
 */
public class UdpServer {
    private String TAG = "UdpServer";
    private boolean isDebug = false;
    public Handler mHandler;
    private ThreadPoolManager threadPoolManager;
    private final int cacheLength = 1024 * 8;
    private boolean isAcceptInfo = true;    //是否接受消息
    private HeartBeatThread mHeartThread;

    public UdpServer(Handler mHandler) {
        this.mHandler = mHandler;
        threadPoolManager = new ThreadPoolManager();
    }


    public void startServer() {
        threadPoolManager.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[cacheLength];
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                    LogUtil.writeLog("UdpServer", "启动UdpServer");
                    showLog("begin server localPort" + MobileTeach.local_udp_port);
                    while (isAcceptInfo) {
                        try {
                            UdpSocketInstance.getInstance().getDatagramSocket().receive(datagramPacket);
                            byte[] m = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
                            acceptSocket(m, datagramPacket);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    showLog("line 59" + e.toString());
                }
            }
        });
    }

    //处理收到的信息,分解信息
    private void acceptSocket(byte[] m, DatagramPacket dp) {
        byte netVersion = m[1];
        if (netVersion == MobileTeachApi.encodeVersion2_v1) {   //网络模块v1版本
            netForV2(m, dp);
        } else if (netVersion == MobileTeachApi.encodeVersion2_v0) {    //网络模块v0版本
            netForV1(m, dp);
        } else {
            LogUtil.writeLog("UdpServer", "收到的Udp消息无法解析");
        }
    }

    //网络模块v1时的指令
    private void netForV1(byte[] m, DatagramPacket dp) {
        if (m.length < 10) {
            LogUtil.writeLog("UdpServer", "v1 收到的消息长度小于10" + m.length);
            return;
        }
        byte[] buff = new byte[]{};
        byte appCodeCmd = m[2];
        byte mainCmd = m[4];
        byte subCmd = m[6];
        if (isBeatCmd(mainCmd, subCmd, 1)) {
            return;
        }
        buff = new byte[2];
        System.arraycopy(m, 8, buff, 0, 2);
        int stringBodySize = ByteUtil.bytesToShort(buff);
        buff = new byte[stringBodySize];
        if (m.length < 10 + stringBodySize) {
            LogUtil.writeLog("UdpServer", "v1 收到的消息长度理论上是" + (10 + stringBodySize) + "实际是" + m.length);
            return;
        }
        System.arraycopy(m, 10, buff, 0, stringBodySize);
        String body = new String(buff);
        sendMsg((short) (mainCmd & 0x00FF), (short) (subCmd & 0x00FF), appCodeCmd, body, dp.getAddress().getHostAddress(), dp.getPort(), 1);
        buff = null;
    }

    // 网络模块v2版本的处理逻辑
    private void netForV2(byte[] m, DatagramPacket dp) {
        if (m.length < 19) {
            LogUtil.writeLog("UdpServer", "v2 收到的消息长度小于19" + m.length);
            return;
        }
        byte[] buff = new byte[]{};
        byte appCodeCmd = m[2];        //应用名
        buff = new byte[2];
        System.arraycopy(m, 4, buff, 0, 2);
        short mainCmd = ByteUtil.bytesToShort(buff);      //主指令
        buff = new byte[2];
        System.arraycopy(m, 6, buff, 0, 2);
        short subCmd = ByteUtil.bytesToShort(buff);      //子指令
        if (isBeatCmd(mainCmd, subCmd, 2)) {
            return;
        }
        buff = new byte[4];
        System.arraycopy(m, 8, buff, 0, 4);                 //发送方的消息是从那个端口号发送的
        int sendPort = ByteUtil.bytesToInt(buff);
        System.arraycopy(m, 12, buff, 0, 4);
        int stringBodySize = ByteUtil.bytesToInt(buff);
        if (m.length < 19 + stringBodySize) {
            LogUtil.writeLog("UdpServer", "v2 收到的消息长度理论上是" + (19 + stringBodySize) + "实际是" + m.length);
            return;
        }
        buff = new byte[stringBodySize];
        System.arraycopy(m, 19, buff, 0, stringBodySize);
        String body = new String(buff);
        // TODO 打开下面哪行代码可以调试收到的消息
        showLog(body);
//        LogUtil.writeLog("UdpServer", body);
        sendMsg(mainCmd, subCmd, appCodeCmd, body, dp.getAddress().getHostAddress(), sendPort, 1);
    }

    //

    /**
     * 判断是否是心跳指令
     *
     * @param response_main
     * @param response_sub
     * @param netVersion    0是v0版协议,1是v1版协议
     * @return true是心跳指令, false不是心跳指令
     */
    private boolean isBeatCmd(short response_main, short response_sub, int netVersion) {
        if (netVersion == 2) {
            if (response_main == MobileTeachApi.NetworkLayer.Main_Cmd &&   //收到心跳指令
                    response_sub == MobileTeachApi.NetworkLayer.Response_AskHeartBeat && mHeartThread != null) {
                mHeartThread.acceptHeartBeat();
                return true;
            } else {
                return false;
            }
        } else {
            if (response_main == (0x10) &&
                    response_sub == (0x81)) {
                showLog("收到心跳指令" + System.currentTimeMillis());
                return true;
            } else {
                return false;
            }
        }
    }

    //拼接消息,发走
    private void sendMsg(short mainCmd, short subCmd, byte appCodeCmd, String body, String ip, int port, int type) {
        ReceiverInfo receiverInfo = new ReceiverInfo(mainCmd, subCmd, appCodeCmd, body,
                new ReceiverAddress(ip, port, type));
        Message msg = new Message();
        msg.what = SocketConstant.UDP_COMMAND;
        msg.obj = receiverInfo;
        mHandler.sendMessage(msg);
    }

    //停止心跳线程
    public void stopHeartBeat() {
        if (mHeartThread != null) {
            mHeartThread.stopHeartBeat();
            mHeartThread = null;
        }
    }

    //开启心跳线程
    public void startHeartBeat() {
        stopHeartBeat();
        mHeartThread = new HeartBeatThread(this);
        mHeartThread.start();
    }


    //关闭udpSocket
    public void closeSocket() {
        stopHeartBeat();
        isAcceptInfo = false;
        closeUdpServer();
        if (mHeartThread != null) {
            mHeartThread.interrupt();
            mHeartThread = null;
        }
    }

    private void closeUdpServer() {
        UdpSocketInstance.getInstance().closeDatagramSocket();
        showLog("UdpServer关闭");
    }

    private void showLog(String msg) {
        if (!isDebug) return;
        Log.e(TAG, msg);
    }
}

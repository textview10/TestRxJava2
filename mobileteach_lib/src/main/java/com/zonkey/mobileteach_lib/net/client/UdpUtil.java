package com.zonkey.mobileteach_lib.net.client;

import android.text.TextUtils;
import android.util.Log;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.net.UdpSocketInstance;
import com.zonkey.mobileteach_lib.net.util.ProcessMsgUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by xu.wang
 * Date on 2016/11/16 10:55
 * Modify on 2017/05/02
 */
public class UdpUtil {
    public static final String TAG = "UdpUtil";
    private int port;
    private String ip;
    private ProcessMsgUtil processMsgUtil;
    //--------------后续版本扩充使用------------------

    DatagramSocket ds;

    public UdpUtil(String ip, int port) {
        this.port = port;
        this.ip = ip;
        processMsgUtil = new ProcessMsgUtil();
    }

    public UdpUtil() {
        this.ip = MobileTeach.pc_ip;
        this.port = MobileTeach.pc_udp_port;
        processMsgUtil = new ProcessMsgUtil();
    }

    /**
     * 发送不需要获得回应的UDP指令
     */
    public void sendMessage(short mainCommand, short subCommand) {
        sendMessage(mainCommand, subCommand, null);
    }

    public void sendMessage(final short mainCommand, final short subCommand, final String sendBody) {
        if (TextUtils.isEmpty(ip) || port == 0) return;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    send(mainCommand, subCommand, sendBody);
                } catch (Exception e) {
                    Log.e(TAG, e.toString() + "");
                }
            }
        }.start();
    }

    private synchronized void send(short mainCmd, short subCmd, String sendBody) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(ip);
//        ds = new DatagramSocket();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] sendMsg = processMsgUtil.getSendMsg(mainCmd, subCmd, MobileTeach.local_udp_port, sendBody);
        if (!TextUtils.isEmpty(sendBody)) {
            baos.write(sendMsg);    //向os写入消息内容
            baos.write(sendBody.getBytes());    //向os写入消息内容
        } else {
            baos.write(sendMsg);
        }
        //定义用来发送数据的DatagramPacket实例
        DatagramPacket dp_send = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, inetAddress, port);
        UdpSocketInstance.getInstance().getDatagramSocket().send(dp_send);
        baos.close();
    }
}

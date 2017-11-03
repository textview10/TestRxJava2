package com.zonkey.mobileteach_lib.net.util;

import android.text.TextUtils;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.interf.OnTcpSendMessageListner;
import com.zonkey.mobileteach_lib.util.ByteUtil;
import com.zonkey.mobileteach_lib.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by xu.wang
 * Date on 2017/6/15 11:48
 * 处理流消息的工具类
 */

public class ProcessMsgUtil {

    /**
     * 处理从socket接受到的tcp消息,并回调给listener
     *
     * @param socket
     * @param mSendMessageListner 111
     * @throws Exception
     */
    public void processAcceptConnection(Socket socket, OnTcpSendMessageListner mSendMessageListner) throws Exception {
        InputStream is = socket.getInputStream();
        byte[] bytes = readByte(is, 19);
        byte netVersion = bytes[1];
//        if (read == -1) {
//            throw new Exception("远程socket已经关闭,未收到回复信息,1,请检查主指令和子指令,2,请询问pc客户端是否有该指令的处理逻辑");
//        }
        if (netVersion == MobileTeachApi.encodeVersion2_v1) {   //网络模块v2版本
            netForV2(is, mSendMessageListner, bytes);
        } else if (netVersion == MobileTeachApi.encodeVersion2_v0) {    //网络模块v1版本
            netForV1(is, mSendMessageListner);
        } else {
            LogUtil.e("ProcessMsgUtil", "收到的消息无法解析");
        }
    }

    public byte[] getSendMsg(short mainCmd, short subCmd, int port, String sendBody) {
        return getSendMsg(mainCmd, subCmd, port, (short) 0x00, sendBody);
    }

    /**
     * 对发送消息的头部进行组装
     *
     * @param mainCmd  主指令
     * @param subCmd   子指令
     * @param sendBody 发送消息
     * @return
     */
    public byte[] getSendMsg(short mainCmd, short subCmd, int port, short userData, String sendBody) {
        byte[] temp = new byte[19];
        temp[0] = MobileTeachApi.encodeVersion1;   //编码版本1     0
        temp[1] = MobileTeachApi.encodeVersion2_v1;   //编码版本2     1
        temp[2] = MobileTeach.AppCode;   //app指令      2
        temp[3] = MobileTeachApi.MACHINE_TYPE; //机器类型            3
        System.arraycopy(ByteUtil.short2Bytes(mainCmd), 0, temp, 4, 2); //4-5   主指令
        System.arraycopy(ByteUtil.short2Bytes(subCmd), 0, temp, 6, 2);  //6-7   子指令
        System.arraycopy(ByteUtil.int2Bytes(port), 0, temp, 8, 4); //8-11 端口号 , tcp的短连接该位置写0;
        System.arraycopy(ByteUtil.int2Bytes(TextUtils.isEmpty(sendBody) ? 0 : sendBody.getBytes().length), 0, temp, 12, 4);  //12 -16位,数据长度
        System.arraycopy(ByteUtil.short2Bytes(userData), 0, temp, 16, 2);
        byte[] tempb = new byte[18];
        System.arraycopy(temp, 0, tempb, 0, 18);
        temp[18] = ByteUtil.getCheckCode(tempb);
        return temp;
    }

    //处理网络协议v1版本的指令
    private void netForV1(InputStream is, final OnTcpSendMessageListner mSendMessageListner) throws
            IOException {
        byte[] buff = new byte[1];
        is.read(buff);
        final byte appCodeCmd = buff[0];   //应用名
        buff = new byte[2];
        final byte mainCmd = buff[1];
        buff = new byte[2];
        final byte subCmd = buff[1];
        buff = new byte[2];
        int stringBodySize = ByteUtil.bytesToShort(buff);
        buff = new byte[stringBodySize];
        final byte[] bytes = readByte(is, stringBodySize);
        is.close();
        final String body = new String(buff);
        if (mSendMessageListner != null) {
            MobileTeach.handler.post(new Runnable() {
                @Override
                public void run() {
                    mSendMessageListner.success((short) (mainCmd & 0x00FF), (short) (subCmd & 0x00FF), appCodeCmd, new String(bytes));
                }
            });
        }

    }

    // 处理网络协议v2版本的指令
    private void netForV2(InputStream is, final OnTcpSendMessageListner onTcpSendMessageListner, byte[] bytes) throws IOException {
        final byte appCodeCmd = bytes[2];            //应用名   ,2
        byte[] buff = new byte[2];
        System.arraycopy(bytes, 4, buff, 0, 2);
        final short mainCmd = ByteUtil.bytesToShort(buff);       //主指令  4`5
        System.arraycopy(bytes, 6, buff, 0, 2);
        final short subCmd = ByteUtil.bytesToShort(buff);    //子指令  6`7
        buff = new byte[4];
        System.arraycopy(bytes, 8, buff, 0, 4);
        int sendPort = ByteUtil.bytesToInt(buff);           //发送端口号  8~11
        buff = new byte[4];                 //12 ~ 15;
        System.arraycopy(bytes, 12, buff, 0, 4);
        int stringBodySize = ByteUtil.bytesToInt(buff);

        buff = new byte[2 * 1024];
        int len = 0;
        int totalLen = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((len = is.read(buff)) != -1) {
            if (len != -1) totalLen += len;
            baos.write(buff, 0, len);
            if (totalLen >= stringBodySize) {
                break;
            }
        }
        final String body = baos.toString();
        baos.close();
        if (onTcpSendMessageListner != null) {
            MobileTeach.handler.post(new Runnable() {
                @Override
                public void run() {
                    onTcpSendMessageListner.success(mainCmd, subCmd, appCodeCmd, body);
                }
            });
        }
    }

    /**
     * 保证从流里读到指定长度数据
     *
     * @param is
     * @param readSize
     * @return
     * @throws Exception
     */
    private byte[] readByte(InputStream is, int readSize) throws IOException {
        byte[] buff = new byte[readSize];
        int len = 0;
        int eachLen = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (len < readSize) {
            eachLen = is.read(buff);
            if (eachLen != -1) {
                len += eachLen;
                baos.write(buff, 0, eachLen);
            } else {
                break;
            }
            if (len < readSize) {
                buff = new byte[readSize - len];
            }
        }
        byte[] b = baos.toByteArray();
        baos.close();
        return b;
    }
}

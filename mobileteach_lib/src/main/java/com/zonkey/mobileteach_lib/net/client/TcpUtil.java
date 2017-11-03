package com.zonkey.mobileteach_lib.net.client;

import android.text.TextUtils;
import android.util.Log;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.interf.OnTcpSendFileListener;
import com.zonkey.mobileteach_lib.interf.OnTcpSendMessageListner;
import com.zonkey.mobileteach_lib.net.bean.TcpResponse;
import com.zonkey.mobileteach_lib.net.util.ProcessMsgUtil;
import com.zonkey.mobileteach_lib.util.ByteUtil;
import com.zonkey.mobileteach_lib.util.ThreadPoolManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by xu.wang
 * Date on 2016/11/11 09:48
 * tcp发送消息和文件,并接受回调的方法.
 */
public class TcpUtil {
    private static TcpUtil instance;
    private String ip;
    private int port;
    private int connectTime = 2000;
    private final int maxImageSize = 1024 * 1024 * 2;  //超过这个数值的图片将执行压缩操作
    private final int cacheSize = 1024 * 3;  //发送文件一次写入数据大小
    private Socket mSocket;
    private ThreadPoolManager threadPoolManager;
    private Type mType;
    private ProcessMsgUtil processMsgUtil;

    private enum Type {
        PC, SETTING     //向pc发送,向设置ip发送
    }

    public TcpUtil() {
        mType = Type.PC;
        this.ip = MobileTeach.pc_ip;
        this.port = MobileTeach.pc_tcp_port;
        threadPoolManager = new ThreadPoolManager();
    }


    public TcpUtil(String ip, int port) {
        mType = Type.SETTING;
        this.ip = ip;
        this.port = port;
        threadPoolManager = new ThreadPoolManager();
    }

    public static TcpUtil getInstance() {
        instance = new TcpUtil();
        return instance;
    }

    public void sendMessage(short mainCmd, short subCmd, OnTcpSendMessageListner listner) {
        sendMessage(mainCmd, subCmd, (short)0x00,null, connectTime, listner);
    }

    public void sendMessage(short mainCmd, short subCmd, final String sendBody, OnTcpSendMessageListner listner) {
        sendMessage(mainCmd, subCmd,(short)0x00, sendBody, connectTime, listner);
    }

    public void sendMessage(final short mainCmd, final short subCmd, final short userData,final String sendBody, final int connectSoTime, final OnTcpSendMessageListner listner) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    initialSendMessage(mainCmd, subCmd, userData,sendBody, listner, connectSoTime);
                } catch (final Exception e) {
                    Log.e("TcpUtil", e.toString());
                    if (listner != null) {
                        MobileTeach.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listner.error(e);
                            }
                        });
                    }
                }
            }
        }.start();
    }

    public void sendFile(short mainCmd, File file, OnTcpSendFileListener listener) {
        sendFile(mainCmd, (short) 0x00, file, listener);
    }

    public void sendFile(short mainCmd, File file, final boolean isPrefx, OnTcpSendFileListener listener) {
        sendFile(mainCmd, (short) 0x00, file, isPrefx, listener);
    }

    public void sendFile(short mainCmd, short subCmd, File file, OnTcpSendFileListener listener) {
        sendFile(mainCmd, subCmd, file, false, listener);
    }

    public void sendTimeOut(int timeOut) {
        this.connectTime = timeOut;
    }

    public void sendFile(final short mainCmd, final short subCmd, final File file, final boolean isPrefx, final OnTcpSendFileListener listener) {
        threadPoolManager.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    initialSendFile(mainCmd, subCmd, file, listener, isPrefx);
                } catch (final Exception e) {
                    Log.e("TcpUtil sendFile", e.toString());
                    MobileTeach.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFailure(e);
                            }
                        }
                    });
                }
            }
        });
    }

//    public Observable<TcpResponse> sendMessageadapt(final short mainCmd, final short subCmd, final short userData, final String sendBody, final int connectTime){
//        return Observable.create(new ObservableOnSubscribe<TcpResponse>() {
//            @Override
//            public void subscribe(ObservableEmitter<TcpResponse> e) throws Exception {
//                initialSendMessage(mainCmd,subCmd,userData,sendBody,null,connectTime,e);
//            }
//        });
//    }
    private void initialSendMessage(short mainCmd, short subCmd, short userData,String sendBody, OnTcpSendMessageListner mSendMessageListner,
                                    int connectSoTime) throws Exception {
            initialSendMessage(mainCmd,subCmd,userData,sendBody,mSendMessageListner,connectSoTime,null);
    }

    /**
     * 发送消息
     *
     * @param mainCmd             主命令
     * @param subCmd              子命令
     * @param sendBody            发送消息
     * @param mSendMessageListner 消息回调监听
     * @param connectSoTime       超时时间
     * @throws Exception
     */
    private void initialSendMessage(short mainCmd, short subCmd, short userData,String sendBody, OnTcpSendMessageListner mSendMessageListner,
                                    int connectSoTime,ObservableEmitter<TcpResponse> e) throws Exception {
        if (processMsgUtil == null) {
            processMsgUtil = new ProcessMsgUtil();
        }
        mSocket = new Socket();
        mSocket.setReuseAddress(true);
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        mSocket.connect(socketAddress, connectSoTime);
        mSocket.setSoTimeout(10000);    //此方法意为tcp连接成功后is.read阻塞多长时间
        OutputStream outputStream = mSocket.getOutputStream();
        byte[] temp = processMsgUtil.getSendMsg(mainCmd, subCmd, 0,userData,sendBody);
        if (!TextUtils.isEmpty(sendBody)) {
            outputStream.write(temp);    //向os写入消息内容
            outputStream.write(sendBody.getBytes());    //向os写入消息内容
        } else {
            outputStream.write(temp);
        }
        processMsgUtil.processAcceptConnection(mSocket, mSendMessageListner); // 接收服务器端数据,并回调listener
        mSocket.close();
    }

    public void closeSocket() {
        if (mSocket != null && (!mSocket.isClosed())) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 第0个字节(约定指令),1到4为文件大小(int 转byte),之后是文件内容
     *
     * @param mainCmd  主指令
     * @param subCmd   子指令
     * @param file     文件
     * @param listener 发送文件的回调监听
     * @param isPrefx  是否添加前面的前缀
     * @throws IOException
     */
    private void initialSendFile(short mainCmd, short subCmd, final File file, final OnTcpSendFileListener listener, boolean isPrefx) throws IOException {
        if (mType == Type.PC) {
            port = MobileTeach.pc_file_port;
        }
        Socket socket = new Socket(ip, port);
        if (!file.exists()) {
            Log.e("TcpUtil", "发送文件不存在");
            return;
        }
        //2.创建FileInputStream从文件中读取数据, 写出到Socket的输出流
        FileInputStream fis = new FileInputStream(file);
        final long fileSize = file.length();
        OutputStream out = socket.getOutputStream();
        byte[] buffer = new byte[cacheSize];
        int len;
        byte[] bytes;
        if (isPrefx) {
            bytes = new String(("\\image\\" + file.getName()).getBytes(), "UTF-8").getBytes();
        } else {
            bytes = new String((file.getName()).getBytes(), "UTF-8").getBytes();
        }
        //3.将指令和文件大小发送到服务端
        out.write(MobileTeachApi.TransferVersion_v1);          //0 第几版通信协议
        out.write(MobileTeach.AppCode);          //1 发送者的applicationCode
        out.write(MobileTeachApi.MACHINE_TYPE);          // 2  发送者的machineCode
        out.write(ByteUtil.short2Bytes(mainCmd));                    //3 - 4 主指令
        out.write(ByteUtil.short2Bytes(subCmd));                      //5 - 6 子指令
        out.write(ByteUtil.int2Bytes(bytes.length));         //写入文件名字数组的大小 7 - 10
        out.write(ByteUtil.int2Bytes((int) file.length()));            //写入文件大小 11 - 15
        out.write(bytes);                            //文件名字的byte数组

        long sum = 0;
        while ((len = fis.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            sum += len;
            //发送消息
            final float tempSum = sum;
            MobileTeach.handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        if (fileSize > 0) {
                            listener.onProgress(fileSize, (int) (tempSum * 100 / fileSize), file.getName());
                        } else {
                            listener.onProgress(0, 1, file.getName());
                        }
                    }
                }
            });
        }
        out.flush();
        fis.close();
        out.close();
        socket.close();
        MobileTeach.handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.Success();
                }
            }
        });
    }
}

package com.zonkey.mobileteach_lib.net;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.net.server.FileServer;
import com.zonkey.mobileteach_lib.net.server.TcpServer;
import com.zonkey.mobileteach_lib.net.server.UdpServer;
import com.zonkey.mobileteach_lib.ui.activity.ExitActivity;
import com.zonkey.mobileteach_lib.util.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/4/10 18:23
 */
public class AcceptMsgManager {
    private UdpServer udpServer;
    private FileServer fileServer;
    private final int UDP = SocketConstant.UDP_COMMAND;
    private final int TCP = SocketConstant.TCP_COMMAND;
    private final int FILE = SocketConstant.FILE_COMMAND;
    private final int APP_QUIT = SocketConstant.APP_QUIT_LOGIN_COMMAND;
    public boolean isStop = false;
    private static AcceptMsgManager mInstance;
    private TcpServer tcpServer;

    private AcceptMsgManager() {
    }

    public static AcceptMsgManager getInstance() {
        if (mInstance == null) {
            mInstance = new AcceptMsgManager();
        }
        return mInstance;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UDP:
                    ReceiverInfo udpInfo = (ReceiverInfo) msg.obj;
                    if ((byte) udpInfo.getMainCmd() == MobileTeachApi.UserControll.Main_Cmd) {
                        if ((byte) udpInfo.getSubCmd() == MobileTeachApi.UserControll.Command_Logout) {
                            udpServer.stopHeartBeat();
                            quitApp("电脑端强制断开连接");
                            return;
                        } else if ((byte) udpInfo.getSubCmd() == MobileTeachApi.UserControll.Response_NeedLogin) {
                            udpServer.stopHeartBeat();
                            quitApp("登录状态失效");
                            return;
                        }
                    }
                    receiverMsg(udpInfo);
                    break;
                case FILE:
                    ReceiverInfo file_info = (ReceiverInfo) msg.obj;
                    receiverFile(file_info);
                    break;
                case TCP:
                    ReceiverInfo tcp_info = (ReceiverInfo) msg.obj;
                    receiverMsg(tcp_info);
                    break;
                case APP_QUIT:
                    udpServer.stopHeartBeat();
                    Toast.makeText(MobileTeach.AppContext, "电脑端长期无响应", Toast.LENGTH_SHORT).show();
                    quitApp("电脑端长期无响应");
                    break;
            }
        }
    };

    //收到文件
    private void receiverFile(ReceiverInfo file_info) {
        EventBus.getDefault().post(file_info);
    }

    //收到消息
    private void receiverMsg(ReceiverInfo udpInfo) {
        EventBus.getDefault().post(udpInfo);
    }

    private void quitApp(String detail) {
        if (!isStop) {
            isStop = true;  //只执行一次或者设置activity为singleTop;
            LogUtil.writeLog("MobileTeach", "AcceptManager 退出app" + detail);
            if (MobileTeach.mListener != null) {
                MobileTeach.mListener.quit(detail);
                Intent intent = new Intent(MobileTeach.AppContext, ExitActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("qpp_quit", detail);
                MobileTeach.AppContext.startActivity(intent);
            }
        }
    }

    public void startServer() {
        udpServer = new UdpServer(mHandler);
        tcpServer = new TcpServer(mHandler);
        fileServer = new FileServer(mHandler);
        udpServer.startServer();
        tcpServer.startServer();
        fileServer.startServer();
    }

    public void startHeartBeat() {
        udpServer.startHeartBeat();
    }

    public void stopHeartBeat() {
        udpServer.stopHeartBeat();
    }

    public void stopServer() {
        if (udpServer != null) udpServer.closeSocket();
        if (fileServer != null) fileServer.closeServer();
        if (tcpServer != null) tcpServer.closeServer();
        LogUtil.writeLog("MobileTeach_lib", "AcceptMsgManager close server");
    }

}

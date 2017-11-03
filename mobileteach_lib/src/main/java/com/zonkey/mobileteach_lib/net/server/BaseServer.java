package com.zonkey.mobileteach_lib.net.server;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zonkey.mobileteach_lib.net.bean.ReceiverAddress;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.net.util.ProcessMsgUtil;
import com.zonkey.mobileteach_lib.util.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xu.wang
 * Date on 2017/6/15 11:37
 */

public abstract class BaseServer {
    protected String TAG = this.getClass().getSimpleName().toString();
    protected int cacheSize = 4 * 1024;
    private Handler mHandler;
    private boolean isAccept = true;
    private ServerSocket serverSocket;
    private boolean isDebug = false;
    protected int port;
    protected ProcessMsgUtil processMsgUtil;

    public BaseServer(Handler mHandler) {
        this.mHandler = mHandler;
        processMsgUtil = new ProcessMsgUtil();
    }

    public void startServer() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                beginServer();
            }
        }.start();
    }

    private void beginServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
            if (this instanceof TcpServer) {
                LogUtil.writeLog("TcpServer", "tcp server start port" + port);
            } else {
                LogUtil.writeLog("FileServer", "file server start port" + port);
            }
            showLog("begin server localPort" + port);
            while (isAccept) {
                if (serverSocket.isClosed()) break;
                Socket socket = serverSocket.accept();
                if (socket.isClosed() || !socket.isConnected()) continue;
                WorkThread thread = new WorkThread(socket);  //接受消息
                thread.start();
            }
        } catch (IOException e1) {
            Log.e(TAG, "beginServer" + e1.toString());
        }
    }

    protected void showLog(String msg) {
        if (!isDebug) return;
        Log.e(TAG, msg);
    }

    class WorkThread extends Thread {
        Socket socket;

        public WorkThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                acceptSocketMsg(socket);
            } catch (Exception e) {
                showLog("接受文件/消息异常" + e.toString());
                LogUtil.writeLog(TAG, "接受文件/消息异常" + e.toString());
            }
        }
    }

    /**
     * 向handler发送收到的tcp,file消息
     *
     * @param mainCmd         主指令
     * @param subCmd          子指令
     * @param appCodeCmd      appCode
     * @param path            消息内容,如果是文件则是文件的路径
     * @param receiverAddress 地址对象
     * @param what            handler类型
     */
    protected void sendData2Manager(short mainCmd, short subCmd, byte appCodeCmd, String path, ReceiverAddress receiverAddress, int what) {
        ReceiverInfo receiverInfo = new ReceiverInfo(mainCmd, subCmd, appCodeCmd, path
                , receiverAddress);
        Message msg = new Message();
        msg.what = what;
        msg.obj = receiverInfo;
        mHandler.sendMessage(msg);
    }

    protected abstract void acceptSocketMsg(Socket socket);

    public void closeServer() {
        isAccept = false;
        closeSocket();
    }

    //socket.close()操作并不能立即释放绑定的端口,而是把端口设置为TIME_WAIT状态,过段时间(默认240s)
    private void closeSocket() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                        serverSocket = null;
                        showLog("line 131 socket成功关闭");
                    } catch (IOException e) {
                        showLog("line 133" + e.toString());
                    }
                }
            }
        }.start();
    }
}

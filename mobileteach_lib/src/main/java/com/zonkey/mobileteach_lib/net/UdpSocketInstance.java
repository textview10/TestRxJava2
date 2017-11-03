package com.zonkey.mobileteach_lib.net;

import android.util.Log;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.util.LogUtil;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by chengaihua on 2017/8/17.
 */

public class UdpSocketInstance {

    private static UdpSocketInstance instance;

    private DatagramSocket datagramSocket;

    public UdpSocketInstance() {
        if (datagramSocket == null) {
            try {
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.bind(new InetSocketAddress(MobileTeach.local_udp_port));
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtil.e("UdpSocketInstance","" + e.toString());
            }
        }

    }

    public synchronized static UdpSocketInstance getInstance() {
        if (instance == null)
            instance = new UdpSocketInstance();

        return instance;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void closeDatagramSocket() {
        new Thread() {
            @Override
            public void run() {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    Log.e("UdpSocketInstance", "UdpServer关闭");
                    datagramSocket.close();
                }
                datagramSocket.close();
            }
        }.start();
    }
}

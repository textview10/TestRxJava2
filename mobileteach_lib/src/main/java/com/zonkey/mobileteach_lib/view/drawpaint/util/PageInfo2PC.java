package com.zonkey.mobileteach_lib.view.drawpaint.util;

import android.text.TextUtils;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.MobileTeachConfig;
import com.zonkey.mobileteach_lib.net.client.UdpUtil;
import com.zonkey.mobileteach_lib.util.BitmapUtil;
import com.zonkey.mobileteach_lib.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengaihua on 2017/7/7.
 */

public class PageInfo2PC {

    private UdpUtil udpUtil;
    private boolean isSend = true;  //是否将点发送到pc端,默认发送
    private ArrayList<String> mLists;

    private boolean isMiniBorad = false;     //是否是Mini黑板模式
    private String miniBoradId;
    private String pageId;
    private int sendPort;

    public PageInfo2PC() {
        udpUtil = new UdpUtil();
    }

    public void setSendIps(List<String> lists) {
        if (lists == null || lists.size() <= 0) {
            isSend = false;
            return;
        }
        isSend = true;
        mLists = new ArrayList<>();
        mLists.clear();
        mLists.addAll(lists);
        if (MobileTeach.CurrentApp == MobileTeach.App_MobileTeach_Student) {
            sendPort = MobileTeachConfig.teacher_udp_port;
        } else if (MobileTeach.CurrentApp == MobileTeach.App_MobileTeach_Teacher) {
            sendPort = MobileTeachConfig.stu_udp_port;
        }

    }

    //设置显示多张图片
    public void sendShowAllPic(ArrayList<String> lists) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lists.size(); i++) {
            int bitmapDegree = BitmapUtil.getBitmapDegree(lists.get(i));
            sb.append(new File(lists.get(i)).getName());
            if (bitmapDegree != 0) {
                sb.append("?r=").append(bitmapDegree).append("|");
            }
            if (i != lists.size() - 1) {
                sb.append("|");
            }
        }
        LogUtil.writeLog("显示多张图片", sb.toString());
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.REQUEST_PLAY, sb.toString());
    }

    /**
     * 发送消息
     *
     * @param mainCmd
     * @param subCmd
     * @param sendBody
     */
    private void sendMsg(short mainCmd, short subCmd, String sendBody) {
        if (mLists != null && mLists.size() > 0) {
            for (int i = 0; i < mLists.size(); i++) {
                UdpUtil tempUdpUtil;
                if (TextUtils.equals(mLists.get(i), MobileTeach.pc_ip)) {
                    sendMsgToPc(mainCmd, subCmd, sendBody);
                } else {
                    tempUdpUtil = new UdpUtil(mLists.get(i), sendPort);
                    tempUdpUtil.sendMessage(mainCmd, subCmd, sendBody);
                }
            }
        } else {
            sendMsgToPc(mainCmd, subCmd, sendBody);
        }
    }

    private void sendMsgToPc(short mainCmd, short subCmd, String sendBody) {
        if (isMiniBorad) {
            udpUtil.sendMessage(MobileTeachApi.MiniBoardDrawing.Main_Cmd, subCmd,
                    sendBody + "|" + miniBoradId + "|" + pageId);
        } else {
            udpUtil.sendMessage(mainCmd, subCmd, sendBody);
        }
    }

}

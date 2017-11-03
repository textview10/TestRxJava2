package com.zonkey.mobileteach_lib.view.drawpaint.util;

import android.text.TextUtils;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.MobileTeachConfig;
import com.zonkey.mobileteach_lib.net.client.UdpUtil;
import com.zonkey.mobileteach_lib.util.BitmapUtil;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xu.wang
 * Date on 2017/1/19 14:02
 * 向pc端发送点和线信息的类
 */
public class PaintInfo2Pc {
    private UdpUtil udpUtil;
    private boolean isSend = true;  //是否将点发送到pc端,默认发送
    private ArrayList<String> mLists;
    //------------------Mini黑板需要的变量----------------
    private boolean isMiniBorad = false;     //是否是Mini黑板模式
    private String mTeacherIp;
    private String miniBoradId;
    private String pageId;
    private boolean isSharePic = false;
    private String sharePicId;

    public PaintInfo2Pc() {
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
    }

    public void setMiniBoradState(String miniBoradId, String pageId) {
        isMiniBorad = true;
        this.miniBoradId = miniBoradId;
        this.pageId = pageId;
    }

    public void setSharePicStatus(String sharePicId, String pageId) {
        isSharePic = true;
        this.sharePicId = sharePicId;
        this.pageId = pageId;
    }

    public void quitMiniBoradState() {
        isMiniBorad = false;
    }

    /**
     * @param lineId           线条id
     * @param type             线条类型
     * @param currntColor      线条颜色
     * @param currentPaintSize 线条宽度
     * @param downX
     * @param downY
     */
    public void sendDownInfo(String lineId, String type, int currntColor, int currentPaintSize, float downX, float downY) {
        if (!isSend) {
            return;
        }
        StringBuilder sendBody = new StringBuilder();

        sendBody.append(type).append("|")
                .append(lineId).append("|")
                .append(ColorUtil.convertColorToString(currntColor)).append("|")
                .append(currentPaintSize).append("|").append(downX)
                .append(",").append((downY));
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.REQUEST_NEW_SHAPE, sendBody.toString());
    }

    public void sendUpInfo(String lineId) {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.Request_EndDrawing, lineId);
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
                } else if (TextUtils.equals(mLists.get(i), mTeacherIp)) {
                    tempUdpUtil = new UdpUtil(mLists.get(i), MobileTeachConfig.teacher_udp_port);
                    tempUdpUtil.sendMessage(mainCmd, subCmd, sendBody);
                } else {
                    tempUdpUtil = new UdpUtil(mLists.get(i), MobileTeachConfig.stu_udp_port);
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
            return;
        }

        // TODO: 2017/9/4 CAH
        if (isSharePic) {
            return;
        }


        udpUtil.sendMessage(mainCmd, subCmd, sendBody);

    }

    /**
     * 智课黑板的发送点的方法
     * 手势移动时发送绘制点的方法
     *
     * @param lineId       绘制线条的id
     * @param currentPoint 当前线条的第几个点
     * @param moveX        移动到点x坐标
     * @param moveY        移动到点y坐标
     */
    public void sendMoveInfo(String lineId, int currentPoint, float moveX, float moveY) {
        if (!isSend) {
            return;
        }
        StringBuilder sendBody = new StringBuilder();
        sendBody.append(lineId).append("|")
                .append(currentPoint).append("|").
                append(moveX + "," + moveY);
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.REQUEST_ADD_POINT, sendBody.toString());
    }

    /**
     * 智课助手的发送点方法
     * @param lineId       绘制线条的id
     * @param currentPoint 当前线条的第几个点
     * @param startX       开始点x坐标
     * @param startY       开始点y坐标
     * @param controllX    控制点x坐标
     * @param controllY    控制点y坐标
     */
    public void sendMoveInfo(String lineId, int currentPoint, float startX, float startY, float controllX, float controllY) {
        if (!isSend) {
            return;
        }
        StringBuilder sendBody = new StringBuilder();
        sendBody.append(lineId).append("|")
                .append(currentPoint).append("|").
                append(startX + "," + startY)
                .append("|").append(controllX + "," + controllY);
        udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.REQUEST_ADD_POINT, sendBody.toString());
        LogUtil.writeLog("PaintInfo2Pc", "手指移动" + sendBody.toString());
    }

    public void setMoveShape(String lineId, int currentPoint, float mX, float mY) {
        if (!isSend) {
            return;
        }
        StringBuilder sendBody = new StringBuilder();
        sendBody.append(lineId).append("|")
                .append(currentPoint).append("|").
                append(mX + "," + mY);
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.REQUEST_ADD_POINT, sendBody.toString());
    }

    /**
     * 清除画板
     */
    public void sendMoveAllPath() {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_Clear, null);
    }

    /**
     * 清除若干条线
     */
    public void sendDeleteShape(ArrayList<LineInfo> lists) {
        if (lists == null) {
            return;
        }
        StringBuffer sendBody = new StringBuffer();
        for (int i = 0; i < lists.size(); i++) {
            if (i == lists.size() - 1) {
                sendBody.append(lists.get(i).getLineId());
            } else {
                sendBody.append(lists.get(i).getLineId()).append(",");
            }
        }
        sendDeleteShape(sendBody.toString());
    }

    public void sendDeleteShape(String shapeId) {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_Clear, shapeId);
    }

    public void sendUndoInfo(String shapeId) {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_Undo, shapeId);
    }

    public void sendRedoInfo(String shapeId) {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.Request_Redo, shapeId);
    }

    public void sendTransLateInfo(float disX, float disY) {
        if (!isSend) {
            return;
        }
        StringBuffer sb_temp = new StringBuffer();
        sb_temp.append(disX).append(",").append(disY);
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_Translate,
                sb_temp.toString());
    }

    public void sendScaleInfo(float scale, float focusX, float focusY) {
        if (!isSend) {
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(scale).append("|").
                append(focusX).append(",").append(focusY);
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_Scale, stringBuffer.toString());
    }

    public void setTeacherIp(String teacherIp) {
        this.mTeacherIp = teacherIp;
    }

    public void sendRotateInfo(int rotate) {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_Rotate, rotate + "");
    }

    public void clearMatrix() {
        if (!isSend) {
            return;
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_ResetTransform, null);
    }

    //根据当前集合的path.
    public void sendShowPic2Pc(String path) {
        StringBuilder sb = new StringBuilder();
        int bitmapDegree = BitmapUtil.getBitmapDegree(path);
        sb.append(new File(path).getName());
        if (bitmapDegree != 0) {
            sb.append("?r=").append(bitmapDegree);
        }
        sendMsg(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.REQUEST_PLAY, sb.toString());
        LogUtil.writeLog("显示单张图片", sb.toString());
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

    //设置显示多张图片
    public void sendShowHost(ArrayList<String> lists) {
        if (!isSend) {
            return;
        }
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
        udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PicHost.Request_HostBegin, sb.toString());
    }
}

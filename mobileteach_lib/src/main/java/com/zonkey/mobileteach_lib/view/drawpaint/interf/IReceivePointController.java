package com.zonkey.mobileteach_lib.view.drawpaint.interf;

import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.util.TransPointF;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/9/7 11:11
 */

public interface IReceivePointController {
    void newShape(ReceiverInfo receiverInfo, TransPointF transPointF);   //增加新图形

    void addPoint(ReceiverInfo receiverInfo, TransPointF transPointF);   //追加点

    void endDrawing(ReceiverInfo receiverInfo, TransPointF transPointF); //结束一笔绘制

    ArrayList<LineInfo> getReceiveList(); //获得接收的远程缓存区里的全部信息
}

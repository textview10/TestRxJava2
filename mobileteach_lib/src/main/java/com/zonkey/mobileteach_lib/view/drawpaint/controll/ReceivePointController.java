package com.zonkey.mobileteach_lib.view.drawpaint.controll;

import android.graphics.PointF;
import android.text.TextUtils;

import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PointInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.IReceivePointController;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.OnRefreshListner;
import com.zonkey.mobileteach_lib.view.drawpaint.util.ColorUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.util.TransPointF;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by xu.wang
 * Date on 2017/9/7 11:02
 * 控制接收远程的点
 */

public class ReceivePointController implements IReceivePointController {
    private ArrayList<LineInfo> mReceiveLists = new ArrayList<>();   //收到远程的lineInfo
    private ArrayList<LineInfo> mPaintLists;
    private OnRefreshListner mListener;

    public ReceivePointController(ArrayList<LineInfo> lists, OnRefreshListner listener) {
        this.mPaintLists = lists;
        this.mListener = listener;
        Collections.synchronizedList(mReceiveLists);
    }


    //接收增加新的LineInfo指令...
    @Override
    public void newShape(ReceiverInfo receiverInfo, TransPointF transPointF) {
        String[] splits = receiverInfo.getData().split("\\|");
        if (splits.length != 5) {
            LogUtil.e("PaintImageView", "收到增加新图形的数据类型不正确" + receiverInfo.getData());
            return;
        }
        LineInfo lineInfo = new LineInfo();
        lineInfo.setLineId(splits[1]);      //     直线的id
        lineInfo.setColor(ColorUtil.convertStringToColor(splits[2]));  //颜色
        lineInfo.setStrokeWidth(Integer.parseInt(splits[3]));   //线的粗细
        String[] split_temp = splits[4].split(",");
        if (split_temp.length != 2) {
            LogUtil.writeLog("PaintImageView", "accept first PointF point x y error");
            return;
        }
        lineInfo.getCurrentPointLists().add(0, new PointInfo(
                transPointF.remote2Local(new PointF(Float.parseFloat(split_temp[0]), Float.parseFloat(split_temp[1]))), -1));

        switch (splits[0]) { //第一项数据类型
            case MobileTeachApi.PresentControl.QuadraticBezier:
                lineInfo.setType(0);            //增加贝塞尔
                break;
            case MobileTeachApi.PresentControl.PAINT_TIANZIGE:
                lineInfo.setType(5);            //增加田字格
                break;
            case MobileTeachApi.PresentControl.PAINT_MIZIGE:
                lineInfo.setType(6);            //增加米字格
                break;
            case MobileTeachApi.PresentControl.PAINT_SiXianGE:
                lineInfo.setType(7);            //增加类型
                break;
        }
        LogUtil.writeLog("PaintImageView", "NewShape " + receiverInfo.getData());
        mReceiveLists.add(lineInfo);
    }

    //往LineInfo增加新的点...
    @Override
    public void addPoint(ReceiverInfo receiverInfo, TransPointF transPointF) {
        if (mReceiveLists.size() < 1) {
            LogUtil.writeLog("PaintImageView Add Point", "数据总量不正确" + receiverInfo.getData());
            return;
        }
        String[] splits = receiverInfo.getData().split("\\|");
        if (splits.length != 3) {
            LogUtil.writeLog("PaintImageView Add Point", "收到追加点的信息不正确");
            return;
        }
        //遍历查看集合中有没有这个lineinfo的id,没有就扔掉这个点
        int index = -1;
        int type = -1;
        for (int i = 0; i < mReceiveLists.size(); i++) {
            if (TextUtils.equals(mReceiveLists.get(i).getLineId(), splits[0])) {
                index = i;
                type = mReceiveLists.get(i).getType();
                break;
            }
        }
        if (index == -1) {
            LogUtil.writeLog("PaintImageView Add Point", "接受点的list中没有这个id" + splits[0]);
            return;
        }

        String[] controlP = splits[2].split(",");
        if (controlP.length != 2) {
            LogUtil.writeLog("PaintImageView Add Point", "收到追加点的信息不正确");
            return;
        }
        switch (type) {
            case 5:
            case 6:
            case 7:
                float[] tempF = {Float.parseFloat(controlP[0]), Float.parseFloat(controlP[1])};
                LineInfo lineInfo = mReceiveLists.get(index);
                if (lineInfo.getCurrentPointLists().size() >= 2) {
                    lineInfo.getCurrentPointLists().set(1, new PointInfo(new PointF(tempF[0], tempF[1]), 1));
                } else {
                    //lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), 0));
                    lineInfo.getCurrentPointLists().add(1, new PointInfo(new PointF(tempF[0], tempF[1]), 1));
                }
                break;
            case 0:
                LineInfo tempInfo = mReceiveLists.get(index);
                int indexId = Integer.parseInt(splits[1]);
                PointInfo latestPoint = tempInfo.getCurrentPointLists().get(tempInfo.getCurrentPointLists().size() - 1);
                if (latestPoint.getIndex() < indexId) {
                    tempInfo.getCurrentPointLists().add(new PointInfo(
                            transPointF.remote2Local(new PointF(Float.parseFloat(controlP[0]), Float.parseFloat(controlP[1]))), indexId));
                    LogUtil.writeLog("PaintImageView", "AddPoint success" + receiverInfo.getData());
                } else if (latestPoint.getIndex() > indexId) {
                    boolean isExist = false;
                    for (int i = tempInfo.getCurrentPointLists().size() - 1; i > 0; i--) {
                        PointInfo pointInfo = tempInfo.getCurrentPointLists().get(i);
                        if (pointInfo.getIndex() < indexId) {
                            tempInfo.getCurrentPointLists().add(i + 1, new PointInfo(
                                    transPointF.remote2Local(new PointF(Float.parseFloat(controlP[0]), Float.parseFloat(controlP[1]))),
                                    i + 1));
                            isExist = true;
                            LogUtil.writeLog("PaintImageView", "AddPoint error sort and insert pos" + (i + 1) + "  " + receiverInfo.getData());
                            break;
                        }
                    }
                    if (!isExist) {
                        tempInfo.getCurrentPointLists().add(0, new PointInfo(
                                transPointF.remote2Local(new PointF(Float.parseFloat(controlP[0]), Float.parseFloat(controlP[1]))),
                                0));
                        LogUtil.writeLog("PaintImageView", "AddPoint sort and insert pos" + 0 + "  " + receiverInfo.getData());
                    }
                } else {
                    LogUtil.writeLog("PaintImageView", "收到重复点,不处理");
                }
                break;
        }
        mListener.refresh(false);

    }


    //结束LineInfo的绘制.
    @Override
    public void endDrawing(ReceiverInfo receiverInfo, TransPointF transPointF) {
        String lineId = receiverInfo.getData();
        for (int i = 0; i < mReceiveLists.size(); i++) {
            if (TextUtils.equals(lineId, mReceiveLists.get(i).getLineId())) {
                mPaintLists.add(mReceiveLists.get(i));  //向当前记录LineInfo的lists里记录当前一条线的信息
                mReceiveLists.remove(i);    //从receivereLists的缓存区中删除.
                break;
            }
        }
        mListener.refresh(true);
    }

    @Override
    public ArrayList<LineInfo> getReceiveList() {
        return mReceiveLists;
    }

}

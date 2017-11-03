package com.zonkey.mobileteach_lib.view.drawpaint;

import android.graphics.Color;
import android.graphics.PointF;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.ActionLineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LogicalType;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PageInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PointInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.RemoteDocment;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.RemoteMiniBorad;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.RemotePage;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.RemotePoint;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.RemoteShape;
import com.zonkey.mobileteach_lib.view.drawpaint.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xu.wang
 * Date on 2017/6/2 12:01
 */

public class RemoteInfoToLocal {

    /**
     * 将远程的点转化成本地的点
     *
     * @param remotePages
     * @return
     */
    public static ArrayList<PageInfo> remotepage2PageInfo(List<RemotePage> remotePages) {
        ArrayList<PageInfo> lists = new ArrayList<>();
        for (int i = 0; i < remotePages.size(); i++) {
            ArrayList<LineInfo> lineInfos = new ArrayList<>();
            ArrayList<RemoteShape> shapes = remotePages.get(i).getShapes();
            for (int j = 0; j < shapes.size(); j++) {
                RemoteShape remoteShape = shapes.get(j);
                LineInfo lineInfo = new LineInfo();
                lineInfo.setLineId(remoteShape.getId());
                if (remoteShape.isIsDelete()) {
                    lineInfo.setIsDelete(1);
                } else {
                    lineInfo.setIsDelete(0);
                }
                lineInfo.setColor(Color.parseColor(remoteShape.getBorderColor()));
                lineInfo.setStrokeWidth((int) remoteShape.getBorderWidth());
                if (remoteShape.getLogicalType() == LogicalType.TianZiGe) {
                    lineInfo.setType(5);

                    String[] startP = remoteShape.getStartPoint().split(",");
                    String[] endP = remoteShape.getEndPoint().split(",");

                    lineInfo.getCurrentPointLists().add(0, new PointInfo(
                            new PointF(Float.parseFloat(startP[0]), Float.parseFloat(startP[1])), 0));
                    lineInfo.getCurrentPointLists().add(1, new PointInfo(
                            new PointF(Float.parseFloat(endP[0]), Float.parseFloat(endP[1])), 1));
                } else if (remoteShape.getLogicalType() == LogicalType.SiXianGe) {
                    lineInfo.setType(7);

                    String[] startP = remoteShape.getStartPoint().split(",");
                    String[] endP = remoteShape.getEndPoint().split(",");

                    lineInfo.getCurrentPointLists().add(0, new PointInfo(
                            new PointF(Float.parseFloat(startP[0]), Float.parseFloat(startP[1])), 0));
                    lineInfo.getCurrentPointLists().add(1, new PointInfo(
                            new PointF(Float.parseFloat(endP[0]), Float.parseFloat(endP[1])), 1));
                } else if (remoteShape.getLogicalType() == LogicalType.MiZiGe) {
                    lineInfo.setType(6);

                    String[] startP = remoteShape.getStartPoint().split(",");
                    String[] endP = remoteShape.getEndPoint().split(",");

                    lineInfo.getCurrentPointLists().add(0, new PointInfo(
                            new PointF(Float.parseFloat(startP[0]), Float.parseFloat(startP[1])), 0));
                    lineInfo.getCurrentPointLists().add(1, new PointInfo(
                            new PointF(Float.parseFloat(endP[0]), Float.parseFloat(endP[1])), 1));
                } else if (remoteShape.getLogicalType() == LogicalType.PolyLine) {  //折线
                    lineInfo.setType(8);

                    ArrayList<RemotePoint> points = remoteShape.getPoints();
                    for (int k = 0; k < points.size(); k++) {
                        RemotePoint remotePoint = points.get(k);
                        String[] split = remotePoint.getPoint().split(",");
                        if (k == 0) {   //第一个点
                            PointInfo pointInfo = new PointInfo(new PointF(
                                    Float.parseFloat(split[0]), Float.parseFloat(split[1])), k);
                            lineInfo.getCurrentPointLists().add(k, pointInfo);
                        } else {
                            PointInfo pointInfo = new PointInfo(new PointF(
                                    Float.parseFloat(split[0]), Float.parseFloat(split[1])), k);
                            lineInfo.getCurrentPointLists().add(pointInfo);
                        }
                    }
                } else if (remoteShape.getLogicalType() == LogicalType.QuadraticBezier) {
                    lineInfo.setType(0);

                    ArrayList<RemotePoint> points = remoteShape.getPoints();
                    for (int k = 0; k < points.size(); k++) {
                        String[] curP = points.get(k).getPoint().split(",");
                        if (curP.length != 2) {
                            LogUtil.e("RemoteInfoToLocal", "split.length  不为2" + points.get(k).getPoint());
                            continue;
                        }
                        float x = Float.parseFloat(curP[0]);
                        float y = Float.parseFloat(curP[1]);
                        PointInfo pointInfo = new PointInfo(
                                new PointF(x, y), k);
                        lineInfo.getCurrentPointLists().add(pointInfo);
                    }
                } else {
                    LogUtil.writeLog("RemoteInfoTolocal", "存在无法解析的线条类型" + remoteShape.getId() + "类型" + remoteShape.getLogicalType());
                }
                lineInfos.add(lineInfo);
            }
            PageInfo pageInfo = new PageInfo(i, lineInfos, null, remotePages.get(i).getId(),new ArrayList<ActionLineInfo>(),new ArrayList<ActionLineInfo>());
            lists.add(pageInfo);
        }
        return lists;
    }

    /**
     * 将本地的点转化为pc需要的json信息
     *
     * @param pageInfos
     * @param id        id 不能为空
     * @param fileName  fileName不能为空
     * @return
     */
    public static String getRemoteListString(ArrayList<PageInfo> pageInfos, String id, String fileName) {
        if (TextUtils.isEmpty(id) || fileName == null) {    //pc端要求id必须有值, fileName不能为null
            LogUtil.e("RemoteInfoToLocal", "id或者fileName 不能为空");
            return null;
        }
        ArrayList<RemotePage> remoteList = getRemotePageList(pageInfos);
        if (remoteList == null || remoteList.size() == 0) {
            return "";
        }
        RemoteDocment remoteDocment = new RemoteDocment();
        remoteDocment.setPages(remoteList);
        remoteDocment.setCount(0);
        RemoteMiniBorad remoteMiniBorad = new RemoteMiniBorad();
        remoteMiniBorad.setId(id);
        remoteMiniBorad.setFileName(fileName);
        remoteMiniBorad.setContentDocument(remoteDocment);
        return JSONObject.toJSON(remoteMiniBorad).toString();
    }

    /**
     * * 将本地的点转化为pc需要的RemotePage信息
     *
     * @param pageInfos
     * @return
     */
    public static ArrayList<RemotePage> getRemotePageList(ArrayList<PageInfo> pageInfos) {
        if (pageInfos == null || pageInfos.size() == 0) {
            LogUtil.e("RemoteInfoToLocal", "传入信息不正确");
            return null;
        }
        ArrayList<RemotePage> lists = new ArrayList<>();
        for (int i = 0; i < pageInfos.size(); i++) {
            RemotePage remotePage = pageInfo2RemotePage(pageInfos.get(i));
            lists.add(remotePage);
        }

        return lists;
    }

    /**
     * 获得某一页的json
     *
     * @param pageInfo
     * @return
     */
    public static String getRemotePageString(PageInfo pageInfo) {
        return JSONObject.toJSON(pageInfo2RemotePage(pageInfo)).toString();
    }

    /**
     * * 将本地的一页批注信息转化为pc需要的RemotePage信息
     *
     * @param pageInfo
     * @return
     */
    public static RemotePage pageInfo2RemotePage(PageInfo pageInfo) {
        RemotePage remotePage = new RemotePage();
        remotePage.setId(pageInfo.getId());
        ArrayList<RemoteShape> shapeLists = new ArrayList<>();
        for (int j = 0; j < pageInfo.getLineInfos().size(); j++) {
            LineInfo lineInfo = pageInfo.getLineInfos().get(j);
            RemoteShape remoteShape = new RemoteShape();
            remoteShape.setId(lineInfo.getLineId());
            remoteShape.setBorderColor(ColorUtil.convertColorToRemote(lineInfo.getColor()));
            remoteShape.setBorderWidth(lineInfo.getStrokeWidth());
            if (lineInfo.getIsDelete() == 1) {
                remoteShape.setIsDelete(true);
            } else {
                remoteShape.setIsDelete(false);
            }
            if (lineInfo.getCurrentPointLists().size() < 2) {
                continue;
            }
            switch (lineInfo.getType()) {

                case 0: //贝塞尔曲线
                    ArrayList<RemotePoint> remotePoints = new ArrayList<>();
                    remoteShape.setLogicalType(LogicalType.QuadraticBezier);
                    int id = 0;
                    for (int k = 0; k < lineInfo.getCurrentPointLists().size(); k++) {
                        PointInfo pointInfo = lineInfo.getCurrentPointLists().get(k);
                        RemotePoint remotePoint = new RemotePoint();
                        StringBuffer sb = new StringBuffer();
                        sb.append(pointInfo.getPointF().x).append(",").append(pointInfo.getPointF().y);
                        remotePoint.setId(id);
                        remotePoint.setPoint(sb.toString());
                        remotePoints.add(remotePoint);
                        id++;
                    }
                    remoteShape.setPoints(remotePoints);
                    break;
                case 5: //田字格
                    remoteShape.setLogicalType(LogicalType.TianZiGe);
                    break;
                case 6: //米字格
                    remoteShape.setLogicalType(LogicalType.MiZiGe);
                    break;
                case 7: //四线格
                    remoteShape.setLogicalType(LogicalType.SiXianGe);
                    break;
                case 8: //直线
                    remoteShape.setLogicalType(LogicalType.PolyLine);
                    break;
            }
            PointF startPoint = lineInfo.getCurrentPointLists().get(0).getPointF();
            remoteShape.setStartPoint(new StringBuffer().append(startPoint.x)
                    .append(",").append(startPoint.y).toString());
            PointF endPoint = lineInfo.getCurrentPointLists().get(lineInfo.getCurrentPointLists().size() - 1).getPointF();
            remoteShape.setEndPoint(new StringBuffer().append(endPoint.x)
                    .append(",").append(endPoint.y).toString());
            shapeLists.add(remoteShape);
        }
        remotePage.setShapes(shapeLists);
        return remotePage;
    }
}

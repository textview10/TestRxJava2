package com.zonkey.mobileteach_lib.view.drawpaint.util;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PointInfo;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/8/22 16:27
 * 获得各种类型Path的Util
 */

public class PathFactory {
    private static float arrowAngle = (float) (40 * 3.14 / 360);
    private static final int arrowLength = 30;
    private static final String TAG = "PathFactory";

    /**
     * 创建四线格的path
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return
     */
    public static Path creatAllSiXianGe(float startX, float startY, float endX, float endY) {
        float disVer = (endY - startY) / 3;
        Path path = new Path();
        path.addRect(new RectF(startX, startY, endX, endY), Path.Direction.CCW);
        path.moveTo(startX, startY);
        path.lineTo(endX, startY);
        path.moveTo(startX, startY + disVer);
        path.lineTo(endX, startY + disVer);
        path.moveTo(startX, startY + disVer * 2);
        path.lineTo(endX, startY + disVer * 2);
        path.moveTo(startX, endY);
        path.lineTo(endX, endY);
        return path;
    }

    /**
     * 创建米字格的Path
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return
     */
    public static Path createAllMiZhiGe(float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.addRect(new RectF(startX, startY, endX, endY), Path.Direction.CCW);
        path.moveTo(startX, startY + (endY - startY) / 2);
        path.lineTo(endX, startY + (endY - startY) / 2);
        path.moveTo(startX + (endX - startX) / 2, startY);
        path.lineTo(startX + (endX - startX) / 2, endY);
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        path.moveTo(startX, endY);
        path.lineTo(endX, startY);
        return path;
    }

    /**
     * 创建十字格Path
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return
     */
    public static Path createAllShiZhiGe(float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.addRect(new RectF(startX, startY, endX, endY), Path.Direction.CCW);
        path.moveTo(startX, startY + (endY - startY) / 2);
        path.lineTo(endX, startY + (endY - startY) / 2);
        path.moveTo(startX + (endX - startX) / 2, startY);
        path.lineTo(startX + (endX - startX) / 2, endY);
        return path;
    }

    /**
     * 绘制箭头
     *
     * @param startP
     * @param endP
     * @return
     * @author 孙潮宗 计算箭头的算法,复制自pc客户端
     */
    public static Path createArrow(PointF startP, PointF endP) {
        float lineAngle = (float) Math.atan((endP.y - startP.y) / (endP.x - startP.x));
        float angleA1 = lineAngle - arrowAngle;
        float angleA2 = lineAngle + arrowAngle;
        int adjust = (endP.x >= startP.x) ? -1 : 1;
        float a1X = (float) (endP.x + adjust * arrowLength * Math.cos(angleA1));
        float a1Y = (float) (endP.y + adjust * arrowLength * Math.sin(angleA1));
        float a2X = (float) (endP.x + adjust * arrowLength * Math.cos(angleA2));
        float a2Y = (float) (endP.y + adjust * arrowLength * Math.sin(angleA2));
        PointF _arrowPoint1 = new PointF(a1X, a1Y);
        PointF _arrowPoint2 = new PointF(a1X + (a2X - a1X) / 3, a1Y + (a2Y - a1Y) / 3);
        PointF _arrowPoint3 = new PointF(a1X + (a2X - a1X) * 2 / 3, a1Y + (a2Y - a1Y) * 2 / 3);
        PointF _arrowPoint4 = new PointF(a2X, a2Y);
        double minPointDistance = arrowLength * Math.cos(arrowAngle);
        PointF[] points;
        if (distance(startP, endP) > minPointDistance) {
            points = new PointF[]{startP, _arrowPoint2, _arrowPoint1, endP, _arrowPoint4, _arrowPoint3, startP};
        } else {
            points = new PointF[]{_arrowPoint1, endP, _arrowPoint4, _arrowPoint1};
        }
        Path path = new Path();
        for (int i = 0; i < points.length; i++) {
            if (i == 0) {
                path.moveTo(points[0].x, points[0].y);
            } else {
                path.lineTo(points[i].x, points[i].y);
            }
        }
        return path;
    }

    private static double distance(PointF startP, PointF endP) {
        return Math.sqrt(Math.pow(Math.abs(startP.x - endP.x), 2) + Math.pow(Math.abs(startP.y - endP.y), 2));
    }

    /**
     * 绘制贝塞尔曲线
     *
     * @param lineInfo
     * @param transPointF
     * @return
     */
    public static Path createBesier(LineInfo lineInfo, TransPointF transPointF) {
        Path path = new Path();
        ArrayList<PointInfo> pointInfos = lineInfo.getCurrentPointLists();
        for (int j = 0; j < pointInfos.size(); j++) {
            PointF pointF = null;
            if (transPointF == null) {
                pointF = pointInfos.get(j).getPointF();
            } else {
                pointF = transPointF.logic2Display(pointInfos.get(j).getPointF());
            }
            if (j == 0) {
                path.moveTo(pointF.x, pointF.y);
            } else {
                PointF startPointF = pointInfos.get(j - 1).getPointF();
                if (startPointF == null) {
                    LogUtil.writeLog(TAG, "第" + (j - 1) + "个位置point点为空");
                    continue;
                }
                PointF prePointF;
                if (transPointF == null) {
                    prePointF = startPointF;
                } else {
                    prePointF = transPointF.logic2Display(startPointF);
                }
                float besX = getBesPoint(prePointF.x, pointF.x);
                float besY = getBesPoint(prePointF.y, pointF.y);
                path.quadTo(prePointF.x, prePointF.y, besX, besY);     //绘制贝斯尔曲线
            }
        }
        return path;
    }

    /**
     * 创建一个矩形
     *
     * @param startF
     * @param endF
     * @return
     */
    public static RectF createRectF(PointF startF, PointF endF) {
        float startX, startY, endX, endY = 0;
        if (endF.x > startF.x) {
            if (endF.y > startF.y) {
                startX = startF.x;
                startY = startF.y;
                endX = endF.x;
                endY = endF.y;
            } else {
                startX = startF.x;
                startY = endF.y;
                endX = endF.x;
                endY = startF.y;
            }
        } else {
            if (endF.y > startF.y) {
                startX = endF.x;
                startY = startF.y;
                endX = startF.x;
                endY = endF.y;
            } else {
                startX = endF.x;
                startY = endF.y;
                endX = startF.x;
                endY = startF.y;
            }
        }
        return new RectF(startX, startY, endX, endY);
    }

    /**
     * 根据矩形创建十字
     *
     * @param rectF
     * @return
     */
    public static Path createShiZi(RectF rectF) {
        Path path = new Path();
        path.moveTo(rectF.left, rectF.top + (rectF.bottom - rectF.top) / 2);
        path.lineTo(rectF.right, rectF.top + (rectF.bottom - rectF.top) / 2);
        path.moveTo(rectF.left + (rectF.right - rectF.left) / 2, rectF.top);
        path.lineTo(rectF.left + (rectF.right - rectF.left) / 2, rectF.bottom);
        return path;
    }

    /**
     * 创建直线Path
     *
     * @param lineInfo
     * @param transPointF if transPointF is null ,no translate coordinate
     * @return
     */
    public static Path createPolyLine(LineInfo lineInfo, TransPointF transPointF) {
        Path path = new Path();
        ArrayList<PointInfo> pointInfos = lineInfo.getCurrentPointLists();
        for (int j = 0; j < pointInfos.size(); j++) {
            PointF pointF = null;
            if (pointF == null) {
                pointF = pointInfos.get(j).getPointF();
            } else {
                pointF = transPointF.logic2Display(pointInfos.get(j).getPointF());
            }
            if (j == 0) {
                path.moveTo(pointF.x, pointF.y);
            } else {
                path.lineTo(pointF.x, pointF.y);
            }
        }
        return path;
    }

    public static Path createMiZi(RectF rectF) {
        Path path = new Path();
        path.moveTo(rectF.left, rectF.top + (rectF.bottom - rectF.top) / 2);
        path.lineTo(rectF.right, rectF.top + (rectF.bottom - rectF.top) / 2);
        path.moveTo(rectF.left + (rectF.right - rectF.left) / 2, rectF.top);
        path.lineTo(rectF.left + (rectF.right - rectF.left) / 2, rectF.bottom);
        path.moveTo(rectF.left, rectF.top);
        path.lineTo(rectF.right, rectF.bottom);
        path.moveTo(rectF.left, rectF.bottom);
        path.lineTo(rectF.right, rectF.top);
        return path;
    }


    /**
     * 获得上一个点为当前贝塞尔曲线的控制点,本算法计算出的点为贝塞尔曲线结束点
      */
    public static float getBesPoint(float prex, float x) {
        return (prex + x) / 2;
    }
}

package com.zonkey.mobileteach_lib.view.drawpaint.util;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PointInfo;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/8/22 16:25
 * 擦除区域或者某一笔的工具类
 */

public class EraserUtil {
    /**
     * 触摸时获得某一笔id的方法
     *
     * @param touchX      触摸x坐标
     * @param touchY      触摸y坐标
     * @param transPointF 转化坐标系
     * @param mPaintLines 所有绘制信息的集合
     * @return 如果检测到该Path与点击区域相交, 则返回该path的id, 否则返回null
     */
    public static String getTouchLineId(float touchX, float touchY, TransPointF transPointF, ArrayList<LineInfo> mPaintLines) {
        if (mPaintLines.size() < 1) return null;
        PointF pointF = transPointF.display2Logic(touchX, touchY);
        double minDistance=2;
        int index = -1;
        for (int i = mPaintLines.size() - 1; i >= 0; i--) {
            LineInfo lineInfo = mPaintLines.get(i);
            if (lineInfo.getIsDelete() == 1) continue;
            ArrayList<PointInfo> mPointFLists = lineInfo.getCurrentPointLists();
            for (int j = 0; j < mPointFLists.size()-1; j++) {
                PointF pointFJ = mPointFLists.get(j).getPointF();
                PointF pointFJNext = mPointFLists.get(j+1).getPointF();
               double distance = pointToLine(pointFJ.x,pointFJ.y,pointFJNext.x,pointFJNext.y,pointF.x,pointF.y);
                if(distance<minDistance) {
                    minDistance=distance;
                    index = i;
                }
            }
        }
        if(index==-1) {
            return null;
        }else  {
            return mPaintLines.get(index).getLineId();
        }

    }

    // 点到直线的最短距离的判断 点（x0,y0） 到由两点组成的线段（x1,y1） ,( x2,y2 )
    private static double pointToLine(float x1, float y1, float x2, float y2, float x0,
                               float y0) {
        double space = 0;
        double a, b, c;
        a = lineSpace(x1, y1, x2, y2);// 线段的长度
        b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
        c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
        if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
        }
        if (a <= 0.000001) {
            space = b;
            return space;
        }
        if (c * c >= a * a + b * b) {
            space = b;
            return space;
        }
        if (b * b >= a * a + c * c) {
            space = c;
            return space;
        }
        double p = (a + b + c) / 2;// 半周长
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }

    // 计算两点之间的距离
    private static double lineSpace(float x1, float y1, float x2, float y2) {
        double lineLength = 0;
        lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));
        return lineLength;
    }

    /**
     * 删除区域内包含的所有曲线
     */

    public static ArrayList<LineInfo> deleteArea(float downX, float downY, float moveX, float moveY, ArrayList<LineInfo> mPaintLines, TransPointF transPointF) {
        if (downX == -1 || downY == -1 || moveX == -1 || moveY == -1) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        PointF startF = transPointF.display2Logic(downX, downY);
        PointF endF = transPointF.display2Logic(moveX, moveY);
        RectF rectF = PathFactory.createRectF(startF,endF);
        ArrayList<LineInfo> temp = new ArrayList<>();
        for (int i = mPaintLines.size() - 1; i >= 0; i--) {
            LineInfo lineInfo = mPaintLines.get(i);
            if (lineInfo.getIsDelete() == 1) continue;
            Path path = getLineInfoPath(lineInfo);
            if (path == null) continue;
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            if (rectF.contains(bounds)) {
                sb.append(lineInfo.getLineId()).append(",");
                temp.add(lineInfo);
                mPaintLines.get(i).setIsDelete(1);
            }
        }
        return (temp == null || temp.size() == 0) ? null : temp;
    }

    /**
     * 获得某个类的形状
     * @param lineInfo
     * @return
     */
    private static Path getLineInfoPath(LineInfo lineInfo) {
        if (lineInfo == null) return null;
        ArrayList<PointInfo> currentPointLists = lineInfo.getCurrentPointLists();
        if (currentPointLists.size() < 2) {
            return null;
        }
        Path path = new Path();
        PointF preP = currentPointLists.get(0).getPointF();
        PointF ctrlP = currentPointLists.get(1).getPointF();
        switch (lineInfo.getType()) {
            case 0:
                path = PathFactory.createBesier(lineInfo, null);
                break;
            case 5:
                if (preP == null || ctrlP == null) return null;
                path = PathFactory.createAllShiZhiGe(preP.x, preP.y, ctrlP.x, ctrlP.y);
                break;
            case 6:
                if (preP == null || ctrlP == null) return null;
                path = PathFactory.createAllMiZhiGe(preP.x, preP.y, ctrlP.x, ctrlP.y);
                break;
            case 7:
                if (preP == null || ctrlP == null) return null;
                path = PathFactory.creatAllSiXianGe(preP.x, preP.y, ctrlP.x, ctrlP.y);
                break;
            case 8:
                path = PathFactory.createPolyLine(lineInfo, null);
                break;
        }
        return path;
    }
}

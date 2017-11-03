package com.zonkey.mobileteach_lib.view.drawpaint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.R;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.util.Build32Code;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.util.ToastUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PageInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PointInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.controll.ActionController;
import com.zonkey.mobileteach_lib.view.drawpaint.controll.ReceivePointController;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.OnDrawListener;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.OnRefreshListner;
import com.zonkey.mobileteach_lib.view.drawpaint.util.ColorUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.util.DrawUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.util.EraserUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.util.PaintInfo2Pc;
import com.zonkey.mobileteach_lib.view.drawpaint.util.PathFactory;
import com.zonkey.mobileteach_lib.view.drawpaint.util.TransPointF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xu.wang
 * Date on 2017/7/17 10:07
 */

public class PaintAttacher implements OnRefreshListner {
    private String TAG = "PaintAttacher";
    private ArrayList<LineInfo> mPaintLines = new ArrayList<>();     //记录所有线条的集合
    private ArrayList<LineInfo> mTeachMasterUodoLines = new ArrayList<>();  //只用于智课助手的undo,redo逻辑
    private int currentColor; //记录画笔颜色
    private int currentPaintSize; //记录画笔粗细
    private int currentPoint = 0;   //当前是本次绘制的第几个点
    private float preX, preY = 0;   //上一个点的位置
    private float downX, downY, endX, endY = -1; //用于区域擦除
    private PaintInfo2Pc paintInfo2Pc;      //向pc端发送消息的对象
    private int mTouchSlop;             //最小滑动距离
    private DrawState mDrawState = DrawState.NONE; //当前绘制状态
    public OnDrawListener mOnDrawListener;
    private TransPointF transPointF;    //转化坐标系的工具类
    private LineInfo lineInfo;          //最后一笔的lineInfo
    private Bitmap mEraserBitmap;
    private boolean isEraser = false;   //是否绘制橡皮擦图形
    private Matrix mBitmapMatrix = new Matrix();
    private View mView;
    private Paint mPaint;
    private ActionController mActionController;     //控制undo,redo栈
    private ReceivePointController mReceivePointController;     //处理收到远端点的逻辑

    public enum DrawState {
        //无状态,折线,贝塞尔曲线 橡皮,擦除区域,荧光笔 ,带箭头的直线,正方形,圆形,田字格,米字格,四线格
        NONE, POLYLINE, BEZIER, ERASER, ERASER_RECT, PEN_YINGGUANG, ARROW, RECTANGLE, ELLIPSE, TIANZHIGE, MIZIGE, SIXIANGE
    }

    public PaintAttacher(View view) {
        this.mView = view;
        initialData();
    }

    private void initialData() {
        mTouchSlop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
        currentColor = DrawPathCong.mStartColor;
        currentPaintSize = DrawPathCong.mStartSize;
        paintInfo2Pc = new PaintInfo2Pc();
        Collections.synchronizedList(mPaintLines);
        mActionController = new ActionController(paintInfo2Pc);
        mReceivePointController = new ReceivePointController(mPaintLines, this);
        mEraserBitmap = BitmapFactory.decodeResource(mView.getResources(), R.drawable.eraser);
        initPaint();
    }

    /**
     * 初始化画笔,为简化逻辑,使用统一变量记录当前画笔颜色,粗细
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(currentColor);
//        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(currentPaintSize);
    }

    private void touch_down(float downX, float downY, ScaleAttacher scaleAttacher) {
        float[] tempF = {downX, downY};
        if (scaleAttacher != null) {
            Matrix tempMatrix = new Matrix();
            scaleAttacher.getCurrentMatrix().invert(tempMatrix);
            tempMatrix.mapPoints(tempF, new float[]{downX, downY});
        }
        lineInfo = new LineInfo();
        mActionController.pushData(lineInfo, ActionController.ADD_SHAPE);
        lineInfo.setLineId(Build32Code.createGUID());
        if (mDrawState == DrawState.BEZIER || mDrawState == DrawState.PEN_YINGGUANG) {
            if (mDrawState == DrawState.BEZIER) {
                lineInfo.setColor(currentColor);
                lineInfo.setStrokeWidth(currentPaintSize);
                lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), 0));
                paintInfo2Pc.sendDownInfo(lineInfo.getLineId(), MobileTeachApi.PresentControl.QuadraticBezier, currentColor, currentPaintSize,
                        transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
            } else if (mDrawState == DrawState.PEN_YINGGUANG) {
                int currentAlphaColor = ColorUtil.convertColorToString(currentColor, DrawPathCong.mYingGuangAlhpa);
                lineInfo.setColor(currentAlphaColor);
                lineInfo.setStrokeWidth(DrawPathCong.mYingGuangSize);
                lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), 0));
                paintInfo2Pc.sendDownInfo(lineInfo.getLineId(), MobileTeachApi.PresentControl.QuadraticBezier, currentAlphaColor, DrawPathCong.mYingGuangSize,
                        transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
            }
            mPaintLines.add(lineInfo);
            preX = tempF[0];
            preY = tempF[1];
        } else if (mDrawState == DrawState.ARROW || mDrawState == DrawState.ELLIPSE || mDrawState == DrawState.RECTANGLE
                || mDrawState == DrawState.TIANZHIGE || mDrawState == DrawState.MIZIGE || mDrawState == DrawState.SIXIANGE) {
            lineInfo.setColor(currentColor);
            lineInfo.setStrokeWidth(currentPaintSize);
            lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), 0));
            String drawType = MobileTeachApi.PresentControl.PAINT_ARROW_LINE;
            switch (mDrawState) {
                case ARROW:
                    lineInfo.setType(4);
                    drawType = MobileTeachApi.PresentControl.PAINT_ARROW_LINE;
                    break;
                case ELLIPSE:
                    lineInfo.setType(3);
                    drawType = MobileTeachApi.PresentControl.PAINT_ELLIPSE;
                    break;
                case RECTANGLE:
                    lineInfo.setType(2);
                    drawType = MobileTeachApi.PresentControl.PAINT_RECTANGLE;
                    break;
                case TIANZHIGE:
                    lineInfo.setType(5);
                    drawType = MobileTeachApi.PresentControl.PAINT_TIANZIGE;
                    break;
                case MIZIGE:
                    lineInfo.setType(6);
                    drawType = MobileTeachApi.PresentControl.PAINT_MIZIGE;
                    break;
                case SIXIANGE:
                    lineInfo.setType(7);
                    drawType = MobileTeachApi.PresentControl.PAINT_SiXianGE;
                    break;
            }
            paintInfo2Pc.sendDownInfo(lineInfo.getLineId(), drawType, currentColor, currentPaintSize,
                    transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
            mPaintLines.add(lineInfo);
        } else if (mDrawState == DrawState.POLYLINE) {
            lineInfo.setType(8);
            lineInfo.setColor(currentColor);
            lineInfo.setStrokeWidth(currentPaintSize);
            paintInfo2Pc.sendDownInfo(lineInfo.getLineId(), MobileTeachApi.PresentControl.PAINT_POLY_LINE, currentColor, currentPaintSize,
                    transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
        } else if (mDrawState == DrawState.ERASER_RECT) {
            this.downX = downX;
            this.downY = downY;
        }
        mView.invalidate();
    }

    private void touch_move(float moveX, float moveY, ScaleAttacher scaleAttacher) {
        float dx = Math.abs(moveX - preX);
        float dy = Math.abs(moveY - preY);
        if (dx >= mTouchSlop || dy >= mTouchSlop) {   //对发送点的频率进行一定的限制
            float[] tempF = {moveX, moveY};
            if (scaleAttacher != null) {
                Matrix tempMatrix = new Matrix();
                scaleAttacher.getCurrentMatrix().invert(tempMatrix);
                tempMatrix.mapPoints(tempF, new float[]{moveX, moveY});
            }
            if (lineInfo == null) {
                LogUtil.writeLog("PaintImageView", "按压点还没有执行");
                return;
            }
            if (mDrawState == DrawState.BEZIER || mDrawState == DrawState.PEN_YINGGUANG) {
                PointInfo prePointInfo = lineInfo.getCurrentPointLists().get(lineInfo.getCurrentPointLists().size() - 1);
                lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]),
                        prePointInfo.getIndex() + 1));
                if (MobileTeach.CurrentApp == MobileTeach.App_Teachmaster) {
                    float besX = PathFactory.getBesPoint(preX, tempF[0]);
                    float besY = PathFactory.getBesPoint(preY, tempF[1]);
                    PointF lj_start = new PointF(transPointF.display2RemoteX(preX), transPointF.display2RemoteY(preY));
                    PointF lj_ctrl = new PointF(transPointF.display2RemoteX(besX), transPointF.display2RemoteY(besY));
                    paintInfo2Pc.sendMoveInfo(lineInfo.getLineId(), currentPoint,
                            lj_start.x, lj_start.y, lj_ctrl.x, lj_ctrl.y);
                } else {
                    paintInfo2Pc.sendMoveInfo(lineInfo.getLineId(), currentPoint,
                            transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
                }
                mView.invalidate();
                preX = tempF[0];
                preY = tempF[1];
            } else if (mDrawState == DrawState.RECTANGLE || mDrawState == DrawState.ARROW || mDrawState == DrawState.ELLIPSE
                    || mDrawState == DrawState.TIANZHIGE || mDrawState == DrawState.MIZIGE || mDrawState == DrawState.SIXIANGE) {
                if (lineInfo.getCurrentPointLists().size() > 1) {
                    lineInfo.getCurrentPointLists().set(1, new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), 1));
                } else {
                    lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), 1));
                }
                paintInfo2Pc.setMoveShape(lineInfo.getLineId(), currentPoint, transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
                mView.invalidate();
            } else if (mDrawState == DrawState.POLYLINE) {
                lineInfo.getCurrentPointLists().add(new PointInfo(transPointF.display2Logic(tempF[0], tempF[1]), currentPoint));
                paintInfo2Pc.setMoveShape(lineInfo.getLineId(), currentPoint, transPointF.display2RemoteX(tempF[0]), transPointF.display2RemoteY(tempF[1]));
            } else if (mDrawState == DrawState.ERASER_RECT) {
                endX = moveX;
                endY = moveY;
                mView.invalidate();
            }
            currentPoint++;
        }
    }

    private void touch_up(float x, float y) {
        currentPoint = 0;
        if (MobileTeach.CurrentApp != MobileTeach.App_Teachmaster)
            paintInfo2Pc.sendUpInfo(lineInfo.getLineId());
        if (mDrawState == DrawState.ERASER_RECT) {
            ArrayList<LineInfo> lists = EraserUtil.deleteArea(downX, downY, x, y, mPaintLines, transPointF);
            mActionController.pushData(lists, ActionController.DELETE_MULTI_SHAPE);
            paintInfo2Pc.sendDeleteShape(lists);
            downX = downY = endX = endY = -1;
            if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        } else {
            if (mOnDrawListener != null) mOnDrawListener.drawLine(lineInfo, transPointF);
        }
        mView.invalidate();
        lineInfo = null;
    }

    public void onDraw(Canvas canvas) {
        //绘制最后一笔Path的轨迹
        if (lineInfo != null) {
            DrawUtil.drawPath(canvas, lineInfo, transPointF);
        }
        //绘制橡皮
        if (mDrawState == DrawState.ERASER && mEraserBitmap != null && isEraser) {
            canvas.drawBitmap(mEraserBitmap, mBitmapMatrix, mPaint);
        }
        //擦除区域模式
        if (mDrawState == DrawState.ERASER_RECT) {
            drawEraserRect(canvas);
        }
        //绘制收到的远端点的缓存区
        DrawUtil.drawAllPath(canvas, mReceivePointController.getReceiveList(), transPointF);
    }

    //擦除区域
    private void drawEraserRect(Canvas canvas) {
        if (downX == -1 || downY == -1 || endX == -1 || endY == -1) {
            return;
        }
        canvas.drawRect(PathFactory.createRectF(new PointF(downX, downY), new PointF(endX, endY)), mPaint);
    }


    /**
     * 可供调用的触摸事件
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event, ScaleAttacher scaleAttacher) {
        float x = event.getX();
        float y = event.getY();
        if (mDrawState == DrawState.NONE) return true;

        if (mDrawState == DrawState.ERASER) {
            preX = x;
            preY = y;
            if (mEraserBitmap == null)
                mEraserBitmap = BitmapFactory.decodeResource(mView.getResources(), R.drawable.eraser);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                String lineId = EraserUtil.getTouchLineId(x, y, transPointF, mPaintLines);
                if (TextUtils.isEmpty(lineId)) return true;
                deleteById(lineId, false);
                mView.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                initialBitmapMatrix();
            }
            return true;
        } else {    //不在放大状态,可以批注
            mView.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_down(x, y, scaleAttacher);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y, scaleAttacher);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    touch_up(x, y);
                    break;
            }
            return true;
        }
    }

    //点击橡皮擦除的时候,执行一个逐渐缩小的动画
    private void initialBitmapMatrix() {
        mBitmapMatrix.reset();
        mBitmapMatrix.postTranslate(preX - mEraserBitmap.getWidth() / 2, preY - mEraserBitmap.getHeight() / 2);
        new Thread() {
            @Override
            public void run() {
                super.run();
                isEraser = true;
                mView.postInvalidate();
                for (int i = 0; i < 10; i++) {
                    SystemClock.sleep(50);
                    if (i < 6)
                        mBitmapMatrix.postScale(0.92f, 0.92f, preX - mEraserBitmap.getWidth() / 2, preY - mEraserBitmap.getHeight() / 2);
                    mView.postInvalidate();
                }
                isEraser = false;
                mView.postInvalidate();
            }
        }.start();
    }


    /**
     * 改变画笔信息,为简化逻辑,使用统一变量记录当前画笔颜色,粗细
     */
    private void changePaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(currentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(currentPaintSize);
    }


    public void sendTransLateInfo(float disX, float disY, int width, int height) {
        paintInfo2Pc.sendTransLateInfo(disX / width * 100, disY / height * 100);
    }

    public void sendScaleInfo(float scale, float focusX, float focusY, boolean isRemote) {
        if (!isRemote) {
            paintInfo2Pc.sendScaleInfo(scale, transPointF.display2RemoteX(focusX), transPointF.display2RemoteY(focusY));
        } else {
            paintInfo2Pc.sendScaleInfo(scale, focusX, focusY);
        }
    }

    //绘制远程收到的点...
    public void drawRemotePointF(ReceiverInfo receiverInfo) throws Exception {
        if (TextUtils.isEmpty(receiverInfo.getData())) return;

        if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.REQUEST_NEW_SHAPE) { //第一个点,增加图形
            mReceivePointController.newShape(receiverInfo, transPointF); //收到增加某一条线条的指令
        } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.REQUEST_ADD_POINT) { //第一个点之后的点
            mReceivePointController.addPoint(receiverInfo, transPointF); //收到往某一条线条上追加点的指令
        } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_EndDrawing) {
            mReceivePointController.endDrawing(receiverInfo, transPointF);   //结束某一条线条的绘制
        }
    }

    //###########################Api ################################
    public void setCurrentColor(int color) {
        this.currentColor = color;
        changePaint();
    }

    public void clearLocalData() {
        mPaintLines.clear();
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
    }

    /**
     * 清空画板,将其加到undo栈里
     */
    public void clear(boolean isRemote) {
        if (!isRemote) {
            ArrayList<LineInfo> totalLists = new ArrayList<>(); //记录当前显示在view上的线条
            for (LineInfo lineInfo : mPaintLines) {
                if (lineInfo.getIsDelete() == 0) totalLists.add(lineInfo);
            }
            mActionController.pushData(totalLists, ActionController.DELETE_MULTI_SHAPE);
            paintInfo2Pc.sendMoveAllPath();
        }
        for (LineInfo line : mPaintLines) { //全部置为不绘制
            line.setIsDelete(1);
        }
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
    }


    @Override
    public void refresh(boolean isCreate) {
        if (mOnDrawListener != null && isCreate) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
    }

    /**
     * 本地根据线条id删除线条,可以是单id也可以是多id用,隔开   如id1,id2...
     *
     * @param lineIds
     * @param isRemote 是否是远端来的线条,远端的线条不进undo redo栈,不发送
     */
    public void deleteById(String lineIds, boolean isRemote) {
        if (mPaintLines == null || mPaintLines.size() == 0) return;
        if (isRemote && TextUtils.isEmpty(lineIds)) {   //如果是远端传来的,并且参数为null,则清空全部线条
            clear(true);
            return;
        }

        String[] split = lineIds.split(",");
        if (split == null || split.length == 0) return;
        ArrayList<LineInfo> tempLists = new ArrayList<>();
        for (String id : split) {
            for (int i = 0; i < mPaintLines.size(); i++) {
                if (TextUtils.equals(id, mPaintLines.get(i).getLineId()) && (mPaintLines.get(i).getIsDelete() == 0)) {
                    tempLists.add(mPaintLines.get(i));
                    mPaintLines.get(i).setIsDelete(1);
                    break;
                }
            }
        }
        if (tempLists.size() == 0) return;
        if (!isRemote) {
            if (tempLists.size() == 1) { //删除单线条
                mActionController.pushData(tempLists.get(0), ActionController.DELETE_SHAPE);
            } else {    //删除多线条
                mActionController.pushData(tempLists, ActionController.DELETE_MULTI_SHAPE);
            }
            paintInfo2Pc.sendDeleteShape(tempLists);
        }
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
    }

    public void receiveUndoRedo(String cmd) {
        mActionController.receiveUndoRedo(mPaintLines, cmd);
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
    }

    public boolean redo() {
        boolean isCanRedo = mActionController.redo(mPaintLines);
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
        return isCanRedo;
    }

    public boolean undo() {
        boolean isCanUndo = mActionController.undo(mPaintLines);
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
        return isCanUndo;
    }

    public void reset(boolean isSend) {
        if (isSend) paintInfo2Pc.clearMatrix();
    }

    public void setSendIps(List<String> lists) {
        ArrayList<String> tempLists = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            if (!tempLists.contains(lists.get(i)))
                tempLists.add(lists.get(i));
        }
        paintInfo2Pc.setSendIps(lists);
    }

    public void setMiniBoradState(String miniBoradId, String pageId) {
        paintInfo2Pc.setMiniBoradState(miniBoradId, pageId);
    }

    // TODO: 2017/9/5 CAH
    public void setSharePicStatus(String sharePicId, String pageId) {
        paintInfo2Pc.setSharePicStatus(sharePicId, pageId);
    }

    public void quitMiniBoradState() {
        paintInfo2Pc.quitMiniBoradState();
    }

    public void sendRotate(int rotate) {
        paintInfo2Pc.sendRotateInfo(rotate);
    }

    public ArrayList<LineInfo> getDrawInfo() {
        return mPaintLines;
    }

    public ActionController getActionController() {
        return mActionController;
    }

    public void setDrawInfo(PageInfo pageInfo) {
        if (this.mPaintLines.size() > 0)
            this.mPaintLines.clear();
        this.mPaintLines.addAll(pageInfo.getLineInfos());
        this.mActionController.setUndoList(pageInfo.getUndoLists());
        this.mActionController.setRedoList(pageInfo.getRedoLists());
        //因为setDrawInfo的时候,图片有可能还没有加载到页面上,判断mCanvas的状态可以知道加载是否成功
        if (mOnDrawListener != null) mOnDrawListener.refreshCanvasBitmap();
        mView.invalidate();
    }

    public void setStrokeWidth(int strokeWidth) {
        this.currentPaintSize = strokeWidth;
        changePaint();
    }

    public void setTeacherIp(String teacherIp) {
        paintInfo2Pc.setTeacherIp(teacherIp);
    }

    public void setDrawState(DrawState drawState) {
        mDrawState = drawState;
    }

    public void initialData(float bitmapW, float bitmapH, float centerVX, float centerVY) {
        transPointF = new TransPointF(bitmapW, bitmapH, centerVX, centerVY);
    }

    public TransPointF getTransPointF() {
        return transPointF;
    }

    public DrawState getDrawState() {
        return mDrawState;
    }

    public void setOnDrawListener(OnDrawListener listener) {
        mOnDrawListener = listener;
    }
}
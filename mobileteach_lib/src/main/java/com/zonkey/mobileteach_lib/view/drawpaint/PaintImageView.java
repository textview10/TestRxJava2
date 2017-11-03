package com.zonkey.mobileteach_lib.view.drawpaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.net.client.UdpUtil;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.ActionLineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PageInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.IPaint;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.IState;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.OnDrawListener;
import com.zonkey.mobileteach_lib.view.drawpaint.util.DrawUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.util.TransPointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xu.wang
 * Date on 2017/2/20 13:15
 * 图片显示划线逻辑
 */
public class PaintImageView extends AppCompatImageView implements OnDrawListener, IState, IPaint {
    private String TAG = getClass().getSimpleName().toString();
    private Canvas mCanvas;         //双缓冲中缓存历史线条
    private Bitmap mCanvasBitmap; //用来绘制线条的bitmap
    private PaintAttacher mPaintAttacher;
    private ScaleAttacher mScaleAttacher;   //执行处理放大逻辑的类

    private boolean isCanClick = false; //是否可以点击 true能, ,默认false
    private int mBitmapWidth, mBitmapHeight = -1;
    private float mCentreTranX, mCentreTranY;// 图片初始化居中时的偏移


    public PaintImageView(Context context) {
        this(context, null);
    }

    public PaintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialData();
    }

    private void initialData() {
        mScaleAttacher = new ScaleAttacher(this);
        mPaintAttacher = new PaintAttacher(this);
        mPaintAttacher.setOnDrawListener(this);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //给画布设置matrix
        if (mScaleAttacher.getCurrentMatrix() != null && !isCanClick)
            canvas.setMatrix(mScaleAttacher.getCurrentMatrix());
        //绘制imageview图片的信息
        super.onDraw(canvas);
        //绘制Bitmap缓存区内的线条
        if (mCanvasBitmap != null) {
            canvas.drawBitmap(mCanvasBitmap, 0, 0, new Paint());
        } else {
            if (mPaintAttacher.getTransPointF() != null) {
                DrawUtil.drawAllPath(canvas, mPaintAttacher.getDrawInfo(), mPaintAttacher.getTransPointF());
            }
        }
        mPaintAttacher.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPaintAttacher.getDrawState() == PaintAttacher.DrawState.NONE) {
            if (!isCanClick) {                  //作为列表中条目时,不可放大,可点击
                return mScaleAttacher.onPaintTouchEvent(event);
            } else {
                return super.onTouchEvent(event);
            }
        } else {
            return mPaintAttacher.onTouchEvent(event, mScaleAttacher);
        }
    }

    @Override
    public void drawLine(LineInfo lineInfo, TransPointF transPointF) {
        if (mCanvas != null) {
            DrawUtil.drawPath(mCanvas, lineInfo, transPointF);
        }
    }

    @Override
    public void refreshCanvasBitmap() {
        if (mCanvas != null) initDrawCanvas();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            return;
        }
        mBitmapWidth = drawable.getIntrinsicWidth();
        mBitmapHeight = drawable.getIntrinsicHeight();
        mCentreTranX = (getWidth() - drawable.getIntrinsicWidth()) / 2;
        mCentreTranY = (getHeight() - drawable.getIntrinsicHeight()) / 2;
        mPaintAttacher.initialData(mBitmapWidth, mBitmapHeight, mCentreTranX, mCentreTranY);
        if (mBitmapWidth > 0 || mBitmapHeight > 0) {
            invalidate();
        }
        mScaleAttacher.update();
    }

    public void sendTransLateInfo(float disX, float disY) {
        mPaintAttacher.sendTransLateInfo(disX, disY, mBitmapWidth, mBitmapHeight);
    }

    public void sendScaleInfo(float scale, float focusX, float focusY) {
        mPaintAttacher.sendScaleInfo(scale, focusX, focusY, false);
    }


    //####################供外界使用的Api#################################
    //####################################################################
    @Override
    public void setCurrentColor(int color) {
        mPaintAttacher.setCurrentColor(color);
    }

    /**
     * 清空
     */
    @Override
    public void clearLocalData() {
        mPaintAttacher.clearLocalData();
    }

    @Override
    public void clear() {
        mPaintAttacher.clear(false);
    }

    /**
     * 清空变化信息,重置Matrix
     */
    public void reset() {
        mScaleAttacher.getCurrentMatrix().reset();
        mPaintAttacher.reset(true);
        invalidate();
    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    @Override
    public boolean undo() {
        return mPaintAttacher.undo();
    }

    @Override
    public boolean redo() {
        return mPaintAttacher.redo();
    }

    /**
     * 设置是否发送消息到那几个ip
     * 如果没有设置发送ip,则默认向pc端发送,
     * 如果设置了,则不再向pc端发送,转而向设置的ip发送
     * 如果为null,则不发送;
     *
     * @param lists 发送列表
     */
    @Override
    public void setSendIps(List<String> lists) {
        mPaintAttacher.setSendIps(lists);
    }

    /**
     * 调用则进入迷你黑板模式,迷你黑板下,向pc发送的数据与其它情况下不一样
     *
     * @param miniBoradId 迷你黑板的Id
     * @param pageId      迷你黑板当前页的Id
     */
    public void setMiniBoradState(String miniBoradId, String pageId) {
        mPaintAttacher.setMiniBoradState(miniBoradId, pageId);
    }

    public void setSharePicStatus(String sharePicId, String pageId) {
        mPaintAttacher.setSharePicStatus(sharePicId, pageId);
    }

    /**
     * 退出迷你黑板模式
     */
    public void quitMiniBoradState() {
        mPaintAttacher.quitMiniBoradState();
    }

    /**
     * 设定需要旋转的角度
     * 执行前会set matrix
     *
     * @param rotate
     */
    public void setRotationTo(int rotate) {
        mScaleAttacher.setRotationTo(rotate);
        mPaintAttacher.sendRotate(rotate);
    }

    /**
     * 设定继续旋转的角度
     * 会post matrix
     *
     * @param rotate
     */
    public void setRotationBy(int rotate) {
        mScaleAttacher.setRotationBy(rotate);
        mPaintAttacher.sendRotate(rotate);
    }

    /**
     * 获得当前图片的信息
     *
     * @return
     */
    @Override
    public ArrayList<LineInfo> getDrawInfo() {
        return mPaintAttacher.getDrawInfo();
    }

    /**
     * 获取所有的撤销记录
     * @return
     */
    @Override
    public ArrayList<ActionLineInfo> getUndoInfo() {
        return mPaintAttacher.getActionController().getUndoList();
    }

    /**
     * 获取所有的恢复记录
     * @return
     */
    @Override
    public ArrayList<ActionLineInfo> getRedoInfo() {
        return mPaintAttacher.getActionController().getRedoList();
    }


    /**
     * 设置所有要绘制的线条信息
     *
     * @param pageInfo
     */
    @Override
    public void setDrawInfo(PageInfo pageInfo) {
        mPaintAttacher.setDrawInfo(pageInfo);
    }

    /**
     * 进入擦除某一笔的橡皮擦状态...
     */
    @Override
    public void setLineEraserState() {
        quitDraw();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ERASER);
    }

    /**
     * 进入擦除一片区域的模式
     */
    @Override
    public void setEraserRect() {
        quitDraw();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ERASER_RECT);
    }

    /**
     * 绘制正方形
     */
    @Override
    public void setDrawRectangle() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.RECTANGLE);
    }

    /**
     * 绘制贝塞尔曲线
     */
    @Override
    public void setDrawHandWrite() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.BEZIER);
    }

    /**
     * 绘制折线
     */
    @Override
    public void setDrawPolyLine() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.POLYLINE);
    }

    /**
     * 绘制箭头
     */
    @Override
    public void setDrawArrow() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ARROW);
    }

    /**
     * 绘制椭圆
     */
    @Override
    public void setDrawEllipse() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ELLIPSE);
    }

    /**
     * 绘制荧光笔
     */
    @Override
    public void setPenYingGuang() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.PEN_YINGGUANG);
    }

    /**
     * 绘制十字格
     */
    @Override
    public void setTianZiGe() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.TIANZHIGE);
    }

    /**
     * 绘制米字格
     */
    @Override
    public void setMiZiGe() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.MIZIGE);
    }

    /**
     * 绘制四线格
     */
    @Override
    public void setSiXianGe() {
        initDrawCanvas();
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.SIXIANGE);
    }

    /**
     * 设置当前状态可放大
     */
    @Override
    public void setScaleState() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.NONE);
        quitDraw();
    }

    /**
     * 为节约内存当有退出批注的操作时,回收缓存区bitmap
     */
    private void quitDraw() {
        if (mCanvasBitmap != null) {
            mCanvasBitmap.recycle();
            mCanvasBitmap = null;
            invalidate();
        }
    }

    /**
     * 设置当前图片不可放大，可点击
     */
    public void setIsCanClick() {
        this.isCanClick = true;
    }


    /**
     * 设置receiverInfo指令的处理.可以处理所有跟PaintImageView有关的receiverInfo指令.
     *
     * @param receiverInfo
     */
    @Override
    public void setReceiverInfo(ReceiverInfo receiverInfo) {
        try {
            if (receiverInfo.getMainCmd() == MobileTeachApi.PresentControl.Main_Cmd) {
                if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.REQUEST_NEW_SHAPE || receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.REQUEST_ADD_POINT
                        || receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_EndDrawing) {
                    mPaintAttacher.drawRemotePointF(receiverInfo);
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Clear) {
                    mPaintAttacher.deleteById(receiverInfo.getData(), true);
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_ResetTransform) {
                    mPaintAttacher.reset(false);
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Redo) {
                    mPaintAttacher.receiveUndoRedo(receiverInfo.getData());
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Undo) {
                    mPaintAttacher.receiveUndoRedo(receiverInfo.getData());
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Scale) {
                    String[] split = receiverInfo.getData().split("\\|");
                    if (split.length != 2) {
                        throw new Exception("收到的放大指令不正确" + receiverInfo.getData());
                    }
                    String[] focusP = split[1].split(",");
                    PointF mPointF = new PointF(Float.parseFloat(focusP[0]), Float.parseFloat(focusP[1]));
                    PointF mNewPointF = mPaintAttacher.getTransPointF().logic2Display(mPointF);
                    setScale(mNewPointF.x, mNewPointF.y, Float.parseFloat(split[0]), false);
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Translate) {
                    String[] split = receiverInfo.getData().split(",");
                    if (split.length != 2) {
                        throw new Exception("收到的移动指令不正确" + receiverInfo.getData());
                    }
                    setTranslate(mPaintAttacher.getTransPointF().logic2DisplayDx(Float.parseFloat(split[0])), mPaintAttacher.getTransPointF().logic2DisplayDx(Float.parseFloat(split[1
                            ])));
                }
            }
        } catch (Exception e) {
            LogUtil.writeLog("PaintImageView", "接受远程指令异常" + e.toString());
        }
    }

    private void setScale(float focusX, float focusY, float scale, boolean isSend) {
        mScaleAttacher.getCurrentMatrix().reset();
        mScaleAttacher.getCurrentMatrix().setScale(scale, scale, focusX, focusY);
        invalidate();
        if (isSend) mPaintAttacher.sendScaleInfo(scale, focusX, focusY, true);
    }

    private void setTranslate(float dX, float dY) {
        mScaleAttacher.getCurrentMatrix().postTranslate(dX, dY);
        mScaleAttacher.checkMatrixBounds();
        invalidate();
    }

    /**
     * 设置教师端的Ip
     *
     * @param teacherIp
     */
    @Override
    public void setTeacherIp(String teacherIp) {
        mPaintAttacher.setTeacherIp(teacherIp);
    }

    /**
     * 设置当前画笔的粗细
     *
     * @param strokeWidth
     */
    @Override
    public void setStrokeWidth(int strokeWidth) {
        mPaintAttacher.setStrokeWidth(strokeWidth);
    }

    @Override
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight
                , Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        getDrawable().draw(canvas);
        TransPointF transPointF = new TransPointF(mBitmapWidth, mBitmapHeight, 0, 0);  //无需进行坐标系的转化
        DrawUtil.drawAllPath(canvas, mPaintAttacher.getDrawInfo(), transPointF);
        return bitmap;
    }

    /**
     * 初始化绘制时的bitmap缓存区
     */
    public void initDrawCanvas() {
        if (getWidth() > 0 && getHeight() > 0) {
            if (mCanvasBitmap != null) {
                mCanvasBitmap.recycle();
                mCanvasBitmap = null;
            }
            mCanvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_4444);
            mCanvas = new Canvas(mCanvasBitmap);
            if (mPaintAttacher.getDrawInfo() != null) {
                DrawUtil.drawAllPath(mCanvas, mPaintAttacher.getDrawInfo(), mPaintAttacher.getTransPointF());
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        setLayerType(View.LAYER_TYPE_NONE, null);
        super.onDetachedFromWindow();
    }

}

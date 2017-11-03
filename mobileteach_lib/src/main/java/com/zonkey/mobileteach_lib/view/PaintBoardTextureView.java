package com.zonkey.mobileteach_lib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.bean.ReceiverInfo;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.PaintAttacher;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.ActionLineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.bean.PageInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.IPaint;
import com.zonkey.mobileteach_lib.view.drawpaint.interf.IState;
import com.zonkey.mobileteach_lib.view.drawpaint.util.DrawUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xu.wang
 * Date on 2017/8/31 12:01
 */

public class PaintBoardTextureView extends TextureView implements TextureView.SurfaceTextureListener, Runnable, IState, IPaint {
    private boolean mLoop = false;
    private PaintAttacher mPaintAttacher;
    private int mColor = Color.WHITE;

    public PaintBoardTextureView(Context context) {
        this(context, null);
    }

    public PaintBoardTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialView();
    }

    private void initialView() {
        this.setSurfaceTextureListener(this);
        mPaintAttacher = new PaintAttacher(this);
//        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mLoop = true;
        new Thread(this).start();
//        LogUtil.e("PaintBlackBoradView", "create width" + getWidth() + "height " + getHeight());
        mPaintAttacher.initialData(getWidth(), getHeight(), 0, 0);
        drawCanvas();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void drawCanvas() {
        Canvas canvas = lockCanvas();
        if (canvas == null) {
            LogUtil.e("PaintBoardTextureView", "canvas == null");
            return;
        }
//       canvas.saveLayerAlpha(0,0,getWidth(),getHeight(),1.0f,)
        ArrayList<LineInfo> tempLists = mPaintAttacher.getDrawInfo();
        canvas.drawColor(mColor);
        DrawUtil.drawAllPath(canvas, tempLists, mPaintAttacher.getTransPointF());
        unlockCanvasAndPost(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPaintAttacher.getDrawState() == PaintAttacher.DrawState.NONE) {
            return super.onTouchEvent(event);
        } else {
            return mPaintAttacher.onTouchEvent(event, null);
        }
    }

    @Override
    public void run() {
        while (mLoop) {
            long start = System.currentTimeMillis();
            drawCanvas();
            long end = System.currentTimeMillis();
//            LogUtil.e("PaintBlackBoradView", "绘制时间" + (end - start));
            if (end - start < 50) {
                SystemClock.sleep(50 - (end - start));
            }
        }
    }

    @Override
    public void setLineEraserState() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ERASER);
    }

    @Override
    public void setEraserRect() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ERASER_RECT);
    }

    @Override
    public void setDrawHandWrite() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.BEZIER);
    }

    @Override
    public void setDrawPolyLine() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.POLYLINE);
    }

    @Override
    public void setDrawRectangle() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.RECTANGLE);
    }

    @Override
    public void setDrawArrow() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ARROW);
    }

    @Override
    public void setDrawEllipse() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.ELLIPSE);
    }

    @Override
    public void setPenYingGuang() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.PEN_YINGGUANG);
    }

    @Override
    public void setTianZiGe() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.TIANZHIGE);
    }

    @Override
    public void setMiZiGe() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.MIZIGE);
    }

    @Override
    public void setSiXianGe() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.SIXIANGE);
    }

    @Override
    public void setScaleState() {
        mPaintAttacher.setDrawState(PaintAttacher.DrawState.NONE);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        this.mColor = color;
    }

    @Override
    public void setDrawInfo(PageInfo pageInfo) {
        mPaintAttacher.setDrawInfo(pageInfo);
    }

    @Override
    public void setStrokeWidth(int strokeWidth) {
        mPaintAttacher.setStrokeWidth(strokeWidth);
    }

    @Override
    public boolean undo() {
        return mPaintAttacher.undo();
    }

    @Override
    public boolean redo() {
        return mPaintAttacher.redo();
    }

    @Override
    public void clearLocalData() {
        mPaintAttacher.clearLocalData();
    }

    @Override
    public void clear() {
        mPaintAttacher.clear(false);
    }

    @Override
    public void setCurrentColor(int color) {
        mPaintAttacher.setCurrentColor(color);
    }

    @Override
    public void setSendIps(List<String> lists) {
        mPaintAttacher.setSendIps(lists);
    }

    @Override
    public void setTeacherIp(String teacherIp) {
        mPaintAttacher.setTeacherIp(teacherIp);
    }

    @Override
    public void setReceiverInfo(ReceiverInfo receiverInfo) {
        try {
            if (receiverInfo.getMainCmd() == MobileTeachApi.PresentControl.Main_Cmd) {
                if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.REQUEST_NEW_SHAPE || receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.REQUEST_ADD_POINT
                        || receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_EndDrawing) {
                    mPaintAttacher.drawRemotePointF(receiverInfo);
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Clear) {
                    // TODO
//                    mPaintAttacher.clear(false);
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Redo) {
//                    mPaintAttacher.redo();
                } else if (receiverInfo.getSubCmd() == MobileTeachApi.PresentControl.Request_Undo) {
//                    mPaintAttacher.undo();
                }
            }
        } catch (Exception e) {
            LogUtil.writeLog("PaintBlackBoardView", "接受远程指令异常" + e.toString());
        }
    }

    @Override
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight()
                , Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(mColor);
        DrawUtil.drawAllPath(canvas, mPaintAttacher.getDrawInfo(), mPaintAttacher.getTransPointF());
        return bitmap;
    }

    @Override
    public ArrayList<LineInfo> getDrawInfo() {
        return mPaintAttacher.getDrawInfo();
    }

    @Override
    public ArrayList<ActionLineInfo> getUndoInfo() {
        return mPaintAttacher.getActionController().getUndoList();
    }

    @Override
    public ArrayList<ActionLineInfo> getRedoInfo() {
        return mPaintAttacher.getActionController().getRedoList();
    }

}

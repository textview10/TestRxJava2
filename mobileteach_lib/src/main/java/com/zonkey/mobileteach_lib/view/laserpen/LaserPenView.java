package com.zonkey.mobileteach_lib.view.laserpen;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.zonkey.mobileteach_lib.MobileTeach;
import com.zonkey.mobileteach_lib.MobileTeachApi;
import com.zonkey.mobileteach_lib.net.client.UdpUtil;
import com.zonkey.mobileteach_lib.util.Build32Code;
import com.zonkey.mobileteach_lib.view.drawpaint.util.ColorUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.util.TransPointF;


/**
 * Created by shaoNing.sun
 * Date on 2017/2/22 9:56
 */
public class LaserPenView extends View implements ScaleGestureDetector.OnScaleGestureListener {
    private String TAG = getClass().getSimpleName().toString();
    public final int STATE_OTHER = -1;    //原生状态
    public final int STATE_LASER_PEN = 0; //激光笔
    public final int STATE_SPOTLIGHT = 1; //聚光灯
    public Bitmap mBitmap;
    private float mX, mY;   //在屏幕上的点的坐标...
    private static final float TOUCH_TOLERANCE = 3; //移动距离
    private int bitmapWidth;
    private int bitmapHeight;
    //-------------------------------------------------
    private String lineId;  //本条划线的id
    private UdpUtil udpUtil;    //udp通讯工具
    private int currentPoint = 0;   //当前是本次绘制的第几个点
    private boolean isNet = true;   //是否向服务端发送数据
    private int topY = 0;
    private int bottomY = 0;
    private int leftX = 0;
    private int rightX = 0;
    //----------------------------------------------------------
    private int mCurrentState = STATE_OTHER;

    //圆坐标
    private float mCicleX;
    private float mCicleY;
    //圆半径
    private float mRadius = 200;
    private Paint mPaint;
    private int mWidth, mHight;

    private boolean isScaleBegin = false;

    private ScaleGestureDetector mScaleGestureDetector = null;  //缩放检查手势的类
    private TransPointF transPointF;


    public LaserPenView(Context c) {
        this(c, null);
    }

    public LaserPenView(Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }

    public LaserPenView(Context c, AttributeSet attrs, int defStyleAttr) {
        super(c, attrs, defStyleAttr);
        initalPaintView(c);
        mScaleGestureDetector = new ScaleGestureDetector(c, this);
    }

    /**
     * 初始化paintview
     *
     * @param context
     */
    private void initalPaintView(Context context) {
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(16);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(2);

        //得到屏幕的分辨率
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels;
        mRadius = bitmapWidth / 10;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        udpUtil = new UdpUtil();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCicleX = getWidth() / 2;
        mCicleY = getHeight() / 2;
        invalidate();
    }

    public void setTopAndBottom(int width, int height, int topY, int bottomY, int leftX, int rightX) {
        this.topY = topY;
        this.bottomY = bottomY;
        this.leftX = leftX;
        this.rightX = rightX;
        this.mWidth = width;
        this.mHight = height;
        this.mCicleX = leftX + (rightX - leftX) / 2;
        this.mCicleY = topY + ((bottomY - topY) / 2);
        transPointF = new TransPointF(width, height, leftX, topY);
    }

    public void setCurrentState(int state) {
        mCurrentState = state;
        if (mCurrentState == STATE_SPOTLIGHT) {
            mCicleX = leftX + (rightX - leftX) / 2;
            mCicleY = topY + ((bottomY - topY) / 2);
        }
        invalidate();
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCurrentState == STATE_SPOTLIGHT) {
            if (mCicleX == -1 || mCicleY == -1) {
                return;
            }
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(mCicleX, mCicleY, mRadius, mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
            mPaint.setColor(Color.BLACK);
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
            mPaint.setXfermode(null);
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }

    private void setCicleRadius(float scale) {
        mRadius = mRadius * scale;
        invalidate();
    }

    boolean isInCirecle = false;

    private void touch_down(float downX, float downY) {
        mX = downX;
        mY = downY;
        if (!isInCircle(downX, downY) && mCurrentState == STATE_SPOTLIGHT) {
            isInCirecle = false;
            return;
        }
        isInCirecle = true;
        PointF pointF = null;
        if (MobileTeach.CurrentApp == MobileTeach.App_Teachmaster) {
            pointF = new PointF(downX, downY);
        } else {
            pointF = transPointF.display2Logic(downX, downY);
        }
        invalidate();
        if (isNet) {
            lineId = Build32Code.createGUID();
            StringBuilder sendBody = new StringBuilder();
            if (mCurrentState == STATE_LASER_PEN) {
                sendBody.append(MobileTeachApi.PresentControl.PAINT_LASER_PEN).append("|")
                        .append(lineId).append("|")
                        .append(ColorUtil.convertColorToString(Color.TRANSPARENT)).append("|")
                        .append(7).append("|").append(pointF.x)
                        .append(",").append(pointF.y);
                udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd,
                        MobileTeachApi.PresentControl.REQUEST_NEW_SHAPE, sendBody.toString());
            }
        }
    }

    private void touch_move(float moveX, float moveY, MotionEvent event) {
        if (!isInCirecle && mCurrentState == STATE_SPOTLIGHT) {
            return;
        }
        float tempX_one = mX;
        float tempY_one = mY;
        float tempX_two = (moveX + mX) / 2;
        float tempY_two = (moveY + mY) / 2;
        mX = moveX;
        mY = moveY;
        if (!isScaleBegin) {
            mCicleX = event.getX();
            mCicleY = event.getY();
        }
        invalidate();
        PointF prePointF = transPointF.display2Logic(tempX_one, tempY_one);
        PointF pointF = transPointF.display2Logic(tempX_two, tempY_two);
        if (isNet) {
            StringBuilder sendBody1 = new StringBuilder();
            sendBody1.append(lineId).append("|")
                    .append(currentPoint).append("|").
                    append(prePointF.x).append(",").
                    append(prePointF.y).append("|").
                    append(pointF.x).append(",").
                    append(pointF.y);
            udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd,
                    MobileTeachApi.PresentControl.REQUEST_ADD_POINT, sendBody1.toString());
            currentPoint++;
        }
    }

    private void touch_up() {
        if (isNet) {
            currentPoint = 0;
        }
        if (mCurrentState == STATE_LASER_PEN) {
            udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_EndDrawing);
        }
        isInCirecle = true;
    }

    boolean isScale = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScaleGestureDetector.isInProgress()) {
                    isScale = true;
                } else {
                    isScale = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isScale = true;
                break;
            case MotionEvent.ACTION_UP:
                isScale = false;
                break;
        }

        if (event.getPointerCount() > 1) {
            if (mCurrentState == STATE_SPOTLIGHT) {
                mScaleGestureDetector.onTouchEvent(event);
            }
            return true;
        } else if (event.getPointerCount() == 1) {
            if (isScaleBegin) {
                return true;
            }
            if (isScale) {
                return true;
            }
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isNet) {
                        if (y < topY) { //如果y 小于上边界,则等于上边界
                            y = topY;
                        }
                        if (y > bottomY) {  //如果y 大于下边界,则等于下边界
                            y = bottomY;
                        }
                        if (x < leftX) {
                            x = leftX;
                        }
                        if (x > rightX) {
                            x = rightX;
                        }
                    }
                    touch_down(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isNet) {
                        if (y < topY) { //如果y 小于上边界,则等于上边界
                            y = topY;
                        }
                        if (y > bottomY) {  //如果y 大于下边界,则等于下边界
                            y = bottomY;
                        }
                        if (x < leftX) {
                            x = leftX;
                        }
                        if (x > rightX) {
                            x = rightX;
                        }
                    }
                    touch_move(x, y, event);
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        sendScaleInfo(scaleFactor, mCicleX, mCicleY);
        setCicleRadius(scaleFactor);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        isScaleBegin = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        isScaleBegin = false;
    }

    private void sendScaleInfo(float scale, float focusX, float focusY) {
        StringBuffer stringBuffer = new StringBuffer();
        PointF pointF = transPointF.display2Logic(focusX, focusY);
        stringBuffer.append(lineId).append("|").append(scale).append("|").
                append(pointF.x).append(",").append(pointF.y);
        udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd, MobileTeachApi.PresentControl.Request_ShapeScale, stringBuffer.toString());
    }


    public void openJiGuangbi(int width, int hight, int topY, int bottomY, int leftX, int rightX) {
        setTopAndBottom(width, hight, topY, bottomY, leftX, rightX);
        setCurrentState(STATE_LASER_PEN);
        if (this.getVisibility() != View.VISIBLE) {
            this.setVisibility(View.VISIBLE);
        }
    }

    public void closeJiGuanbi() {
        this.setVisibility(View.INVISIBLE);
        setCurrentState(STATE_OTHER);
    }

    public void openJuGuangdeng(int width, int hight, int topY, int bottomY, int leftX, int rightX) {
        setTopAndBottom(width, hight, topY, bottomY, leftX, rightX);
        setCurrentState(STATE_SPOTLIGHT);
        if (this.getVisibility() != View.VISIBLE) {
            this.setVisibility(View.VISIBLE);
        }
        String lineId = Build32Code.createGUID();
        StringBuilder sendBody = new StringBuilder();
        sendBody.append(MobileTeachApi.PresentControl.PAINT_SPOTLIGHT).append("|")
                .append(lineId).append("|")
                .append(ColorUtil.convertColorToString(Color.TRANSPARENT)).append("|")
                .append(7).append("|").append(mCicleX)
                .append(",").append(mCicleY);
        udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.REQUEST_NEW_SHAPE, sendBody.toString());
    }

    public void closeJuGuangDeng() {
        mRadius = bitmapWidth / 10;
        this.setVisibility(View.INVISIBLE);
        udpUtil.sendMessage(MobileTeachApi.PresentControl.Main_Cmd,
                MobileTeachApi.PresentControl.Request_EndDrawing);
        setCurrentState(STATE_OTHER);
    }

    public boolean isInCircle(float x, float y) {
        return Math.sqrt(Math.pow(Math.abs(x - mCicleX), 2) + Math.pow(Math.abs(y - mCicleY), 2)) > mRadius ? false : true;
    }
}

package com.zonkey.mobileteach_lib.view.blackboard;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.zonkey.mobileteach_lib.R;
import com.zonkey.mobileteach_lib.view.drawpaint.DrawPathCong;
import com.zonkey.mobileteach_lib.view.drawpaint.PaintImageView;
import com.zonkey.mobileteach_lib.view.laserpen.LaserPenView;

import java.util.ArrayList;

/**
 * 分享图片底部布局
 */
public class SharePictureBottomView extends FrameLayout  implements View.OnClickListener{

    public enum PaintType {
       MIN, MIDDLE, MAX
    }
    public enum EraserType {
        MIN, MIDDLE, MAX
    }
    public enum BlackboardState {
        //无状态,笔,黑板擦
        NONE, PEN, ERASER
    }

    private View mView;
    private LaserPenView mLaserPenView;
    private ImageView mPenMain, mEraser;
    private RadioGroup mPenGroup, mEraserGroup;
    private ImageButton mRevoke, mRedo;
    private View mColorView;
    private SeekBar mColorSeekBar;
    private RelativeLayout mColorLayout;
    private LinearLayout mRecorderLayout, mSaveLayout;

    private PaintImageView mPaintImageView;
    private BlackboardState mBlackboardState = BlackboardState.NONE;

    /**
     * ------------画笔---------------
     **/
    private int mCurrentColor = Color.RED;
    private Bitmap mColorSrc;
    private boolean isLaserView;

    private boolean isBlackboardMode = true;
    protected LoadDialog loadDialog;
    private ArrayList<String> mEditPictures = new ArrayList<>();
    private String filePath;

    public SharePictureBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mView = View.inflate(context, R.layout.show_picture_bottom, null);
        initView();
        initListener();
        this.addView(mView);
        loadDialog = new LoadDialog(context);
    }

    public SharePictureBottomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SharePictureBottomView(Context context) {
        this(context, null);
    }

    private void initListener() {
        mPenMain.setOnClickListener(this);
        mPenMain.setOnClickListener(this);
        mEraser.setOnClickListener(this);
        mRevoke.setOnClickListener(this);
        mRedo.setOnClickListener(this);
        mPenGroup.check(R.id.rb_b_pen_min);
        mEraserGroup.check(R.id.rb_b_eraser_middle);
    }

    private void initView() {
        mPenMain = (ImageView) mView.findViewById(R.id.img_b_pen);
        mLaserPenView = (LaserPenView) mView.findViewById(R.id.lpv_show_pic);
        mPenGroup = (RadioGroup) mView.findViewById(R.id.rg_b_pen);
        mEraser = (ImageView) mView.findViewById(R.id.img_b_eraser);
        mEraserGroup = (RadioGroup) mView.findViewById(R.id.rg_b_eraser);
        mRevoke = (ImageButton) mView.findViewById(R.id.img_b_revoke);
        mRedo = (ImageButton) mView.findViewById(R.id.blackboard_redo);
        mColorView = mView.findViewById(R.id.view_b_color);
        mColorSeekBar = (SeekBar) mView.findViewById(R.id.sk_b_color);
        mColorLayout = (RelativeLayout) mView.findViewById(R.id.rl_b_color);
        mRecorderLayout = (LinearLayout) mView.findViewById(R.id.recorder_layout);
        mSaveLayout = (LinearLayout) mView.findViewById(R.id.save_layout);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.img_b_pen) {
            setBlackboardStateNone();
            if (mBlackboardState == BlackboardState.PEN) {//关闭划线
                mPaintImageView.setScaleState();
                mBlackboardState = BlackboardState.NONE;
            } else {
                notifyBlackboardState(BlackboardState.PEN);
                mPaintImageView.setCurrentColor(mCurrentColor);
                mPaintImageView.setDrawHandWrite();
            }
        } else if (i == R.id.img_b_eraser) {
            setBlackboardStateNone();
            if (mBlackboardState == BlackboardState.ERASER) {
                mBlackboardState = BlackboardState.NONE;
            } else {
                notifyBlackboardState(BlackboardState.ERASER);
                mPaintImageView.setLineEraserState();
            }
        } else if (i == R.id.img_b_revoke) {
            mPaintImageView.undo();
        } else if (i == R.id.blackboard_redo) {
            mPaintImageView.redo();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBlackboardStateNone() {
        if (isLaserView) {
            if (mLaserPenView.getCurrentState() == mLaserPenView.STATE_LASER_PEN) {
                mLaserPenView.closeJiGuanbi();
            } else {
                mLaserPenView.closeJuGuangDeng();
            }
            isLaserView = false;
        }
        mPaintImageView.setScaleState();
        switch (mBlackboardState) {
            case NONE:      //无状态
                break;
            case PEN:       //笔
                mPenMain.setBackground(getResources().getDrawable(R.drawable.b_pen));
                mPenGroup.setVisibility(View.GONE);
                mColorLayout.setVisibility(View.GONE);
                break;
            case ERASER:    //黑板擦
                mEraser.setBackground(getResources().getDrawable(R.drawable.b_eraser));
                mEraserGroup.setVisibility(View.GONE);
                break;
        }
    }

    /***
     * 获得图片某处位置的颜色值
     * @param progress progressbar位置
     * @return 色值
     */
    private int getPixColor(int progress) {
        if (mColorSrc == null) {
            mColorSrc = BitmapFactory.decodeResource(getResources(), R.drawable.draw_color_line);
        }
        return mColorSrc.getPixel(mColorSrc.getWidth() / 100 * progress, mColorSrc.getHeight() / 2);
    }

    private void openJiGuangBiOrJuGuangDeng(int position) {
        if (mLaserPenView.getVisibility() != View.VISIBLE) {
            mLaserPenView.setVisibility(View.VISIBLE);
        }
        int height = mPaintImageView.getDrawable().getBounds().height();
        int width = mPaintImageView.getDrawable().getBounds().width();
        int topY = (mLaserPenView.getHeight() - height) / 2;
        int bottomY = topY + height;
        int leftX = (mLaserPenView.getWidth() - width) / 2;
        int rightX = leftX + width;
        if (position == mLaserPenView.STATE_LASER_PEN) {
            mLaserPenView.openJiGuangbi(width, height, topY, bottomY, leftX, rightX);
        } else if (position == mLaserPenView.STATE_SPOTLIGHT) {
            mLaserPenView.openJuGuangdeng(width, height, topY, bottomY, leftX, rightX);
        }
    }

    /**
     * 添加操作对象PaintImageView
     * @param paintImageView
     */
    public void setPaintImageView(final PaintImageView paintImageView){
        mPaintImageView = paintImageView;
        mPenGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int i = group.getCheckedRadioButtonId();
                if (i == R.id.rb_b_pen_min) {
                    mPaintImageView.setStrokeWidth(DrawPathCong.mStartSize);
                } else if (i == R.id.rb_b_pen_middle) {
                    mPaintImageView.setStrokeWidth(DrawPathCong.mMiddleSize);
                } else if (i == R.id.rb_b_pen_max) {
                    mPaintImageView.setStrokeWidth(DrawPathCong.mMaxSize);
                }
            }
        });

        mColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mCurrentColor = getPixColor(progress);
                mColorView.setBackgroundColor(mCurrentColor);
                mPaintImageView.setCurrentColor(mCurrentColor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /***
     * 设置是黑板模式还是图片模式
     * @param blackboardMode true 是黑板模式，默认是黑板模式
     */
    public void isBlackboardMode(boolean blackboardMode) {
        isBlackboardMode = blackboardMode;
        //编辑图片模式
        if (!isBlackboardMode) {
            mRecorderLayout.setVisibility(View.GONE);
            mSaveLayout.setVisibility(View.VISIBLE);
        }
    }

    /***
     * 设置画笔粗细类型
     * @param paintType
     */
    public void setPanitType(PaintType paintType){

        switch (paintType){
            case MIN:
                mPenGroup.check(R.id.rb_b_pen_min);
                break;
            case MIDDLE:
                mPenGroup.check(R.id.rb_b_pen_middle);
                break;
            case MAX:
                mPenGroup.check(R.id.rb_b_pen_max);
                break;
        }
    }
    /***
     * 设置橡皮擦大小
     * @param eraserType
     */
    public void setEraserType(EraserType eraserType){

        switch (eraserType){
            case MIN:
                mPenGroup.check(R.id.rb_b_pen_min);
                break;
            case MIDDLE:
                mPenGroup.check(R.id.rb_b_pen_middle);
                break;
            case MAX:
                mPenGroup.check(R.id.rb_b_pen_max);
                break;
        }
    }

    /***
     * 设置图片模式下截图保存路径
     * @param path
     */
    public void setPicturePath(final String path){
        filePath = path;
    }

    /***
     * 返回黑板现在的状态
     * @return
     */
    public BlackboardState getBlackboardState(){
        return mBlackboardState;
    }

    /***
     * 更新黑板模块状态
     * @param state
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void notifyBlackboardState(BlackboardState state) {
        switch (state) {
            case NONE:      //无状态
                setBlackboardStateNone();
                mBlackboardState = BlackboardState.NONE;
                break;
            case PEN:       //笔
                mPenMain.setBackground(getResources().getDrawable(R.drawable.b_pen_on));
                mPenGroup.setVisibility(View.VISIBLE);
                mColorLayout.setVisibility(View.VISIBLE);
                mBlackboardState = BlackboardState.PEN;
                break;
            case ERASER:    //黑板擦
                mEraser.setBackground(getResources().getDrawable(R.drawable.b_eraser_on));
                mEraserGroup.setVisibility(View.VISIBLE);
                mBlackboardState = BlackboardState.ERASER;
                break;
        }
    }
}

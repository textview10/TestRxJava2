package com.zonkey.mobileteach_lib.view.blackboard;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zonkey.mobileteach_lib.R;
import com.zonkey.mobileteach_lib.util.BitmapUtil;
import com.zonkey.mobileteach_lib.util.Constant;
import com.zonkey.mobileteach_lib.util.LogUtil;
import com.zonkey.mobileteach_lib.util.ToastUtil;
import com.zonkey.mobileteach_lib.view.drawpaint.DrawPathCong;
import com.zonkey.mobileteach_lib.view.drawpaint.PaintImageView;
import com.zonkey.mobileteach_lib.view.laserpen.LaserPenView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by shaoning.sun
 * Date on 2017/7/3 10:49
 */
public class BlackBoardView extends FrameLayout implements View.OnClickListener {

    private static final Rect EMPTY_RECT = new Rect();
    public static final int SAVE_OK = 110;
    public static final int CUT_OK = 111;

    public enum PaintType {
        MIN, MIDDLE, MAX
    }

    public enum EraserType {
        MIN, MIDDLE, MAX
    }

    public enum BlackboardState {
        //无状态,笔,聚光灯,激光笔,黑板擦,剪切
        NONE, PEN, SPOTLIGHT, LASER_PEN, ERASER, CUT
    }

    private View mView;
    private Context mContext;
    private LaserPenView mLaserPenView;
    private CropOverlayView mCutView;
    private TextView mRecorderTime;
    private ProgressBar mProgressBar;
    private ImageView mPenMain, mFlashlight, mLaser, mEraser, mCutImage;
    private RadioGroup mPenGroup, mEraserGroup;
    private RelativeLayout mRLCut;
    private ImageButton mRevoke, mRedo, mScreenRecorder;
    private LinearLayout blackboardBottomLayout;
    private View mColorView;
    private SeekBar mColorSeekBar;
    private RelativeLayout mColorLayout;
    private LinearLayout mRecorderLayout, mSaveLayout, mEraserLayout;

    private PaintImageView mPaintImageView;
    private BlackboardState mBlackboardState = BlackboardState.NONE;
    private Button mClearBtn;

    /**
     * ------------画笔---------------
     **/
    private int mCurrentColor = Color.RED;
    private Bitmap mColorSrc;
    private boolean isLaserView;

    private boolean isBlackboardMode = true;
    protected LoadDialog loadDialog;
    private ArrayList<String> mEditPictures = new ArrayList<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SAVE_OK:
                    loadDialog.dismiss();
                    ToastUtil.showToast(mContext, "保存成功");
                    break;
                case CUT_OK:
                    loadDialog.dismiss();
                    ToastUtil.showToast(mContext, "截图成功");
                    break;
            }
        }
    };

    private ScreenRecorderListener mBlackboardBottomView;
    private String filePath;

    public BlackBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mView = View.inflate(context, R.layout.paintimageview_bottom, null);
        initView();
        initListener();
        this.addView(mView);
        mContext = context;
        loadDialog = new LoadDialog(context);
    }

    public BlackBoardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlackBoardView(Context context) {
        this(context, null);
    }

    private void initListener() {
        mPenMain.setOnClickListener(this);
        mPenMain.setOnClickListener(this);
        mFlashlight.setOnClickListener(this);
        mLaser.setOnClickListener(this);
        mEraser.setOnClickListener(this);
        mCutImage.setOnClickListener(this);
        mRevoke.setOnClickListener(this);
        mRedo.setOnClickListener(this);
        mScreenRecorder.setOnClickListener(this);
        mSaveLayout.setOnClickListener(this);
        mPenGroup.check(R.id.rb_b_pen_min);
        mEraserGroup.check(R.id.rb_b_eraser_middle);
        mClearBtn.setOnClickListener(this);
    }

    private void initView() {
        mEraserLayout = (LinearLayout) mView.findViewById(R.id.rg_b_eraser_layout);
        mPenMain = (ImageView) mView.findViewById(R.id.img_b_pen);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progressConnecting);
        mLaserPenView = (LaserPenView) mView.findViewById(R.id.lpv_show_pic);
        mPenGroup = (RadioGroup) mView.findViewById(R.id.rg_b_pen);
        mFlashlight = (ImageView) mView.findViewById(R.id.img_b_flashlight);
        mLaser = (ImageView) mView.findViewById(R.id.img_b_laser);
        mEraser = (ImageView) mView.findViewById(R.id.img_b_eraser);
        mCutView = (CropOverlayView) mView.findViewById(R.id.cut_view);
        mRLCut = (RelativeLayout) mView.findViewById(R.id.rl_cut);
        mEraserGroup = (RadioGroup) mView.findViewById(R.id.rg_b_eraser);
        mClearBtn = (Button) mView.findViewById(R.id.rb_b_eraser_full);
        mCutImage = (ImageView) mView.findViewById(R.id.img_b_cut);
        mRevoke = (ImageButton) mView.findViewById(R.id.img_b_revoke);
        mRedo = (ImageButton) mView.findViewById(R.id.blackboard_redo);
        blackboardBottomLayout = (LinearLayout) mView.findViewById(R.id.blackboard_bottom_layout);
        mColorView = mView.findViewById(R.id.view_b_color);
        mColorSeekBar = (SeekBar) mView.findViewById(R.id.sk_b_color);
        mColorLayout = (RelativeLayout) mView.findViewById(R.id.rl_b_color);
        mRecorderLayout = (LinearLayout) mView.findViewById(R.id.recorder_layout);
        mScreenRecorder = (ImageButton) mView.findViewById(R.id.btn_screen_recorder);
        mRecorderTime = (TextView) mView.findViewById(R.id.text_recorder_time);
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
        } else if (i == R.id.img_b_flashlight) {
            setBlackboardStateNone();
            if (mBlackboardState == BlackboardState.SPOTLIGHT) {
                mBlackboardState = BlackboardState.NONE;
            } else {
                isLaserView = true;
                notifyBlackboardState(BlackboardState.SPOTLIGHT);
                openJiGuangBiOrJuGuangDeng(mLaserPenView.STATE_SPOTLIGHT);
            }
        } else if (i == R.id.img_b_laser) {
            setBlackboardStateNone();
            if (mBlackboardState == BlackboardState.LASER_PEN) {
                mBlackboardState = BlackboardState.NONE;
            } else {
                isLaserView = true;
                notifyBlackboardState(BlackboardState.LASER_PEN);
                openJiGuangBiOrJuGuangDeng(mLaserPenView.STATE_LASER_PEN);
            }
        } else if (i == R.id.img_b_eraser) {
            setBlackboardStateNone();
            if (mBlackboardState == BlackboardState.ERASER) {
                mBlackboardState = BlackboardState.NONE;
            } else {
                notifyBlackboardState(BlackboardState.ERASER);
                mPaintImageView.setLineEraserState();
                mEraserGroup.check(R.id.rb_b_eraser_middle);
            }
        } else if (i == R.id.rb_b_eraser_full) {
            mPaintImageView.clear();
            mEraserGroup.check(R.id.rb_b_eraser_middle);
        } else if (i == R.id.img_b_cut) {
            setBlackboardStateNone();
            if (mBlackboardState == BlackboardState.CUT) {
                mBlackboardState = BlackboardState.NONE;
                Bitmap bitmap = mCutView.getCroppedImage(Bitmap.createBitmap(mPaintImageView.getBitmap()), mPaintImageView);
                if (isBlackboardMode) {
                    ScreenshotDialog screenshotDialog = new ScreenshotDialog(mContext, bitmap);
                    screenshotDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mPaintImageView.setDrawingCacheEnabled(true);
                        }
                    });
                    screenshotDialog.show();
                    mCutView.setBitmapRect(EMPTY_RECT);
                    mRLCut.setVisibility(View.GONE);
                    mCutView.setVisibility(View.GONE);
                    mPaintImageView.setDrawingCacheEnabled(false);
                } else {
                    //图片编辑模式
                    mCutView.setBitmapRect(EMPTY_RECT);
                    mRLCut.setVisibility(View.GONE);
                    mCutView.setVisibility(View.GONE);
                    mPaintImageView.setDrawingCacheEnabled(false);
                    // TODO: 2017/5/10
                    saveCutPictures(bitmap);
                    mPaintImageView.setDrawingCacheEnabled(true);
                }
            } else {
                notifyBlackboardState(BlackboardState.CUT);
            }
        } else if (i == R.id.img_b_revoke) {
            mPaintImageView.undo();
        } else if (i == R.id.blackboard_redo) {
            mPaintImageView.redo();
        } else if (i == R.id.btn_screen_recorder) {
            if (Constant.isRecording) {
                stopScreenRecording();
                statusIsStoped();
                Toast.makeText(mContext, "结束录制", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "开始录制", Toast.LENGTH_SHORT).show();
                startScreenRecording();
            }
        } else if (i == R.id.save_layout) {// TODO: 2017/5/10
            saveEditedPictures();

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
        if (mBlackboardState != BlackboardState.CUT && mRLCut.getVisibility() == View.VISIBLE) {
            mCutView.setBitmapRect(EMPTY_RECT);
            mRLCut.setVisibility(View.GONE);
            mCutView.setVisibility(View.GONE);
        }
        switch (mBlackboardState) {
            case NONE:      //无状态
                break;
            case PEN:       //笔
                mPenMain.setBackground(getResources().getDrawable(R.drawable.b_pen));
                mPenGroup.setVisibility(View.GONE);
                mColorLayout.setVisibility(View.GONE);
                break;
            case SPOTLIGHT: //聚光灯
                mFlashlight.setBackground(getResources().getDrawable(R.drawable.b_flashlight));
                break;
            case LASER_PEN: //激光笔
                mLaser.setBackground(getResources().getDrawable(R.drawable.b_laser));
                break;
            case ERASER:    //黑板擦
                mEraser.setBackground(getResources().getDrawable(R.drawable.b_eraser));
                mEraserLayout.setVisibility(View.GONE);
                break;
            case CUT:       //剪切
                mCutImage.setBackground(getResources().getDrawable(R.drawable.b_cut));
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

    private void saveCutPictures(final Bitmap bitmap) {
        loadDialog.setText("截图中");
        loadDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String picName = String.valueOf(System.currentTimeMillis());
                String path = filePath + picName;
                saveToSD(bitmap, filePath, picName);
                mEditPictures.add(0, path);
                mHandler.sendEmptyMessage(CUT_OK);
            }
        }).start();
    }

    /**
     * 关闭屏幕录制，即停止录制Service
     */
    private void stopScreenRecording() {
        // TODO Auto-generated method stub
        Constant.isRecording = false;
        mBlackboardBottomView.stopScreenRecorder();
    }

    /**
     * 结束屏幕录制后的UI状态
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void statusIsStoped() {
        mRecorderTime.setText("00:00");
        mScreenRecorder.setBackground(getResources().getDrawable(R.drawable.b_play));
    }

    /***
     * 开始屏幕录制
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void startScreenRecording() {
        Constant.isRecording = true;
        mScreenRecorder.setBackground(getResources().getDrawable(R.drawable.b_stop));
        mBlackboardBottomView.startScreenRecorder();
    }

    private void saveEditedPictures() {
        loadDialog.setText("保存中");
        loadDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(mPaintImageView.getDrawingCache());
                String picName = String.valueOf(System.currentTimeMillis());
                String path = filePath + picName;
                saveToSD(bitmap, filePath, picName);
                mEditPictures.add(0, path);
                mHandler.sendEmptyMessage(SAVE_OK);
            }
        }).start();
    }

    private boolean saveToSD(Bitmap bmp, String dirName, String fileName) {
        if (Environment.getExternalStorageState().equals(    // 判断sd卡是否存在
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dirName, fileName);
            FileOutputStream fos = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                if (fos != null) {
                    // 第一参数是图片格式，第二个是图片质量，第三个是输出流
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                }
                return true;
            } catch (Exception e) {
                LogUtil.e("创建图片失败", e.toString());
                LogUtil.writeLog("创建图片失败", e.toString());
                return false;
            }
        } else {
            return false;
        }
    }

    /*********************************************************************************************************/

    /**
     * 添加操作对象PaintImageView
     *
     * @param paintImageView
     */
    public void setPaintImageView(final PaintImageView paintImageView) {
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
        mEraserGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int i = group.getCheckedRadioButtonId();
                setEraser(i);
            }
        });

        mCutView.setOnCancelListener(new CropOverlayView.OnCancelListener() {
            @Override
            public void onCancel() {
                setBlackboardStateNone();
                mBlackboardState = BlackboardState.NONE;
                ToastUtil.showToast(mContext, "取消截图");
                mCutView.setBitmapRect(EMPTY_RECT);
                mRLCut.setVisibility(View.GONE);
                mCutView.setVisibility(View.GONE);
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

    public void setEraser(final int id) {
        if (id == R.id.rb_b_eraser_middle) {
            mPaintImageView.setLineEraserState();
        } else if (id == R.id.rb_b_eraser_max) {
            mPaintImageView.setEraserRect();
        } /*else if (id == R.id.rb_b_eraser_full) {

        }*/
    }

    /***
     * 设置录制屏幕时间
     * @param time
     */
    public void setRecorderTime(String time) {
        mRecorderTime.setText(time);
    }

    /***
     * 设置是黑板模式还是图片模式
     * @param blackboardMode true 是黑板模式，默认是黑板模式
     */
    public void isBlackboardMode(boolean blackboardMode) {
        isBlackboardMode = blackboardMode;
        //编辑图片模式
        if (!isBlackboardMode) {
            mFlashlight.setVisibility(View.GONE);
            mLaser.setVisibility(View.GONE);
            mRecorderLayout.setVisibility(View.GONE);
            mSaveLayout.setVisibility(View.INVISIBLE);
        }
    }

    /***
     *
     * @param screenRecorderListener
     */
    public void setScreenRecorderListener(ScreenRecorderListener screenRecorderListener) {
        mBlackboardBottomView = screenRecorderListener;
    }

    public interface ScreenRecorderListener {
        /***
         * 申请权限，开始录制屏幕
         */
        void startScreenRecorder();

        /***
         * 停止录制屏幕
         */
        void stopScreenRecorder();
    }

    /***
     * 获取保存的截图路径
     * @return
     */
    public ArrayList<String> getEditPictures() {
        return mEditPictures;
    }

    /***
     * 设置画笔粗细类型
     * @param paintType
     */
    public void setPanitType(PaintType paintType) {

        switch (paintType) {
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
    public void setEraserType(EraserType eraserType) {

        switch (eraserType) {
            case MIN:
                mEraserGroup.check(R.id.rb_b_eraser_middle);
                break;
            case MIDDLE:
                mEraserGroup.check(R.id.rb_b_eraser_max);
                break;
            case MAX:
                mPaintImageView.clear();
                mEraserGroup.check(R.id.rb_b_eraser_middle);
                break;
        }
    }

    /***
     * 设置图片模式下截图保存路径
     * @param path
     */
    public void setPicturePath(final String path) {
        filePath = path;
    }

    /***
     * 返回黑板现在的状态
     * @return
     */
    public BlackboardState getBlackboardState() {
        return mBlackboardState;
    }

    /***
     * 是否在录制屏幕
     * @return
     */
    public boolean isScreenRecording() {
        return Constant.isRecording;
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
            case SPOTLIGHT: //聚光灯
                mFlashlight.setBackground(getResources().getDrawable(R.drawable.b_flashlight_on));
                mBlackboardState = BlackboardState.SPOTLIGHT;
                break;
            case LASER_PEN: //激光笔
                mLaser.setBackground(getResources().getDrawable(R.drawable.b_laser_on));
                mBlackboardState = BlackboardState.LASER_PEN;
                break;
            case ERASER:    //黑板擦
                mEraser.setBackground(getResources().getDrawable(R.drawable.b_eraser_on));
                mEraserLayout.setVisibility(View.VISIBLE);
                mBlackboardState = BlackboardState.ERASER;
                break;
            case CUT:       //剪切
                mCutImage.setBackground(getResources().getDrawable(R.drawable.b_cut_on));
                mBlackboardState = BlackboardState.CUT;
                mRLCut.setVisibility(View.VISIBLE);
                mCutView.setVisibility(View.VISIBLE);
                Bitmap mBitmap = BitmapUtil.drawableToBitmap(mPaintImageView.getDrawable(),getContext());
                if (mBitmap == null) {
                    Log.i("zonekey", "bitmap is null");
                    return;
                }
                final Rect bitmapRect = ImageViewUtil.getBitmapRectCenterInside(mBitmap, mPaintImageView);
                mCutView.setBitmapRect(bitmapRect);
                break;
        }
    }
}

package com.zonkey.mobileteach_lib.view.drawpaint.interf;

import com.zonkey.mobileteach_lib.view.drawpaint.bean.LineInfo;
import com.zonkey.mobileteach_lib.view.drawpaint.util.TransPointF;

/**
 * Created by xu.wang
 * Date on 2017/8/30 14:24
 * PaintAttacher逻辑和View之间的回调
 */

public interface OnDrawListener {
    void drawLine(LineInfo lineInfo, TransPointF transPointF);

    void refreshCanvasBitmap();
}

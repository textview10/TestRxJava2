package com.zonkey.mobileteach_lib.view.blackboard;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.zonkey.mobileteach_lib.R;


/**
 * Created by xu.wang
 * Date on 2017/4/27 14:56
 */
public class ScreenshotDialog extends Dialog {
    private ImageView iv_blur;
    private Bitmap bitmap;

    public ScreenshotDialog(Context context, Bitmap bitmap) {
        super(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_home);
        initialView();
    }

    private void initialView() {
        iv_blur = (ImageView) findViewById(R.id.iv_blur);
        if (bitmap == null) {
            Log.e("bitmap","为null");
        } else {
            iv_blur.setImageBitmap(bitmap);
        }
        this.setCancelable(true);
    }

}

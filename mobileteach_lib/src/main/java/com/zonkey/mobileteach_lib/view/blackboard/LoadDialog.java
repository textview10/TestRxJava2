package com.zonkey.mobileteach_lib.view.blackboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zonkey.mobileteach_lib.R;


/**
 * Created by Administrator on 2017/3/27.
 */
public class LoadDialog {

    public Context mContext;
    public Dialog dialog;
    private View view;
    private TextView mLoadTv;

    public LoadDialog(Context context) {
        this.mContext = context;
        view = View.inflate(context, R.layout.dialog_loading, null);
        dialog = new AlertDialog.Builder(context).setView(view).create();
        mLoadTv = (TextView) view.findViewById(R.id.tv_load);
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    dismiss();
                }
                return false;
            }
        });

    }

    public void show() {
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DensityUtils.dp2px(mContext, 200);
        params.height = DensityUtils.dp2px(mContext, 200);
        window.setAttributes(params);
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void setText(String s) {
        mLoadTv.setText(s);
    }


}

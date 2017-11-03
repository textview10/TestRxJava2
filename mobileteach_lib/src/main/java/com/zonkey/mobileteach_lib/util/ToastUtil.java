package com.zonkey.mobileteach_lib.util;

import android.content.Context;
import android.widget.Toast;

import com.zonkey.mobileteach_lib.MobileTeach;


/**
 * Created by xu.wang
 * Date on 2016/6/1 09:22
 */
public class ToastUtil {
    private static Toast toast;

    public static void showToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            //如果toast不为空，则直接更改当前toast的文本
            toast.setText(text + "");
        }
        toast.show();

    }

    public static void showToast(String text) {
        if (toast == null) {
            toast = Toast.makeText(MobileTeach.AppContext, text, Toast.LENGTH_SHORT);
        } else {
            //如果toast不为空，则直接更改当前toast的文本
            toast.setText(text + "");
        }
        toast.show();

    }

    public static void longToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            //如果toast不为空，则直接更改当前toast的文本
            toast.setText(text + "");
        }
        toast.show();

    }
}

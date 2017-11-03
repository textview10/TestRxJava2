package com.zonkey.mobileteach_lib.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.zonkey.mobileteach_lib.MobileTeach;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xu.wang
 * Date on 2016/6/1 09:21
 */
public class LogUtil {
    //是否打印log到logcat
    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void i(Object object, String msg) {
        Log.i(object.getClass().getSimpleName(), msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(Object object, String msg) {
        Log.e(object.getClass().getSimpleName(), msg);
    }

    public static void e(String msg) {
        Log.e("----------->", msg);
    }

    /**
     * 超过4000个字符时打印所有字符
     */
    public static void logMore(String log_content) {
        if (log_content.length() > 4000) {
            int chunkCount = log_content.length() / 4000;     // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= log_content.length()) {
                    Log.e("------>", log_content.substring(4000 * i));
                } else {
                    Log.e("------>", log_content.substring(4000 * i, max));
                }
            }
        } else {
            Log.e("------>", log_content.toString());
        }
    }

    /**
     * 向本地文件写日记
     *
     * @param TAG     标记
     * @param message 信息
     */
    public static void writeLog(String TAG, String message) {
        save2File(TAG + "---" + message);
    }

    //是否保存日志到log文件,为true时才保存
    private static boolean SAVE_2_FILE = true;
    private static File currentLogFile;

    public static void init(Context context) {
        if (SAVE_2_FILE) {
            File logDir = new File(MobileTeach.localLogFile);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            cleanOldLog();
            //创建本次启动log文件
            currentLogFile = new File(logDir, DateUtil.getFileNameDate() + ".txt");
            try {
                currentLogFile.createNewFile();
                writeLog("initial Application", "The device model is " + Build.MODEL + "The version_sdk is " + Build.VERSION.SDK
                        + "The device release is " + Build.VERSION.RELEASE + "versionCode" + getVersionCode(context));
            } catch (IOException e) {
                Log.e("TAG", e.toString());
            }
        }
    }

    private static int getVersionCode(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionCode;
    }

    public static void save2File(String log) {
        if (!SAVE_2_FILE)
            return;
        if (currentLogFile != null) {
            writeStr2File(currentLogFile.getPath(), getDateStr() + "   " + log);
        } else {
            Log.e("wx", "log file is null");
        }
    }


    /**
     * 保留最近十次日志
     */
    public static void cleanOldLog() {
        final File logDir = new File(MobileTeach.localLogFile);
        if (logDir == null || !logDir.exists()) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                cleanLog(logDir);
            }
        }.start();
    }

    //递归清除10次前的日志
    private static boolean cleanLog(File logDir) {
        long[] max = new long[2];
        File[] files = logDir.listFiles();
        if (files == null || files.length <= 10) return true;
        max[0] = System.currentTimeMillis();
        for (int i = 0; i < files.length; i++) {
            if (files[i].lastModified() < max[0]) {
                max[0] = files[i].lastModified();
                max[1] = i;
            }
        }
        files[(int) max[1]].delete();
        return cleanLog(logDir);
    }

    /**
     * 获取当前系统时间，
     *
     * @return 格式2016-06-29 11:58:31
     */
    public static String getDateStr() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return format1.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 追加写入文件：使用FileWriter,写入后自动换行
     *
     * @param filePath 文件绝对路径
     * @param content  写入Str
     */
    private static synchronized void writeStr2File(String filePath, String content) {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(content)) return;
        try {
            //追加形式写文件
            FileWriter writer = new FileWriter(filePath, true);
            writer.write(content);
            writer.write("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LogUtil.e("LogUtil", e.toString());
        }
    }

}

package com.zonkey.mobileteach_lib;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.zonkey.mobileteach_lib.interf.OnAppQuitListener;
import com.zonkey.mobileteach_lib.net.AcceptMsgManager;
import com.zonkey.mobileteach_lib.util.LogUtil;

/**
 * Created by xu.wang
 * Date on 2017/6/1 13:53
 * 智客助手,智客互动教师端,智客助手学生端
 * 共同使用的局域网通讯,画板绘制模块的代码...
 */

public class MobileTeach {
    public static Context AppContext;
    public static Handler handler;
    public static String pc_ip;
    public static int pc_udp_port= 0;
    public static int pc_tcp_port;
    public static int pc_file_port;
    //----------------------记录当前app是--------------------------
    public static int App_Teachmaster = 1;      //智客助手
    public static int App_MobileTeach_Teacher = 2;  //智客互动老师端
    public static int App_MobileTeach_Student = 3;  //智客互动学生端
    //-------------------------------------------------------------
    public static int local_file_port;  //本地文件端口
    public static int local_udp_port ;  //本地udp端口
    public static int local_tcp_port;   //本地tcp端口
    //-------------------------------------------------------------------
    public static int CurrentApp;       //当前是那个App
    public static byte AppCode;
    public static String localCacheDir;
    public static String localLogFile;
    public static OnAppQuitListener mListener;
    public static Class LoginActivity;
    /**
     * 初始化MobileTeach
     * @param application
     * @param CURRENT_APP   当前App的code,智客助手为1,智客分析老师端为2,智客分析学生端为3
     */
    public static void init(final Application application, int CURRENT_APP) {
        AppContext = application;
        handler = new Handler(application.getMainLooper());
        CurrentApp = CURRENT_APP;
        if (CURRENT_APP == App_Teachmaster) {
            local_file_port = MobileTeachConfig.teachmaster_file_port;
            local_tcp_port = MobileTeachConfig.teachmaster_tcp_port;
            local_udp_port = MobileTeachConfig.teachmaster_udp_port;
            AppCode = MobileTeachApi.AppCode.MobileTeach;
            localCacheDir = MobileTeachConfig.teachmaster_dir + "cache/";
            localLogFile = MobileTeachConfig.teachmaster_dir + "log/";
        } else if (CURRENT_APP == App_MobileTeach_Teacher) {
            local_file_port = MobileTeachConfig.teacher_file_port;
            local_tcp_port = MobileTeachConfig.teacher_tcp_port;
            local_udp_port = MobileTeachConfig.teacher_udp_port;
            AppCode = MobileTeachApi.AppCode.EasyBoard_Teacher;
            localCacheDir = MobileTeachConfig.teacher_dir + "cache/";
            localLogFile = MobileTeachConfig.teacher_dir + "log/";
        } else if (CURRENT_APP == App_MobileTeach_Student) {
            local_file_port = MobileTeachConfig.stu_file_port;
            local_tcp_port = MobileTeachConfig.stu_tcp_port;
            local_udp_port = MobileTeachConfig.stu_udp_port;
            AppCode = MobileTeachApi.AppCode.EasyBoard_Student;
            localCacheDir = MobileTeachConfig.student_dir + "cache/";
            localLogFile = MobileTeachConfig.student_dir + "log/";
        }
        LogUtil.init(application.getApplicationContext());
    }

    /**
     * 登录成功(获取pc的各端口信息)后,给MobileTeach赋值,并且重新打开心跳校验线程
     * @param pcIp      pc的Ip地址
     * @param pcUdpPort pc接收Udp消息的端口号
     * @param pcTcpPort pc接收Tcp消息的端口号
     * @param pcFilePort    pc的接收文件的端口号
     */
    public static void setPcInfo(String pcIp, int pcUdpPort, int pcTcpPort, int pcFilePort) {
        pc_ip = pcIp;
        pc_udp_port = pcUdpPort;
        pc_tcp_port = pcTcpPort;
        pc_file_port = pcFilePort;
        AcceptMsgManager.getInstance().startHeartBeat();    //开启心跳线程;
    }

    /**
     * 心跳机制,断开后的回调,用于帮助App做断开连接的逻辑,没有向pc端发送退出udp消息
     * 智客助手和智客互动老师端才有心跳机制,智客互动学生端没有心跳机制..
     * @param activity
     * @param listener
     */
    public static void setOnAppQuitListener(Class activity,OnAppQuitListener listener){
        LoginActivity = activity;
        mListener = listener;
    }

    /**
     * 教师端退出登录,返回登录界面时候调用,关闭心跳线程,防止心跳线程错误
     */
    public static void logOut(){
        AcceptMsgManager.getInstance().stopHeartBeat();
    }

    /**
     * 退出程序
     */
    public static void quit(){
        AcceptMsgManager.getInstance().stopServer();
    }
}

package com.zonkey.mobileteach_lib;

/**
 * Created by xu.wang
 * Date on 2017/6/1 14:19
 */
public class MobileTeachApi {
    /*#########################与window客户端通信使用#######################################*/
    /*--------------------------与设备相关的常量------------------------------------------------*/
    public static final byte encodeVersion1 = 0x00;       //版本号1
    public static final byte encodeVersion2_v0 = 0x00;    //版本号,第一版数据格式
    public static final byte encodeVersion2_v1 = 0x01;    //版本号,第二版数据格式
    public static final byte TransferVersion_v0 = 0x00;    //文件传输的版本号,第一版数据格式
    public static final byte TransferVersion_v1 = 0x01;    //文件传输的版本号,第二版数据格式
    public static final byte MACHINE_TYPE = 0x42; //发送设备类型指令,表明来自Android_phone

    //AppCode
    public class AppCode {
        public static final byte MobileTeach = 0x21;
        public static final byte EasyBoard_Pc = 0x22;  //智客黑板pc
        public static final byte EasyBoard_Teacher = 0x23;  //智客互动教师端
        public static final byte EasyBoard_Student = 0x24;  //智客互动学生端
    }

    //------------------------用户控制指令-----------------------------------
    public class UserControll {
        public static final short Main_Cmd = 0x11;  //用户控制主指令
        public static final short REQUEST_LOGIN = 0x01;                  //用户登录
        public static final short REQUEST_LOGOUT = 0x02;                  //用户登出
        public static final short Response_LoginSuccess = 0xF1;     //登录成功
        public static final short Response_LoginFail = 0xF2;        //登录失败
        public static final short Command_Logout = 0x21;        //服务端踢出
        public static final short Response_NeedLogin = 0x41;        //需要再次登录
    }

    public class File {
        public static final byte REQUEST_FILE = 0x20;
    }

    //------------------------心跳包的指令--------------------------------------
    public class NetworkLayer {
        public static final short Main_Cmd = 0x10;  //用于处理系统信息（心跳，基础信息变更）
        public static final short Request_AskServerName = 0x02;        //询问服务端用户名//询问serverName
        public static final short Response_AskServerName = 0x82;        //回应服务端用户名
        public static final String REQUEST_NET_IP = "255.255.255.255";  //向局域网内所有在线ip发出消息
        public static final short Request_AskHeartBeat = 0x01;  //发送方的子指令
        public static final short Response_AskHeartBeat = 0x81;  //回应方的子指令
        public static final short Request_SwitchLongConnect = 0x10;  //切换tcp长连接
        public static final short Request_TcpClose = 0x11;  //关闭tcp
        public static final short Request_GetAppVersion = 0x04;       //询问对方版本号
        public static final short Response_GetAppVersion = 0x05;      //回应版本号询问
    }

    //-----------状态控制指令-----------------------------------------------------------------
    public class PresentControl {
        public static final short Main_Cmd = 0xA0;
        public static final short REQUEST_PLAY = 0x01;    //请求播放图片
        public static final short REQUEST_PAUSE = 0x02;   //请求暂停播放
        public static final short REQUEST_RESUME = 0x03;  //解除暂停播放
        public static final short REQUEST_STOP = 0x04;  //停止播放
        public static final short REQUEST_NEXT_STEP = 0x05;  //下一步
        public static final short REQUEST_NEXT_PAGE = 0x06;  //下一页
        public static final short Request_PreviousStep = 0x07;  //上一步
        public static final short Request_PreviousPage = 0x08;  //上一页
        public static final short Request_FirstPage = 0x09;  //第一页
        public static final short Request_LastPage = 0x10;  //最后一页一页
        public static final short Request_PlayToPage = 0x0B;  //跳转某页
        public static final short REQUEST_GET_PAGE_COUNT = 0x21;              //

        //############# 接受文档指令####################################################
        public static final short RESPONSE_NEXT = 0xF3;
        public static final short RESPONSE_COMPLETE = 0xF4;
        public static final short Response_PageChanged = 0x22;      //切换页面的时候发送
        public static final short Response_Pic_PageChanged = 0xB;      //图片翻页的指令
        public static final short AskAllPagesByFileName = 0x30;       //请求所有文件
        public static final short Request_AdkOnePageByName = 0x32;       //根据名字请求文件

        //坐标系的控制及数据交互
        public static final short Request_Scale = 0x82;       //请求放大
        public static final short Request_Translate = 0x81;       //请求移动图片
        public static final short REQUEST_RESET_CORDINATE = 0x80; //重新定义边界
        public static final short Request_ResetTransform = 0x8F;  //重置图片逻辑
        public static final short REQUEST_NEW_SHAPE = 0x90;              //绘制新图形
        public static final short REQUEST_ADD_POINT = 0x91;              //增加新的点
        public static final short Request_EndDrawing = 0x92;  //发送此指令来结束当前操作

        public static final short Request_Rotate = 0x83;                 //旋转
        //页面内划线的动作
        public static final short Request_Undo = 0xA0;                    //删除线
        public static final short Request_Redo = 0xA1;                    //恢复划线
        public static final short Request_Clear = 0xA2;                   //清空划线
        public static final short Response_Stopped = 0x24;      //播放完毕/程序崩溃的时候发送

        //绘制线条的类型
        public static final String QuadraticBezier = "QuadraticBezier";               //绘制线的类型,贝塞尔曲线
        public static final String PAINT_LASER_PEN = "LaserLight";               //绘制线的类型,激光笔
        public static final String PAINT_SPOTLIGHT = "FocusLight";               //绘制线的类型,聚光灯
        public static final String PAINT_DOT = "Dot";                             //绘制线的类型,点
        public static final String PAINT_STRAIGHT_LINE = "StraightLine";         //绘制线的类型,直线
        public static final String PAINT_ARROW_LINE = "ArrowLine";               //绘制线的类型,带箭头的直线
        public static final String PAINT_MARK_LINE = "MarkLine";                 //绘制线的类型,荧光笔、批注笔
        public static final String PAINT_POLY_LINE = "Polyline";                 //绘制线的类型,折线、自由曲线
        public static final String PAINT_RECTANGLE = "Rectangle";                //绘制线的类型,矩形、正方形
        public static final String PAINT_ELLIPSE = "Ellipse";                    //绘制线的类型,椭圆、圆
        public static final String PAINT_CENTER_CIRCLE = "CenterCircle";         //绘制线的类型,鼠标按下点为圆心的圆
        public static final String PAINT_RECTANGLE_CIRCLE = "RectangleCircle";   //绘制线的类型,鼠标按下点和当前点矩形的内切圆
        public static final short Request_ShapeScale = (byte) 0x99; //缩放聚光灯
        public static final String PAINT_TIANZIGE = "TianZiGe";                    //米字格
        public static final String PAINT_MIZIGE = "MiZiGe";                    //米字格
        public static final String PAINT_SiXianGE = "SiXianGE";                    //四线格
    }

    public class MiniPresentControll {  // note 获取黑板的信息
        public static final short Main_Cmd = 0x01A6;
        public static final short Request_AllMiniBoard = 0x01; //获得全部迷你黑板的信息
        public static final short Request_MiniBoard = 0x02;     //获得单个迷你黑板的信息
        public static final short Request_AddMiniBoard = 0x04;     //获得单个迷你黑板的信息
        public static final short Request_CloseMiniBoard = 0x05;     //关闭投屏
        public static final short Response_AllMiniBoard = 0x21;  //回应获得全部迷你黑板的信息
        public static final short Response_MiniBoard = 0x22;     //回应获得单个迷你黑板的信息
    }

    public class MiniBoardDrawing extends PresentControl {
        public static final short Main_Cmd = 0x01A7;
    }

    public class PicHost {  //图片房间
        public static final short Main_Cmd = 0xA0;
        public static final short Request_HostBegin = 0xC0;   //请求创建房间
        public static final short Request_HostAppend = 0xC1;  //请求增加图片
        public static final short Request_HostRemove = 0xC2;  //请求移除房间
        public static final short Command_HostUpdated = 0xC3;  //请求更新房间
        public static final short Request_HostCheckState = 0xC4;  //检查能否上传图片
        public static final short Response_HostStateInfo = 0xCE;   //处于host状态
        public static final short Response_HostStateAbnormal = 0xCF;   //不在host状态

        public static final short Response_HostConfirmed = 0xC8;  //可以创建房间
        public static final short Response_HostDenied = 0xC9;     //不可以创建房间
        public static final short Response_HostAppended = 0xCA;   //可以增加房间
        public static final short Response_HostAppendFail = 0xCB;    //不可以增加房间
        public static final short Response_HostRemoved = 0xCC;        //移除房间成功
        public static final short Response_HostRemoveFail = 0xCD;     //移除房间失败
    }

    //-------------------录屏命令-----------------------------------------------------------
    public class RECORD {   //录屏指令
        public static final short MAIN_CMD = 0xA2; //主指令
        public static final short RECORDER_REQUEST_START = 0x01; //单投屏，请求开始
        public static final short RECORDER_REQUEST_STOP = 0x02;   //请求结束
        public static final short Request_StartWithParameters = 0x03;    //会返回更详细信息的多投屏,
        public static final short RECORDER_REQUEST_STATE = 0x20;  //请求状态,如果成功返回Url连接字符串，失败返回空
        public static final short RECORDER_REQUEST_HOSTSTART = 0x10; //屏幕数量（2或4）多投屏
        public static final short Request_HostStop = 0x12; //请求结束多投屏
        public static final short RECORDER_RESPONSE_STOP = 0x21;   //请求停止
        public static final short RECORDER_RESPONSE_HOST_START_CONFIRMED = 0x15; //回复信息，代表可以作为主机
        public static final short RECORDER_RESPONSE_HOST_START_DENIED = 0x16; //回复信息，代表不可以作为主机
        public static final short Command_HostUpdated = 0x1C; //{IP}:{Name}|{IP}:{Name}…投屏者信息
        public static final short Response_HostStopped = 0x18; //响应结束多投屏
        public static final short Response_HostStopDenied = 0x19; // 无法结束多投屏，不是当前主机
        public static final short Request_HostGetState = 0x11; //向PC端发出请求，寻问当前投屏主机
        public static final short Response_HostGetState = 0x17; //PC端回应，告诉当前投屏主机
    }

    //--------------文件列表指令-------------------------------
    public class FILE_LIST {
        public static final short MAIN_CMD = 0x21;      //文件列表,主指令
        //正在演示
        public static final short Request_GetOpenedDocuments = 0x06;     //请求获取列表
        public static final short Request_PresentOpenedDocument = 0x07;      //请求演示已经打开的文档
        //其他文件
        public static final short Request_GetFolderContent = 0x01;          //获得某个文件夹下的内容
        public static final short Request_GotoParentFolder = 0x02;          //去上一级的目录
        public static final short Request_OpenFileInCurrentFolder = 0x03;       //打开某个文件
        //已上传
        public static final short Request_GetWorkFolderContent = 0x04;      //打开已上传的目录
        public static final short Request_OpenFileInWorkFolder = 0x05;       //打开已上传的某个文件
        public static final short Response_ReadyForOpen = 0x81;      //文件准备完成,可以打开
        public static final short Response_GetOpenedDocuments = 0x84;      //获取已经打开的文档的子指令
        public static final short Response_GetWorkFolderContent = 0x83; //获取已上传的子指令
        public static final short Response_GetFolderContent = 0x82;  //获取其他文件的子指令
    }

    //视频交互指令
    public class VIDEO_PLAY {
        public static final short MAIN_CMD = 0xA0;//视频播放控制主指令
        public static final short Video_REQUEST_PLAY = 0x01;    //请求播放视频
        public static final short Video_Response_Started = 0x23;//视频开始后会接受到这个命令
        public static final short Video_Request_GetPageData = 0x21;//用于获取视频基本信息
        public static final short Video_Response_PageChanged = 0x22;//接受视频进度
        public static final short Video_REQUEST_STOP = 0x24;  //停止播放后收到的命令
        public static final short Video_Request_PlayToPage = 0x0B; //设置视频进度
        public static final short Video_Request_NextStep = 0x05; //快进5秒
        public static final short Video_Request_PreviousStep = 0x07;//快退5秒
        public static final short Video_Request_Rotate = 0x83;//旋转
        public static final short Video_Response_Paused = 0x25;//pc push 暂停
        public static final short Video_Response_Resumed = 0x26;// pc push 暂停后重新开始
        public static final short Video_Response_GetScreenShot = 0x49;//返回截屏的图片文件名称
        public static final short Video_Request_GetScreenShot = 0x42;//请求截屏
        public static final short REQUEST_PLAY = 0x01;    //请求播放图片
        public static final short REQUEST_PAUSE = 0x02;   //请求暂停播放
        public static final short REQUEST_RESUME = 0x03;  //请求恢复播放
        public static final short REQUEST_STOP = 0x04;  //停止播放
        //声音控制及数据交互
        public static final short Request_GetVolume = 0x40;      //{volume} 范围：[0,100]
        public static final short Request_SetVolume = 0x41;      //{volume}范围：[0,100]
        public static final short Response_GetVolume = 0x48;      //{volume}范围：[0,100]
    }

    //--------鼠标控制指令-----------------------------------------------------
    public class MOUSE {
        public static final short MAIN_CMD =  0x91;
        public static final short REQUEST_MOUSE_DOWN_LEFT = 0x01;    //按下鼠标左键
        public static final short REQUEST_MOUSE_DOWN_RIGHT = 0x02;   //按下鼠标右键
        public static final short REQUEST_MOUSE_UP_LEFT = 0x03;      //抬起鼠标左键
        public static final short REQUEST_MOUSE_UP_RIGHT = 0x04;     //抬起鼠标右键
        public static final short REQUEST_MOUSE_CLICK_LEFT = 0x05;   //单击鼠标左键
        public static final short REQUEST_MOUSE_CLICK_RIGHT = 0x06;  //单击鼠标右键
        public static final short REQUEST_MOUSE_DOUBLE_CLICK = 0x07; //双击鼠标
        public static final short REQUEST_MOUSE_MOVE = 0x08;          //鼠标移动
        public static final short REQUEST_MOUSE_WHEEL = 0x0B;          //鼠标滚轮
        public static final short Request_Show_Cursor = 0x0C;          //显示鼠标
        public static final short Request_Hidden_Cursor = 0x0D;          //隐藏鼠标
    }

}

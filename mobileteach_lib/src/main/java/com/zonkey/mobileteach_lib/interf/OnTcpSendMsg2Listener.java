package com.zonkey.mobileteach_lib.interf;

import com.zonkey.mobileteach_lib.net.bean.TcpResponse;

/**
 * Created by xu.wang
 * Date on  2017/11/2 19:40:44.
 *
 * @Desc
 */

public interface OnTcpSendMsg2Listener {
    void success(TcpResponse tcp);
    void error(Exception e);
}

package com.zonkey.mobileteach_lib.net.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xu.wang
 * Date on 2017/4/11 10:22
 */
public class ReceiverAddress implements Parcelable {
    private String ip;
    private int port;
    private int type ;          //类型,那个端口接受的指令,1,UDP,2,TCP 3,FILE 5,在智课助手中暂用于关闭录屏界面

    public ReceiverAddress(String ip, int port, int type) {
        this.ip = ip;
        this.port = port;
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    protected ReceiverAddress(Parcel in) {
        ip = in.readString();
        port = in.readInt();
        type = in.readInt();
    }

    public static final Creator<ReceiverAddress> CREATOR = new Creator<ReceiverAddress>() {
        @Override
        public ReceiverAddress createFromParcel(Parcel in) {
            return new ReceiverAddress(in);
        }

        @Override
        public ReceiverAddress[] newArray(int size) {
            return new ReceiverAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ip);
        parcel.writeInt(port);
        parcel.writeInt(type);
    }
}

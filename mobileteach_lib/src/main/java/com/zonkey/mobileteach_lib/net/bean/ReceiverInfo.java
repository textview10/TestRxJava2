package com.zonkey.mobileteach_lib.net.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xu.wang
 * Date on 2016/11/25 09:11
 * 接受地址相关的信息
 */
public class ReceiverInfo implements Parcelable {

    private short mainCmd;   //主命令
    private short subCmd;    //子命令
    private byte appCode;   //应用类型...
    private String data;        //指令
    private ReceiverAddress receiverAddress; //接受地址相关的信息

    public ReceiverInfo(short mainCmd, short subCmd, byte appCode, String data, ReceiverAddress receiverAddress) {
        this.mainCmd = mainCmd;
        this.subCmd = subCmd;
        this.appCode = appCode;
        this.data = data;
        this.receiverAddress = receiverAddress;
    }

    protected ReceiverInfo(Parcel in) {
        mainCmd = in.readByte();
        subCmd = in.readByte();
        appCode = in.readByte();
        data = in.readString();
        receiverAddress = in.readParcelable(ReceiverAddress.class.getClassLoader());
    }

    public static final Creator<ReceiverInfo> CREATOR = new Creator<ReceiverInfo>() {
        @Override
        public ReceiverInfo createFromParcel(Parcel in) {
            return new ReceiverInfo(in);
        }

        @Override
        public ReceiverInfo[] newArray(int size) {
            return new ReceiverInfo[size];
        }
    };

    public short getMainCmd() {
        return mainCmd;
    }

    public void setMainCmd(short mainCmd) {
        this.mainCmd = mainCmd;
    }

    public short getSubCmd() {
        return subCmd;
    }

    public void setSubCmd(short subCmd) {
        this.subCmd = subCmd;
    }

    public byte getAppCode() {
        return appCode;
    }

    public void setAppCode(byte appCode) {
        this.appCode = appCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ReceiverAddress getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(ReceiverAddress receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mainCmd);
        parcel.writeInt(subCmd);
        parcel.writeByte(appCode);
        parcel.writeString(data);
        parcel.writeParcelable(receiverAddress, i);
    }
}

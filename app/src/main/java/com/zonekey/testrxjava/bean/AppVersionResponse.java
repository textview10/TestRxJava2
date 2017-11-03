package com.zonekey.testrxjava.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xu.wang
 * Date on 2017/6/29 17:01
 */

public class AppVersionResponse implements Parcelable{
    private String id;
    private String appid;
    private String num;
    private int orders = 0;
    private String creator;
    private String createdate;
    private String fileurl;
    private int filesize = 0;

    protected AppVersionResponse(Parcel in) {
        id = in.readString();
        appid = in.readString();
        num = in.readString();
        orders = in.readInt();
        creator = in.readString();
        createdate = in.readString();
        fileurl = in.readString();
        filesize = in.readInt();
    }

    public static final Creator<AppVersionResponse> CREATOR = new Creator<AppVersionResponse>() {
        @Override
        public AppVersionResponse createFromParcel(Parcel in) {
            return new AppVersionResponse(in);
        }

        @Override
        public AppVersionResponse[] newArray(int size) {
            return new AppVersionResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(appid);
        dest.writeString(num);
        dest.writeInt(orders);
        dest.writeString(creator);
        dest.writeString(createdate);
        dest.writeString(fileurl);
        dest.writeInt(filesize);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatedate() {
        return createdate;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }

    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }
}

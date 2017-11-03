package com.zonkey.mobileteach_lib.view.drawpaint.bean;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xu.wang
 * Date on 2017/2/15 16:50
 */
public class PointInfo implements Parcelable {
    private PointF pointF;      //当前控制点的坐标
    private int index;          //当前是第几个点

    public PointInfo(PointF pointF, int index) {
        this.pointF = pointF;
        this.index = index;
    }

    protected PointInfo(Parcel in) {
        pointF = in.readParcelable(PointF.class.getClassLoader());
        index = in.readInt();
    }

    public static final Creator<PointInfo> CREATOR = new Creator<PointInfo>() {
        @Override
        public PointInfo createFromParcel(Parcel in) {
            return new PointInfo(in);
        }

        @Override
        public PointInfo[] newArray(int size) {
            return new PointInfo[size];
        }
    };

    public PointF getPointF() {
        return pointF;
    }

    public void setPointF(PointF pointF) {
        this.pointF = pointF;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(pointF, i);
        parcel.writeInt(index);
    }
}

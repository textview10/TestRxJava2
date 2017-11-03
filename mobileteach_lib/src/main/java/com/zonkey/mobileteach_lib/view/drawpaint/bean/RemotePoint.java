package com.zonkey.mobileteach_lib.view.drawpaint.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by xu.wang
 * Date on 2017/5/18 10:27
 */
public class RemotePoint {
    private String ShapeId;
    private int Id;
    private String Point;

    @JSONField(name = "ShapeId")
    public String getShapeId() {
        return ShapeId;
    }

    @JSONField(name = "ShapeId")
    public void setShapeId(String shapeId) {
        ShapeId = shapeId;
    }

    @JSONField(name = "Id")
    public int getId() {
        return Id;
    }

    @JSONField(name = "Id")
    public void setId(int id) {
        Id = id;
    }

    @JSONField(name = "Point")
    public String getPoint() {
        return Point;
    }

    @JSONField(name = "Point")
    public void setPoint(String point) {
        Point = point;
    }

}

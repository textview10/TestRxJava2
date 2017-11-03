package com.zonkey.mobileteach_lib.view.drawpaint.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/5/18 10:19
 */
public class RemoteShape {
    private int LogicalType;    //逻辑上的
    private int DrawingType;    //绘制类型
    private String ImageName;   //图片名字
    private String ParentId;    //父id
    private String CreatorId;   //创建id
    private String Id;          //图形id
    private String StartPoint;  //起始点
    private String EndPoint;    //结束点
    private String ForeColor;   //画笔颜色
    private float BorderWidth;  //线条宽度
    private String BorderColor; //线条颜色
    private String FileName;    //文件名
    private boolean IsDelete;  //是否删除
    private String OriginalStartPoint;
    private String OriginalEndPoint;
    private int MiniBoardState;
    private ArrayList<RemotePoint> Points;

    @JSONField(name = "LogicalType")
    public int getLogicalType() {
        return LogicalType;
    }

    @JSONField(name = "LogicalType")
    public void setLogicalType(int logicalType) {
        LogicalType = logicalType;
    }

    @JSONField(name = "DrawingType")
    public int getDrawingType() {
        return DrawingType;
    }

    @JSONField(name = "DrawingType")
    public void setDrawingType(int drawingType) {
        DrawingType = drawingType;
    }

    @JSONField(name = "ImageName")
    public String getImageName() {
        return ImageName;
    }

    @JSONField(name = "ImageName")
    public void setImageName(String imageName) {
        ImageName = imageName;
    }

    @JSONField(name = "ParentId")
    public String getParentId() {
        return ParentId;
    }

    @JSONField(name = "ParentId")
    public void setParentId(String parentId) {
        ParentId = parentId;
    }

    @JSONField(name = "CreatorId")
    public String getCreatorId() {
        return CreatorId;
    }

    @JSONField(name = "CreatorId")
    public void setCreatorId(String creatorId) {
        CreatorId = creatorId;
    }

    @JSONField(name = "Id")
    public String getId() {
        return Id;
    }

    @JSONField(name = "Id")
    public void setId(String id) {
        Id = id;
    }

    @JSONField(name = "StartPoint")
    public String getStartPoint() {
        return StartPoint;
    }

    @JSONField(name = "StartPoint")
    public void setStartPoint(String startPoint) {
        StartPoint = startPoint;
    }

    @JSONField(name = "EndPoint")
    public String getEndPoint() {
        return EndPoint;
    }

    @JSONField(name = "EndPoint")
    public void setEndPoint(String endPoint) {
        EndPoint = endPoint;
    }

    @JSONField(name = "ForeColor")
    public String getForeColor() {
        return ForeColor;
    }

    @JSONField(name = "ForeColor")
    public void setForeColor(String foreColor) {
        ForeColor = foreColor;
    }

    @JSONField(name = "BorderWidth")
    public float getBorderWidth() {
        return BorderWidth;
    }

    @JSONField(name = "BorderWidth")
    public void setBorderWidth(float borderWidth) {
        BorderWidth = borderWidth;
    }

    @JSONField(name = "BorderColor")
    public String getBorderColor() {
        return BorderColor;
    }

    @JSONField(name = "BorderColor")
    public void setBorderColor(String borderColor) {
        BorderColor = borderColor;
    }

    @JSONField(name = "FileName")
    public String getFileName() {
        return FileName;
    }

    @JSONField(name = "FileName")
    public void setFileName(String fileName) {
        FileName = fileName;
    }

    @JSONField(name = "OriginalStartPoint")
    public String getOriginalStartPoint() {
        return OriginalStartPoint;
    }

    @JSONField(name = "OriginalStartPoint")
    public void setOriginalStartPoint(String originalStartPoint) {
        OriginalStartPoint = originalStartPoint;
    }

    @JSONField(name = "OriginalEndPoint")
    public String getOriginalEndPoint() {
        return OriginalEndPoint;
    }

    @JSONField(name = "OriginalEndPoint")
    public void setOriginalEndPoint(String originalEndPoint) {
        OriginalEndPoint = originalEndPoint;
    }

    @JSONField(name = "MiniBoardState")
    public int getMiniBoardState() {
        return MiniBoardState;
    }

    @JSONField(name = "MiniBoardState")
    public void setMiniBoardState(int miniBoardState) {
        MiniBoardState = miniBoardState;
    }

    @JSONField(name = "Points")
    public ArrayList<RemotePoint> getPoints() {
        return Points;
    }

    @JSONField(name = "Points")
    public void setPoints(ArrayList<RemotePoint> points) {
        Points = points;
    }

    @JSONField(name = "IsDelete")
    public boolean isIsDelete() {
        return IsDelete;
    }

    @JSONField(name = "IsDelete")
    public void setIsDelete(boolean delete) {
        IsDelete = delete;
    }
}

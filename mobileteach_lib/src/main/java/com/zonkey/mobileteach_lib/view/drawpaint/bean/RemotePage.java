package com.zonkey.mobileteach_lib.view.drawpaint.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/5/18 10:16
 * 从远程收到每页黑板的信息
 */
public class RemotePage {
    private String Id;      //黑板的id
    private String BackgroundImageFileName; //背景图片的名字
    private ArrayList<RemoteShape> Shapes;  //所有的线条

    @JSONField(name = "Id")
    public String getId() {
        return Id;
    }
    @JSONField(name = "Id")
    public void setId(String id) {
        Id = id;
    }

    @JSONField(name = "BackgroundImageFileName")
    public String getBackgroundImageFileName() {
        return BackgroundImageFileName;
    }

    @JSONField(name = "BackgroundImageFileName")
    public void setBackgroundImageFileName(String backgroundImageFileName) {
        BackgroundImageFileName = backgroundImageFileName;
    }

    @JSONField(name = "Shapes")
    public ArrayList<RemoteShape> getShapes() {
        return Shapes;
    }
    @JSONField(name = "Shapes")
    public void setShapes(ArrayList<RemoteShape> shapes) {
        Shapes = shapes;
    }

}

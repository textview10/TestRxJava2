package com.zonkey.mobileteach_lib.view.drawpaint.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/6/8 15:57
 * 迷你黑板的docment对象
 */

public class RemoteDocment {
    private int Count;
    private ArrayList<RemotePage> Pages;  //所有的黑板页
    private String FileName;

    @JSONField(name = "Count")
    public int getCount() {
        return Count;
    }
    @JSONField(name = "Count")
    public void setCount(int count) {
        Count = count;
    }

    @JSONField(name = "Pages")
    public ArrayList<RemotePage> getPages() {
        return Pages;
    }

    @JSONField(name = "Pages")
    public void setPages(ArrayList<RemotePage> pages) {
        Pages = pages;
    }

    @JSONField(name = "FileName")
    public String getFileName() {
        return FileName;
    }

    @JSONField(name = "FileName")
    public void setFileName(String fileName) {
        FileName = fileName;
    }
}

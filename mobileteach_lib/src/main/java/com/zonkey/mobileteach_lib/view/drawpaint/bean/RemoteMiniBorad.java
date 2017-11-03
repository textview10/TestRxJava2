package com.zonkey.mobileteach_lib.view.drawpaint.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by xu.wang
 * Date on 2017/6/8 14:49
 */

public class RemoteMiniBorad {
    private String Id;
    private RemoteDocment ContentDocument;
    private String FileName;

    public RemoteMiniBorad() {
    }

    @JSONField(name = "FileName")
    public String getFileName() {
        return FileName;
    }

    @JSONField(name = "FileName")
    public void setFileName(String fileName) {
        FileName = fileName;
    }

    @JSONField(name = "Id")
    public String getId() {
        return Id;
    }

    @JSONField(name = "Id")
    public void setId(String id) {
        Id = id;
    }

    @JSONField(name = "ContentDocument")
    public RemoteDocment getContentDocument() {
        return ContentDocument;
    }

    @JSONField(name = "ContentDocument")
    public void setContentDocument(RemoteDocment contentDocument) {
        ContentDocument = contentDocument;
    }
}

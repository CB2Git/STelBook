package com.jay.javabean;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobPointer;

/**
 * 联系人列表对应类
 * Created by Jay on 2016/7/10.
 */
public class LinkMenDB extends BmobObject {

    private BmobPointer versionId;

    private String linkMenName;

    private List<String> telList;

    public LinkMenDB(BmobPointer versionId, String linkMenName, List<String> telList) {
        this.versionId = versionId;
        this.linkMenName = linkMenName;
        this.telList = telList;
    }

    public List<String> getTelList() {
        return telList;
    }

    public void setTelList(List<String> telList) {
        this.telList = telList;
    }

    public String getLinkMenName() {
        return linkMenName;
    }

    public void setLinkMenName(String linkMenName) {
        this.linkMenName = linkMenName;
    }

    public BmobPointer getVersionId() {
        return versionId;
    }

    public void setVersionId(BmobPointer versionId) {
        this.versionId = versionId;
    }
}

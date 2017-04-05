package com.jay.javabean;

/**
 * 备份版本映射类
 * Created by Jay on 2016/7/10.
 */
public class VersionBean {

    /**
     * 备份总数
     */
    private int backupCount;

    /**
     * 备份的时间
     */
    private String createTime;

    /**
     * 版本号id
     */
    private String versionid;

    public VersionBean(int backupCount, String createTime, String versionid) {
        this.backupCount = backupCount;
        this.createTime = createTime;
        this.versionid = versionid;
    }

    public int getBackupCount() {
        return backupCount;
    }

    public void setBackupCount(int backupCount) {
        this.backupCount = backupCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getVersionid() {
        return versionid;
    }

    public void setVersionid(String versionid) {
        this.versionid = versionid;
    }
}

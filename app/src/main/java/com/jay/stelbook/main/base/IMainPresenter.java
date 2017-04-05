package com.jay.stelbook.main.base;

import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * 主界面需要执行的操作
 * Created by ${jay} on ${2016/8/17
 */
public interface IMainPresenter {

    /**
     * 执行备份操作
     */
    void doBackup(List<BmobObject> contactList);

}

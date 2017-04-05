package com.jay.stelbook.restore.base;

import com.jay.javabean.UserBean;

import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * Created by ${jay} on ${2016/8/17
 */

public interface IRestorePresenter {

    /**
     * 查询所有备份版本
     */
    void queryAllContactVersion(UserBean user);

    /**
     * 删除一个备份
     */
    void deleteVersion(List<BmobObject> contacts);

}

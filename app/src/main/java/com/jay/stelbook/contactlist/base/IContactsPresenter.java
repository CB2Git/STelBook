package com.jay.stelbook.contactlist.base;

import cn.bmob.v3.BmobUser;

/**
 * Created by ${jay} on ${2016/8/17
 */

public interface IContactsPresenter {

    /**
     * 查询指定版本的联系人数据
     */
    void queryAllContacts(BmobUser user, String versionId);
}

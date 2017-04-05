package com.jay.stelbook.contactlist.base;

import com.jay.javabean.ContactBean;

import java.util.List;

/**
 * Created by ${jay} on ${2016/8/17
 */

public interface IContactsView {

    /**
     * 展示所有联系人
     */
    void displayAllContacts(List<ContactBean> contacts);

    /**
     * 显示提示
     */
    void showTip(String tip, int tipType);
}

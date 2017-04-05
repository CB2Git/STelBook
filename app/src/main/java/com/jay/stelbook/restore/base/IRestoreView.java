package com.jay.stelbook.restore.base;

import com.jay.javabean.ContactBean;

import java.util.List;

/**
 * Created by ${jay} on ${2016/8/17
 */

public interface IRestoreView {

    /**
     * 展示联系人列表
     */
    void displayContactVersion(List<ContactBean> list);

    /**
     * 刷新界面
     */
    void updateDisplay();

    /**
     * 显示提示
     */
    void showTip(String tip, int tipType);
}

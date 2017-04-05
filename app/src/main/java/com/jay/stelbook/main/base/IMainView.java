package com.jay.stelbook.main.base;

/**
 * 主界面需要执行的操作
 * Created by ${jay} on ${2016/8/17
 */
public interface IMainView {

    /**
     * 显示等待对话框
     */
    void showWaitDlg();

    /**
     * 隐藏等待对话框
     */
    void hideWaitDlg();

    /**
     * 显示提示
     */
    void showTip(String tip,int tipType);
}

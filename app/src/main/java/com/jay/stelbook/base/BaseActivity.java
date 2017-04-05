package com.jay.stelbook.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import cc.cloudist.acplibrary.ACProgressBaseDialog;
import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

/**
 * 所有Activity的基类，包含一个等待进度框
 * <p>
 * Created by ${jay} on ${2016/8/17
 */
public class BaseActivity extends Activity {

    /**
     * 等待对话框
     */
    private ACProgressBaseDialog mWaitDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    /**
     * 做一些初始化操作
     */
    private void init() {
        //初始化等待对话框
        mWaitDlg = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY).build();
        mWaitDlg.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onDestroy() {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            mWaitDlg.dismiss();
        }
        super.onDestroy();
    }

    public void showWaitDlg() {
        if (mWaitDlg == null) {
            init();
        }
        mWaitDlg.show();
    }

    public void hideWaitDlg() {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            mWaitDlg.cancel();
            mWaitDlg = null;
        }
    }
}

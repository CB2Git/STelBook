package com.jay.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Toast;

import com.jay.stelbook.R;

import java.lang.ref.WeakReference;

/**
 * 双击返回键退出Activity帮助类
 *
 * @author Jay
 */
public class DoubleClickExitHelper {
    private WeakReference<Activity> mActivityWeakRef;
    private boolean isOnKeyBacking;
    private Handler mHandler;
    private Toast mBackToast;
    private int mDelay = 2000;

    public DoubleClickExitHelper(Activity activity) {
        this(activity, 2000);
    }

    public DoubleClickExitHelper(Activity activity, int delay) {
        mActivityWeakRef = new WeakReference<Activity>(activity);
        mHandler = new Handler(Looper.getMainLooper());
        mDelay = delay;
    }

    /**
     * Activity onKeyDown事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return false;
        }
        if (isOnKeyBacking) {
            mHandler.removeCallbacks(onBackTimeRunnable);
            if (mBackToast != null) {
                mBackToast.cancel();
            }
            getActivity().finish();
            return true;
        } else {
            isOnKeyBacking = true;
            if (mBackToast == null) {
                mBackToast = Toast.makeText(getActivity(), R.string.double_click_back_exit, Toast.LENGTH_SHORT);
            }
            mBackToast.show();
            // 延迟mDelay的时间，把Runable发出去
            mHandler.postDelayed(onBackTimeRunnable, mDelay);
            return true;
        }
    }


    private Activity getActivity() {
        return mActivityWeakRef.get();
    }

    private Runnable onBackTimeRunnable = new Runnable() {

        @Override
        public void run() {
            isOnKeyBacking = false;
            if (mBackToast != null) {
                mBackToast.cancel();
            }
        }
    };
}
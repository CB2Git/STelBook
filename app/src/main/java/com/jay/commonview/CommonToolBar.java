package com.jay.commonview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jay.stelbook.R;

import java.lang.ref.WeakReference;

/**
 * 通用顶部标题栏
 * TODO:目前只针对当前应该的需要简单实现了下，如果后期需要更加通用，需要抽取出布局属性方便使用
 * Created by ${jay} on ${2016/8/17
 */
public class CommonToolBar extends FrameLayout implements View.OnClickListener {

    /**
     * 顶部返回按钮
     */
    private ImageView mBackIcon;

    /**
     * 顶部标题
     */
    private TextView mTitle;

    /**
     * 点击返回按钮需要自动关闭的Activity
     */
    private WeakReference<Activity> mActivityWeakReference;

    public CommonToolBar(Context context) {
        this(context, null);
    }

    public CommonToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View rootView = View.inflate(getContext(), R.layout.common_toolbar_layout, this);
        mBackIcon = (ImageView) rootView.findViewById(R.id.common_toolbar_back_icon);
        mTitle = (TextView) rootView.findViewById(R.id.common_toolbar_title);
        mBackIcon.setOnClickListener(this);
    }

    /**
     * 设置顶部标题栏文字
     */
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    /**
     * 设置顶部标题栏文字
     */
    public void setTitle(int titleId) {
        mTitle.setText(getResources().getString(titleId));
    }

    /**
     * 设置返回按钮是否可见
     */
    public void setBackIconVisible(int visible) {
        mBackIcon.setVisibility(visible);
    }

    /**
     * 设置点击返回按钮，自动关闭的Activity
     */
    public void setAutoFinish(Activity activity) {
        mActivityWeakReference = new WeakReference<Activity>(activity);
    }

    /**
     * 返回按钮被点击了
     */
    @Override
    public void onClick(View v) {
        if (mActivityWeakReference != null) {
            Activity activity = mActivityWeakReference.get();
            if (activity != null) {
                activity.finish();
            }
        }
    }
}

package com.jay.commonview;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.EditText;

import com.jay.stelbook.R;

/**
 * 自定义EditText
 * <p/>
 * 右边添加搜索图标，左边添加清空按钮
 * <p/>
 * <p>
 * TODO:添加一个搜索回调
 * Created by Jay on 2016/7/17.
 */
public class SuperEditText extends EditText {

    private Drawable mSearch;
    private Drawable mClear;
    //是否显示清空按钮
    private boolean mShowClear = false;
    // 搜索按钮和清空按钮的大小
    private int mBound = 0;
    // 搜索按钮和清空按钮的外边距
    private int mIconMargin = dp2px(10);

    public SuperEditText(Context context) {
        this(context, null);
    }

    public SuperEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadIcon();
    }

    /**
     * 加载图片
     */
    private void loadIcon() {
        mSearch = getResources().getDrawable(R.drawable.searcher);
        mClear = getResources().getDrawable(R.drawable.clear);
        //添加内容改变监听事件
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    showClearIcon(true);
                } else {
                    showClearIcon(false);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int heightSize = getMeasuredHeight();
        int widthSize = getMeasuredWidth();
        mBound = Math.min(heightSize, widthSize);
        mBound = mBound - mIconMargin;
        mSearch.setBounds(0, 0, mBound, mBound);
        mClear.setBounds(0, 0, mBound, mBound);
        showClearIcon(mShowClear);
    }

    /**
     * 显示/隐藏清空按钮
     *
     * @param show 是否显示空·清空按钮
     */
    private void showClearIcon(boolean show) {
        mShowClear = show;
        mSearch.setBounds(0, 0, mBound, mBound);
        mClear.setBounds(0, 0, mBound, mBound);
        if (mShowClear) {
            setCompoundDrawables(mSearch, null, mClear, null);
        } else {
            setCompoundDrawables(mSearch, null, null, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        RectF rect = new RectF(0, 0, getMeasuredWidth() - mBound, mBound);
        //当点击了清空按钮以后
        if (event.getAction() == MotionEvent.ACTION_UP && mShowClear && !rect.contains(x, y)) {
            //隐藏清空按钮
            showClearIcon(false);
            //清空输入文字
            setText("");
        }
        return super.onTouchEvent(event);
    }

    // dp 转 px
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}

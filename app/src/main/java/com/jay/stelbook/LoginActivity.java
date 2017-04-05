package com.jay.stelbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jay.javabean.UserBean;
import com.jay.stelbook.base.BaseActivity;
import com.jay.stelbook.main.MainActivity;
import com.jay.util.CipherUtils;
import com.jay.util.ToastUtil;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * 登录界面，负责用户登录，以及跳转到注册界面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText mUser;
    private EditText mPsw;
    private Button mLogin;
    private TextView mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);
        initView();
    }

    /**
     * 初始化所有控件、等待对话框并添加点击事件
     */
    private void initView() {
        mUser = (EditText) findViewById(R.id.login_user);
        mPsw = (EditText) findViewById(R.id.login_password);
        mLogin = (Button) findViewById(R.id.login_btn);
        mRegister = (TextView) findViewById(R.id.login_register_text);
        mLogin.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        //设置注册文本
        SpannableString spannableString = new SpannableString(getResources().getString(R.string.click_here_register));
        spannableString.setSpan(new ForegroundColorSpan(0xffa2a2a2), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(0xff5856d6), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 9, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mRegister.setText(spannableString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.login_register_text:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    /**
     * 登录验证
     */
    private void login() {
        String userName = String.valueOf(mUser.getText());
        String password = String.valueOf(mPsw.getText());
        if (TextUtils.isEmpty(userName.trim())) {
            mUser.setError(Html.fromHtml("<font color='red'>用户名不能为空</font>"));
            mUser.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password.trim())) {
            mPsw.setError(Html.fromHtml("<font color='red'>密码不能为空</font>"));
            mPsw.requestFocus();
            return;
        }
        UserBean user = new UserBean();
        user.setUsername(userName);
        //密码使用两次MD5加密
        user.setPassword(CipherUtils.md5(CipherUtils.md5(password)));
        showWaitDlg();
        user.login(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtil.show(LoginActivity.this, R.string.login_error);
                }
                hideWaitDlg();
            }
        });
    }
}

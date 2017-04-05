package com.jay.stelbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jay.javabean.UserBean;
import com.jay.stelbook.base.BaseActivity;
import com.jay.stelbook.main.MainActivity;
import com.jay.util.CipherUtils;
import com.jay.util.ToastUtil;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private EditText mUser;
    private EditText mEmail;
    private EditText mPsw;
    private EditText mConfirmPsw;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_layout);
        InitView();
    }

    /**
     * 初始化所有控件并添加点击事件处理
     */
    private void InitView() {
        mUser = (EditText) findViewById(R.id.register_user_name);
        mEmail = (EditText) findViewById(R.id.register_email);
        mPsw = (EditText) findViewById(R.id.register_password);
        mConfirmPsw = (EditText) findViewById(R.id.register_confirm_password);
        mRegister = (Button) findViewById(R.id.register_btn);
        mRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.register_btn) {
            register();
        }
    }

    /**
     * 注册逻辑处理
     */
    private void register() {

        String userName = mUser.getText().toString();
        String mail = mEmail.getText().toString();
        String psw = mPsw.getText().toString();
        String comfirmPsw = mConfirmPsw.getText().toString();
        //输入检测
        if (checkRegisterInfo(userName, mail, psw, comfirmPsw)) {
            return;
        }

        UserBean user = new UserBean();
        user.setUsername(userName);
        user.setEmail(mail);
        user.setPassword(CipherUtils.md5(CipherUtils.md5(psw)));
        showWaitDlg();
        user.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    BmobUser.getCurrentUser().setObjectId(bmobUser.getObjectId());
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    StringBuffer sb = new StringBuffer("注册失败。");
                    //用户名已经存在
                    if (e.getErrorCode() == 202) {
                        mUser.setError(Html.fromHtml("<font color='red'>用户名被占用！</font>"));
                    }
                    //邮箱已经被占用
                    if (e.getErrorCode() == 203) {
                        mEmail.setError(Html.fromHtml("<font color='red'>邮箱已经被占用！</font>"));
                    }
                    //邮箱格式不正确
                    if (e.getErrorCode() == 301) {
                        mEmail.setError(Html.fromHtml("<font color='red'>请输入有效邮箱地址</font>"));
                    }
                    ToastUtil.show(RegisterActivity.this, sb.toString());
                }
                hideWaitDlg();
            }
        });
    }

    /**
     * 检测注册信息是否完整
     *
     * @param userName
     * @param mail
     * @param psw
     * @param comfirmPsw
     * @return
     */
    private boolean checkRegisterInfo(String userName, String mail, String psw, String comfirmPsw) {
        //检测空数据
        if ("".equals(userName.trim())) {
            mUser.setError(Html.fromHtml("<font color='red'>用户名不能为空</font>"));
            mUser.requestFocus();
            return true;
        }
        if ("".equals(mail.trim())) {
            mEmail.setError(Html.fromHtml("<font color='red'>邮箱地址不能为空</font>"));
            mEmail.requestFocus();
            return true;
        }
        if ("".equals(psw.trim())) {
            mPsw.setError(Html.fromHtml("<font color='red'>密码不能为空</font>"));
            mPsw.requestFocus();
            return true;
        }
        if ("".equals(comfirmPsw.trim())) {
            mConfirmPsw.setError(Html.fromHtml("<font color='red'>请确认密码</font>"));
            mConfirmPsw.requestFocus();
            return true;
        }
        //检查数据正确性
        if (!psw.equals(comfirmPsw)) {
            mConfirmPsw.setError(Html.fromHtml("<font color='red'>两次输入密码不一致</font>"));
            mConfirmPsw.requestFocus();
            return true;
        }
        if (!mail.matches("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$")) {
            mEmail.setError(Html.fromHtml("<font color='red'>请输入有效邮箱地址</font>"));
            mEmail.requestFocus();
            return true;
        }
        return false;
    }
}
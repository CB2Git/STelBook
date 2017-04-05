package com.jay.stelbook.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jay.commonview.CircleImageView;
import com.jay.stelbook.LoginActivity;
import com.jay.stelbook.R;
import com.jay.stelbook.base.BaseActivity;
import com.jay.stelbook.main.base.IMainView;
import com.jay.stelbook.restore.RestoreActivity;
import com.jay.util.ContactsUtil;
import com.jay.util.DoubleClickExitHelper;
import com.jay.util.SharedPreferencesUtil;
import com.jay.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.BmobUser;

/**
 * 登录成功以后的主界面，主要功能，备份/恢复
 * TODO:侧滑栏菜单的点击响应
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, IMainView {

    private static final String TAG = "MainActivity";

    //侧滑菜单
    private DrawerLayout mDrawerLayout;

    //侧滑菜单边栏控件
    private NavigationView mNavigationView;

    //侧滑菜单的头部布局
    private View mNavigationViewHeader;

    //用户头像控件
    private CircleImageView mUserIcon;

    //用户名字
    private TextView mUserName;

    private Toolbar mToolbar;

    //自动切换侧滑菜单的帮助类(默认效果才带动画)
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    //主界面控件
    private ImageView mBackUp;
    private ImageView mRestore;
    private TextView mLastBackupTime;

    private MainPresenter mContactOperate = new MainPresenter(this);

    private DoubleClickExitHelper doubleClickExitHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        initView();
        initListener();
    }

    /**
     * 初始化事件监听
     */
    private void initListener() {
        mNavigationView.setNavigationItemSelectedListener(this);
        mUserIcon.setOnClickListener(this);
        mBackUp.setOnClickListener(this);
        mRestore.setOnClickListener(this);
        //初始化双击退出对象
        doubleClickExitHelper = new DoubleClickExitHelper(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //备份
            case R.id.main_backup:
                doBackUp();
                break;
            //还原
            case R.id.main_restore:
                Intent intent = new Intent(this, RestoreActivity.class);
                startActivity(intent);
                break;
            //修改用户头像
            case R.id.main_user_icon:
                break;
        }
    }

    private void doBackUp() {
        //如果拥有读取联系人权限
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)) {
            mContactOperate.doBackup(ContactsUtil.queryAllContacts(getContentResolver()));
        } else {
            //申请读取联系人
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mContactOperate.doBackup(ContactsUtil.queryAllContacts(getContentResolver()));
            } else {
                //权限被用户拒绝了，但是并没有选择不再提示，也就是说还可以继续申请
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    //申请读取联系人
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "需要申请权限", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(Color.WHITE);
                    TextView tipText = (TextView) view.findViewById(R.id.snackbar_text);
                    tipText.setTextColor(Color.BLACK);
                    snackbar.setAction("重试", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 10);
                        }
                    });
                    snackbar.show();
                } else {
                    ToastUtil.show(this, R.string.no_permission_read_contacts);
                }
            }
        }
    }

    /**
     * 侧滑菜单项被点击
     *
     * @param item 被点击的菜单项
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //TODO 侧滑菜单的响应
        CharSequence title = item.getTitle();
        int id = item.getItemId();
        switch (id) {
            case R.id.main_modify_password:
                break;
            case R.id.main_logout:
                logout();
                break;
            case R.id.main_quit_app:
                quitApp();
                break;
            case R.id.main_about_developer:
                showMe();
                break;
            case R.id.main_feedback:
                break;
        }
        item.setChecked(true);
        ToastUtil.show(this, "靠你啦，骚年~~" + title.toString());
        mDrawerLayout.closeDrawers();
        return true;
    }

    /**
     * 初始化视图
     */
    private void initView() {
        //初始化主界面的控件
        mBackUp = (ImageView) findViewById(R.id.main_backup);
        mRestore = (ImageView) findViewById(R.id.main_restore);
        mLastBackupTime = (TextView) findViewById(R.id.main_last_backup_time);
        //初始化侧滑菜单的控件
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.main_navigationview);
        mNavigationViewHeader = mNavigationView.getHeaderView(0);
        mUserIcon = (CircleImageView) mNavigationViewHeader.findViewById(R.id.main_user_icon);
        mUserName = (TextView) mNavigationViewHeader.findViewById(R.id.main_user_name);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        initLastUploadTime();
    }

    /**
     * 初始化上次备份时间
     */
    private void initLastUploadTime() {
        String lastUploadTime = (String) SharedPreferencesUtil.get(this, "lastUploadTime", "");
        if (!"".equals(lastUploadTime)) {
            mLastBackupTime.setText(lastUploadTime);
        }
    }

    /**
     * 存储上次备份时间，当点击上传备份以后，如果备份成功，则调用此函数
     */
    private void saveUploadTime() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String lastUploadTime = simpleDateFormat.format(date);
        mLastBackupTime.setText(lastUploadTime);
        SharedPreferencesUtil.put(this, "lastUploadTime", lastUploadTime);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return true;
        } else {
            return doubleClickExitHelper.onKeyDown(keyCode, event);
        }
    }

    /**
     * 显示关于我的信息,打开网站的留言板
     */
    private void showMe() {
        String url = "http://www.27house.cn/note/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * 退出应用
     */
    private void quitApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("提示");
        builder.setMessage("是否退出应用？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }


    /**
     * 退出登录
     */
    private void logout() {
        //清楚本地缓存对象
        BmobUser.logOut();
        //跳转到登录界面
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 信息反馈
     * 获取开发者信息(手机号)，发送短信的形式反馈
     */
    private void feedBack() {

    }

    @Override
    public void showTip(String tip, int tipType) {
        Snackbar snackbar = Snackbar.make(mDrawerLayout, tip, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.WHITE);
        TextView tipText = (TextView) view.findViewById(R.id.snackbar_text);
        tipText.setTextColor(Color.BLACK);
        snackbar.show();
        saveUploadTime();
        initLastUploadTime();
    }
}

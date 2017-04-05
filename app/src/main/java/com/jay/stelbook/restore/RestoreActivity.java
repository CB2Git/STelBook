package com.jay.stelbook.restore;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jay.commonview.CommonToolBar;
import com.jay.javabean.ContactBean;
import com.jay.javabean.UserBean;
import com.jay.javabean.VersionBean;
import com.jay.stelbook.R;
import com.jay.stelbook.contactlist.ContactListActivity;
import com.jay.stelbook.restore.base.IRestoreView;
import com.jay.util.ContactsUtil;
import com.jay.util.ToastUtil;
import com.jay.util.ViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 通讯录还原界面
 */
public class RestoreActivity extends Activity implements IRestoreView, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "RestoreActivity";

    //备份列表
    private ListView mBackupList;

    //联系人信息
    private List<ContactBean> mContactList;

    //版本库信息
    private List<VersionBean> mVersionList;

    //没有备份的空视图
    private View mEmptyView;

    //适配器
    private MyAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CommonToolBar mCommonToolBar;

    private RestorePresenter mRestorePresenter = new RestorePresenter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * 删除一个版本
     *
     * @param versionId
     */
    private void deleteVersion(String versionId) {
        List<BmobObject> contactBeen = new ArrayList<>();
        Iterator<ContactBean> iterator = mContactList.iterator();
        while (iterator.hasNext()) {
            ContactBean contact = iterator.next();
            if (contact.getVersion().equals(versionId)) {
                contactBeen.add(contact);
                iterator.remove();
            }
        }
        mRestorePresenter.deleteVersion(contactBeen);
    }

    private String currentVersionId;

    /**
     * 还原一个版本
     */
    private void restoreVersion(String versionId) {
        //如果有权限
        if (PermissionChecker.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)) {
            List<ContactBean> contactBean = new ArrayList<>();
            for (ContactBean contact : mContactList) {
                if (contact.getVersion().equals(versionId)) {
                    contactBean.add(contact);
                }
            }
            try {
                ContactsUtil.insertContacts(getContentResolver(), contactBean);
                showTip("还原成功", 0);
            } catch (RemoteException e) {
                showTip("还原失败", 0);
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                showTip("还原失败", 0);
                e.printStackTrace();
            }
        } else {
            currentVersionId = versionId;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                restoreVersion(currentVersionId);
            } else {
                //权限被用户拒绝了，但是并没有选择不再提示，也就是说还可以继续申请
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    //申请读取联系人
                    Snackbar snackbar = Snackbar.make(mCommonToolBar, "需要申请权限", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(Color.WHITE);
                    TextView tipText = (TextView) view.findViewById(R.id.snackbar_text);
                    tipText.setTextColor(Color.BLACK);
                    snackbar.setAction("重试", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(RestoreActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, 10);
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
     * 初始化视图
     */
    private void initView() {
        mBackupList = (ListView) findViewById(R.id.restore_backup_list);
        mEmptyView = findViewById(R.id.restore_empty_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.restore_refresh_view);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //强行通知SwipeRefreshLayout去测量自己，不然在最初调用
        //mSwipeRefreshLayout.setRefreshing(true);方法无效，不过这样onRefresh()回调不会被调用
        mSwipeRefreshLayout.measure(0, 0);
        //这个使用颜色值
        //mSwipeRefreshLayout.setColorSchemeColors();
        //这个使用资源里面的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);
        mBackupList.setEmptyView(mEmptyView);
        mCommonToolBar = (CommonToolBar) findViewById(R.id.restore_commom_toolbar);
        mCommonToolBar.setTitle(R.string.time_line);
        mCommonToolBar.setAutoFinish(this);
    }

    @Override
    public void displayContactVersion(List<ContactBean> list) {
        mContactList = list;
        parseContactList2VersionList(list);
        mAdapter = new MyAdapter(this, mBackupList, mVersionList, R.layout.activity_restore_timeline_item_layout);
        mBackupList.setAdapter(mAdapter);
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 更新视图
     */
    @Override
    public void updateDisplay() {
        displayContactVersion(mContactList);
    }

    /**
     * 将联系人列表转换为版本列表
     */
    private void parseContactList2VersionList(List<ContactBean> list) {
        String preVersionId = "";
        int preVersionCount = 0;
        mVersionList = new ArrayList<>();
        for (ContactBean contact : list) {
            String version = contact.getVersion();
            //记下第一个id
            if (TextUtils.isEmpty(preVersionId)) {
                preVersionId = version;
            }
            if (!version.equals(preVersionId)) {
                VersionBean versionBean = new VersionBean(preVersionCount, contact.getCreatedAt(), contact.getVersion());
                mVersionList.add(versionBean);
                preVersionId = "";
                preVersionCount = 1;
            } else {
                preVersionCount++;
            }
        }
        if (preVersionCount > 1) {
            ContactBean contact = list.get(list.size() - 1);
            VersionBean versionBean = new VersionBean(preVersionCount, contact.getCreatedAt(), contact.getVersion());
            mVersionList.add(versionBean);
        }
    }

    @Override
    public void showTip(String tip, int tipType) {
        Snackbar snackbar = Snackbar.make(mBackupList, tip, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.WHITE);
        TextView tipText = (TextView) view.findViewById(R.id.snackbar_text);
        tipText.setTextColor(Color.BLACK);
        snackbar.show();
    }

    /**
     * 控件刷新
     */
    @Override
    public void onRefresh() {
        mRestorePresenter.queryAllContactVersion(BmobUser.getCurrentUser(UserBean.class));
    }


    /**
     * ListView的适配器
     */
    private class MyAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener {
        private ListView mListView;
        private List<VersionBean> mVersionList;
        private int mLayoutId;
        private Context mContext;
        //底部弹出框的控件，点击对应联系人弹出
        private PopupWindow mPopupView;
        //底部弹出框 删除
        private TextView mDel;
        //底部弹出框 还原
        private TextView mRestore;
        //当前选中项的序列
        private int mIndex;

        public MyAdapter(Context context, ListView listview, List<VersionBean> versionList, int layoutId) {
            this.mContext = context;
            this.mListView = listview;
            this.mVersionList = versionList;
            this.mLayoutId = layoutId;
            mListView.setOnItemClickListener(this);
            mListView.setOnItemLongClickListener(this);
            initPopupWindow();
        }

        @Override
        public int getCount() {
            return mVersionList == null ? 0 : mVersionList.size();
        }

        @Override
        public Object getItem(int position) {
            return mVersionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //获取对应位置的版本库信息
            VersionBean version = mVersionList.get(position);
            if (convertView == null) {
                convertView = View.inflate(mContext, mLayoutId, null);
            }
            TextView year = ViewHolder.get(convertView, R.id.year);
            TextView mouthAndDay = ViewHolder.get(convertView, R.id.mouthAndDay);
            TextView hour = ViewHolder.get(convertView, R.id.hour);
            TextView amOrPm = ViewHolder.get(convertView, R.id.pmOrAm);
            TextView backupContactsNo = ViewHolder.get(convertView, R.id.backupContactsNo);

            //将版本库的创建时间转为date对象然后转为Calendar
            String createTime = version.getCreateTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(createTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            //设置时间信息
            year.setText(calendar.get(Calendar.YEAR) + "");
            mouthAndDay.setText((calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.DAY_OF_MONTH));
            hour.setText(calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE));
            int amOrpm = calendar.get(Calendar.AM_PM);
            amOrPm.setText(amOrpm == 1 ? "pm" : "am");
            backupContactsNo.setText(String.format("可还原联系人:%d", version.getBackupCount()));
            return convertView;
        }

        //listview的Item被点击相应事件，点击跳转到显示联系人信息界面
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            VersionBean version = mVersionList.get(position);
            Intent intent = new Intent(mContext, ContactListActivity.class);
            intent.putExtra("versiond", version.getVersionid());
            mContext.startActivity(intent);
        }

        //listview长按事件响应
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mIndex = position;
            showBottomMenu(mListView);
            return true;
        }

        /**
         * 初始化PopupWindow
         */
        private void initPopupWindow() {
            // 如果没有加载过，那么加载PopupWindow的布局
            if (mPopupView == null) {
                View view = View.inflate(mContext, R.layout.version_sheet_layout, null);
                mPopupView = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
                mPopupView.setBackgroundDrawable(new ColorDrawable(0));
                //设置动画
                mPopupView.setAnimationStyle(R.style.popwin_anim_style);
                mRestore = (TextView) view.findViewById(R.id.restore);
                mDel = (TextView) view.findViewById(R.id.del);
                mDel.setOnClickListener(this);
                mRestore.setOnClickListener(this);
            }
        }

        /**
         * 显示底部弹出对话框
         *
         * @param v
         */
        private void showBottomMenu(View v) {
            //设置popupwindow的位置
            mPopupView.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            //设置背景半透明
            backgroundAlpha(0.6f);
            //点击空白位置，popupwindow消失的事件监听，这时候让背景恢复正常
            mPopupView.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    backgroundAlpha(1.0f);
                }
            });
        }

        /**
         * 设置屏幕的背景透明度
         *
         * @param bgAlpha
         */
        public void backgroundAlpha(float bgAlpha) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = bgAlpha; // 0.0-1.0
            getWindow().setAttributes(lp);
        }

        /**
         * 底部弹出框的点击相应
         *
         * @param v 被点击的控件
         */
        @Override
        public void onClick(View v) {
            VersionBean version = mVersionList.get(mIndex);
            switch (v.getId()) {
                //删除版本
                case R.id.del:
                    deleteVersion(version);
                    break;
                //还原此版本
                case R.id.restore:
                    restoreVersion(version);
                    break;
            }
            mPopupView.dismiss();
        }

        /**
         * 还原一个版本
         *
         * @param version 待还原的版本
         */
        private void restoreVersion(VersionBean version) {
            if (mContext instanceof RestoreActivity) {
                ((RestoreActivity) mContext).restoreVersion(version.getVersionid());
            }
        }

        /**
         * 删除指定版本
         *
         * @param version 待删除的版本
         */
        private void deleteVersion(VersionBean version) {
            if (mContext instanceof RestoreActivity) {
                ((RestoreActivity) mContext).deleteVersion(version.getVersionid());
            }
        }
    }
}
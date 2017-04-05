package com.jay.stelbook.contactlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.jay.commonview.CommonToolBar;
import com.jay.commonview.SideBar;
import com.jay.commonview.SuperEditText;
import com.jay.javabean.ContactBean;
import com.jay.stelbook.ContactDetailActivity;
import com.jay.stelbook.R;
import com.jay.stelbook.contactlist.base.IContactsPresenter;
import com.jay.stelbook.contactlist.base.IContactsView;
import com.jay.util.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * 显示相应版本联系人的Activity
 */
public class ContactListActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, IContactsView {

    private static final String TAG = "ContactListActivity";
    //对应联系人的版本
    private String mVersionId;
    //搜索框
    private SuperEditText mSuperEdit;
    //联系人列表
    private ListView mListView;
    //索引菜单
    private SideBar mSideBar;
    //适配器
    private MyAdapter mAdapter;
    //悬浮索引标题
    private TextView mFloatSortView;

    private CommonToolBar mCommonToolBar;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private IContactsPresenter mContactsPresenter = new ContactsPresenter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list_layout);
        mVersionId = getIntent().getStringExtra("versionid");
        Log.i(TAG, "onCreate: mVersionId = " + mVersionId);
        initView();
        initListener();
        initContactListAnimation();
    }

    /**
     * 初始化事件监听
     */
    private void initListener() {
        //索引监听
        mSideBar.setOnIndexTouchListener(new SideBar.OnIndexTouchListener() {
            @Override
            public void OnIndexTouch(String sel) {
                if (mAdapter != null) {
                    SectionIndexer sectionIndexer = mAdapter.getSectionIndexer();
                    Object[] objs = sectionIndexer.getSections();
                    for (int i = 0; i < objs.length; i++) {
                        Object obj = objs[i];
                        if (obj.equals(sel)) {
                            int position = sectionIndexer.getPositionForSection(i);
                            mListView.setSelectionFromTop(position, 0);
                            break;
                        }
                    }
                }
            }
        });
        //ListView点击监听
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactBean bean = (ContactBean) mListView.getAdapter().getItem(position);
                Intent intent = new Intent(ContactListActivity.this, ContactDetailActivity.class);
                intent.putExtra("contact", (Parcelable) bean);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化ListView索引挤压特效
     */
    private void initContactListAnimation() {
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            /**
             * 上次第一个可见元素，用于滚动时记录标识。
             */
            private int lastFirstVisibleItem = -1;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mAdapter == null) {
                    return;
                }
                SectionIndexer sectionIndexer = mAdapter.getSectionIndexer();
                Object[] objs = sectionIndexer.getSections();
                int section = sectionIndexer.getSectionForPosition(firstVisibleItem);
                int nextSecPosition = -1;
                if (section + 1 < objs.length) {
                    nextSecPosition = sectionIndexer.getPositionForSection(section + 1);
                }
                //当滑动距离不足一个的时候，直接“盖”在上面即可
                if (firstVisibleItem != lastFirstVisibleItem) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mFloatSortView.getLayoutParams();
                    params.topMargin = 0;
                    mFloatSortView.setLayoutParams(params);
                    mFloatSortView.setText(String.valueOf(objs[section]));
                }
                if (nextSecPosition == firstVisibleItem + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = mFloatSortView.getHeight();
                        int bottom = childView.getBottom();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mFloatSortView.getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            mFloatSortView.setLayoutParams(params);
                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                mFloatSortView.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * 初始化所有视图控件
     */
    private void initView() {
        //初始化视图控件
        mCommonToolBar = (CommonToolBar) findViewById(R.id.contact_list_toolbar);
        mCommonToolBar.setTitle(R.string.contact);
        mCommonToolBar.setAutoFinish(this);
        mSuperEdit = (SuperEditText) findViewById(R.id.super_edit);
        mFloatSortView = (TextView) findViewById(R.id.contact_float_sort_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contact_swiperefreshlayout);
        mSwipeRefreshLayout.measure(0, 0);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);
        mListView = (ListView) findViewById(R.id.contacts_list);
        mSideBar = (SideBar) findViewById(R.id.contacts_index);
    }

    @Override
    public void onRefresh() {
        mContactsPresenter.queryAllContacts(BmobUser.getCurrentUser(), mVersionId);
    }

    /**
     * 展示联系人
     *
     * @param contacts
     */
    @Override
    public void displayAllContacts(List<ContactBean> contacts) {
        if (contacts == null) {
            return;
        }
        Log.i(TAG, "displayAllContacts: contacts.size() = " + contacts.size());
        mAdapter = new MyAdapter(this, contacts);
        mListView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showTip(String tip, int tipType) {
        Snackbar snackbar = Snackbar.make(mSwipeRefreshLayout, tip, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.WHITE);
        snackbar.show();
    }


    /**
     * 联系人列表适配器
     */
    private static class MyAdapter extends BaseAdapter {

        private Context mContext;

        private List<ContactBean> mContactList;

        private SectionIndexer mSectionIndexer;

        private static final int mItemLayout = R.layout.activity_contact_list_item_layout;

        public MyAdapter(Context context, List<ContactBean> contactList) {
            this.mContext = context;
            this.mContactList = contactList;
            mSectionIndexer = new SortSectionIndexer(contactList);
        }

        public SectionIndexer getSectionIndexer() {
            return mSectionIndexer;
        }

        @Override
        public int getCount() {
            return mContactList == null ? 0 : mContactList.size();
        }

        @Override
        public Object getItem(int position) {
            return mContactList == null ? null : mContactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, mItemLayout, null);
            }
            TextView sortKey = ViewHolder.get(convertView, R.id.contact_list_item_sort_key);
            TextView contactName = ViewHolder.get(convertView, R.id.contact_list_name);
            ContactBean bean = (ContactBean) getItem(position);
            sortKey.setText(bean.getSortkey());
            contactName.setText(bean.getName());
            //获取对应的索引
            int section = mSectionIndexer.getSectionForPosition(position);
            //获取这个索引的起始位置
            int beginPos = mSectionIndexer.getPositionForSection(section);
            if (beginPos == position) {
                sortKey.setVisibility(View.VISIBLE);
            } else {
                sortKey.setVisibility(View.GONE);
            }
            return convertView;
        }
    }


    /**
     * 联系人快速滑动索引
     */
    private static class SortSectionIndexer implements SectionIndexer {

        private List<Object> mSections = new ArrayList<>();

        private List<Integer> mPositionForSection;

        private int[] mSectionForPosition;

        public SortSectionIndexer(List<ContactBean> contactList) {
            init(contactList);
        }

        /**
         * 根据传入的数据初始化分组信息
         *
         * @param contactList
         */
        private void init(List<ContactBean> contactList) {
            mSections.clear();
            mPositionForSection = new ArrayList<>();
            mSectionForPosition = new int[contactList.size()];
            Object proSection = null;
            int proSectionIndex = -1;
            for (int i = 0; i < contactList.size(); i++) {
                ContactBean bean = contactList.get(i);
                String sortKey = bean.getSortkey();
                mSectionForPosition[i] = proSectionIndex;
                if (proSection == null || !sortKey.equals(proSection)) {
                    proSection = sortKey;
                    mSections.add(sortKey);
                    mPositionForSection.add(i);
                    proSectionIndex++;
                    mSectionForPosition[i] = proSectionIndex;
                }
            }
        }

        /**
         * 返回所有索引
         */
        @Override
        public Object[] getSections() {
            return mSections.toArray();
        }

        /**
         * 返回索引对应的起始位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {
            return mPositionForSection.get(sectionIndex);
        }

        /**
         * 返回指定位置的索引
         */
        @Override
        public int getSectionForPosition(int position) {
            return mSectionForPosition[position];
        }
    }

    /**
     * 自定义ListView的适配器
     */
//    private class MyAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnClickListener {
//        private Context mContext;
//        private ListView mListView;
//        private List<ContactBean> mContactsList = new ArrayList<>();
//        private PopupWindow mPopupView;
//        private int mLayoutId;
//        //底部弹出框的控件，点击对应联系人弹出
//        private TextView mName;
//        private TextView mCall;
//        private TextView mSendMsg;
//        private TextView mDel;
//        private TextView mRestore;
//
//        public MyAdapter(Context mContext, ListView mListView, List<ContactBean> mContactsList, int mLayoutId) {
//            this.mContext = mContext;
//            this.mListView = mListView;
//            this.mContactsList = new ArrayList<>();
//            this.mLayoutId = mLayoutId;
//            //为ListView添加点击事件响应
//            mListView.setOnItemClickListener(this);
//            //初始化弹出框
//            initPopupWindow();
//        }
//
//        @Override
//        public int getCount() {
//            return mContactsList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mContactsList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ContactBean contacts = mContactsList.get(position);
//            if (convertView == null) {
//                convertView = View.inflate(mContext, mLayoutId, null);
//            }
//            TextView contactName = ViewHolder.get(convertView, R.id.contactName);
//            TextView contactTel = ViewHolder.get(convertView, R.id.contactTel);
//            contactName.setText(contacts.getName());
//            //contactTel.setText(convertList2String(contacts.getTel()));
//            return convertView;
//        }
//
//        /**
//         * 一个联系人的电话号码可能不止一个，使用此函数将一个电话号码的
//         * list集合转化为String对象显示，使用 , 隔开每个号码
//         *
//         * @param list 待转换的联系人集合
//         * @return 转化后的联系人字符串
//         */
//        private String convertList2String(ArrayList<String> list) {
//            StringBuffer result = new StringBuffer();
//            for (int i = 0; i < list.size(); i++) {
//                result.append(list.get(i));
//                if (i != list.size() - 1) {
//                    result.append(",");
//                }
//            }
//            return result.toString();
//        }
//
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            ContactBean contacts = mContactsList.get(position);
//            showBottomMenu(mListView, contacts);
//        }
//
//        /**
//         * 显示底部弹出对话框
//         *
//         * @param v
//         * @param contacts
//         */
//        private void showBottomMenu(View v, ContactBean contacts) {
//            //设置popupwindow的位置
//            mPopupView.showAtLocation(v, Gravity.BOTTOM, 0, 0);
//            //设置背景半透明
//            backgroundAlpha(0.6f);
//            //点击空白位置，popupwindow消失的事件监听，这时候让背景恢复正常
//            mPopupView.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                @Override
//                public void onDismiss() {
//                    backgroundAlpha(1.0f);
//                }
//            });
//            mName.setText(contacts.getName());
//            mName.setTag(contacts);
//        }
//
//        /**
//         * 初始化PopupWindow
//         */
//        private void initPopupWindow() {
//            // 如果没有加载过，那么加载PopupWindow的布局
//            if (mPopupView == null) {
//                View view = View.inflate(mContext, R.layout.action_sheet_layout, null);
//                mPopupView = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
//                mPopupView.setBackgroundDrawable(new ColorDrawable(0));
//                //设置动画
//                mPopupView.setAnimationStyle(R.style.popwin_anim_style);
//                mName = (TextView) view.findViewById(R.id.name);
//                mCall = (TextView) view.findViewById(R.id.call);
//                mSendMsg = (TextView) view.findViewById(R.id.sendMsg);
//                mRestore = (TextView) view.findViewById(R.id.restore);
//                mDel = (TextView) view.findViewById(R.id.del);
//                mCall.setOnClickListener(this);
//                mSendMsg.setOnClickListener(this);
//                mDel.setOnClickListener(this);
//                mRestore.setOnClickListener(this);
//            }
//        }
//
//        /**
//         * 设置屏幕的背景透明度
//         *
//         * @param bgAlpha
//         */
//        public void backgroundAlpha(float bgAlpha) {
//            WindowManager.LayoutParams lp = getWindow().getAttributes();
//            lp.alpha = bgAlpha; // 0.0-1.0
//            getWindow().setAttributes(lp);
//        }
//
//        @Override
//        public void onClick(View v) {
//            ContactBean contacts = (ContactBean) mName.getTag();
//            switch (v.getId()) {
//                //拨打电话
//                case R.id.call:
//                    PhoneUtil.callPhones(mContext, contacts.getTel().get(0));
//                    break;
//                //发送短信
//                case R.id.sendMsg:
//                    PhoneUtil.sendMessage(mContext, contacts.getTel().get(0), "");
//                    break;
//                //还原联系人
//                case R.id.restore:
//                    try {
//                        PhoneUtil.addContact(mContext, contacts);
//                        Toast.makeText(mContext, "还原一个联系人", Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        Toast.makeText(mContext, "还原失败", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//                //删除联系人
//                case R.id.del:
//                    delContacts(contacts);
//                    break;
//            }
//            mPopupView.dismiss();
//        }
//
//        /**
//         * 删除联系人
//         *
//         * @param contacts
//         */
//        private void delContacts(ContactBean contacts) {
//            mContactsList.remove(contacts);
//            this.notifyDataSetChanged();
////            Toast.makeText(mContext, "删除了一个联系人", Toast.LENGTH_SHORT).show();
////            //删除成功，对应版本里面的备份数量-1
////            contacts.delete(new UpdateListener() {
////                @Override
////                public void done(BmobException e) {
////                    if (e == null) {
////                        mVersionId.increment("count", -1);
////                        //不能使用无参数的update，不然无效
////                        mVersionId.update(new UpdateListener() {
////                            @Override
////                            public void done(BmobException e) {
////
////                            }
////                        });
////                    }
////                }
////            });
//        }
//    }
}

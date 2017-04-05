package com.jay.stelbook;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jay.commonview.CommonToolBar;
import com.jay.javabean.ContactBean;
import com.jay.util.PhoneUtil;
import com.jay.util.ViewHolder;

import java.util.List;

public class ContactDetailActivity extends AppCompatActivity {

    private ContactBean mContactBean;

    private ListView mListView;

    private MyAdapter mAdapter;

    private CommonToolBar mCommonToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        mContactBean = getIntent().getParcelableExtra("contact");
        initView();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.contact_detail_photo_list);
        mAdapter = new MyAdapter(this, mListView, mContactBean.getTel());
        mCommonToolBar = (CommonToolBar) findViewById(R.id.contact_detail_toolbar);
        mCommonToolBar.setTitle(mContactBean.getName());
        mCommonToolBar.setAutoFinish(this);
        mAdapter.setContactOperate(new MyAdapter.IContactOperate() {
            @Override
            public void sendMsg(String phoneNo) {
                PhoneUtil.sendMessage(ContactDetailActivity.this, phoneNo);
            }

            @Override
            public void call(String phoneNo) {
                PhoneUtil.callPhones(ContactDetailActivity.this, phoneNo);
            }
        });
        mListView.setAdapter(mAdapter);
    }


    private static class MyAdapter extends BaseAdapter implements View.OnClickListener, AdapterView.OnItemClickListener {

        private Context mContext;

        private ListView mListView;

        private List<String> mTelList;

        private IContactOperate mContactOperate;

        public MyAdapter(Context context, ListView listView, List<String> telList) {
            this.mContext = context;
            this.mListView = listView;
            this.mTelList = telList;
            mListView.setOnItemClickListener(this);
        }

        @Override
        public int getCount() {
            return mTelList == null ? 0 : mTelList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTelList == null ? null : mTelList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.activity_contact_detail_list_item, null);
            }
            String phoneNo = (String) getItem(position);
            TextView tel = ViewHolder.get(convertView, R.id.contact_detail_tel);
            ImageButton sendMsg = ViewHolder.get(convertView, R.id.contact_detail_send_msg);
            sendMsg.setTag(phoneNo);
            sendMsg.setOnClickListener(this);
            tel.setText(phoneNo);
            return convertView;
        }

        /**
         * 发送短信被点击
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            String phoneNo = (String) v.getTag();
            if (mContactOperate != null) {
                mContactOperate.sendMsg(phoneNo);
            }
        }

        /**
         * listview每一项被点击，执行拨号操作
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String phoneNo = (String) getItem(position);
            if (mContactOperate != null) {
                mContactOperate.call(phoneNo);
            }
        }

        /**
         * 与联系人相关的回调
         */
        public interface IContactOperate {
            /**
             * 发送短信
             */
            void sendMsg(String phoneNo);

            /**
             * 拨打电话
             */
            void call(String phoneNo);
        }

        public void setContactOperate(IContactOperate contactOperate) {
            this.mContactOperate = contactOperate;
        }
    }
}

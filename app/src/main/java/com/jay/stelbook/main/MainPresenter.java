package com.jay.stelbook.main;

import android.util.Log;

import com.jay.javabean.ContactBean;
import com.jay.javabean.UserBean;
import com.jay.stelbook.main.base.IMainPresenter;
import com.jay.stelbook.main.base.IMainView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListListener;

/**
 * 联系人操作类
 * Created by ${jay} on ${2016/8/17
 */
public class MainPresenter extends QueryListListener<BatchResult> implements IMainPresenter {

    private static final String TAG = "MainPresenter";

    private IMainView mMainView;

    //由于后台限制最多只能批处理插入50条数据，所以分割分块上传
    private List<BmobBatch> bmobBatchs = new ArrayList<>();

    private int begin = 0;

    public MainPresenter(IMainView mainView) {
        this.mMainView = mainView;
    }

    @Override
    public void doBackup(final List<BmobObject> contactList) {
        mMainView.showWaitDlg();
        String uuid = UUID.randomUUID().toString();
        bmobBatchs.clear();
        begin = 0;
        for (BmobObject bmobObject : contactList) {
            //设置版本号
            ((ContactBean) bmobObject).setVersion(uuid);
            //设置用户
            ((ContactBean) bmobObject).setUser(BmobUser.getCurrentUser(UserBean.class));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < contactList.size()) {
                    List<BmobObject> bmobObjects;
                    if (i + 50 < contactList.size()) {
                        bmobObjects = contactList.subList(i, i + 50);
                        i += 50;
                    } else {
                        bmobObjects = contactList.subList(i, contactList.size());
                        i = contactList.size();
                    }
                    BmobBatch bmobBatch = new BmobBatch();
                    bmobBatch.insertBatch(bmobObjects);
                    bmobBatchs.add(bmobBatch);
                    Log.i(TAG, "doBackup: " + bmobObjects.size());
                }
                BmobBatch bmobBatch = bmobBatchs.get(begin);
                bmobBatch.doBatch(MainPresenter.this);
            }
        }).start();
    }


    @Override
    public void done(List<BatchResult> list, BmobException e) {
        if (e != null) {
            mMainView.showTip("备份失败" + e.getMessage(), 0);
            mMainView.hideWaitDlg();
        } else {
            begin++;
            if (begin >= bmobBatchs.size()) {
                mMainView.showTip("备份成功", 0);
                mMainView.hideWaitDlg();
            } else {
                bmobBatchs.get(begin).doBatch(this);
            }
        }
    }
}

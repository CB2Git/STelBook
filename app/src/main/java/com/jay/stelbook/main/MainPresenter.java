package com.jay.stelbook.main;

import com.jay.javabean.ContactBean;
import com.jay.javabean.UserBean;
import com.jay.stelbook.main.base.IMainPresenter;
import com.jay.stelbook.main.base.IMainView;

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
public class MainPresenter implements IMainPresenter {

    private IMainView mMainView;

    public MainPresenter(IMainView mainView) {
        this.mMainView = mainView;
    }

    @Override
    public void doBackup(List<BmobObject> contactList) {
        mMainView.showWaitDlg();
        String uuid = UUID.randomUUID().toString();
        for (BmobObject bmobObject : contactList) {
            //设置版本号
            ((ContactBean) bmobObject).setVersion(uuid);
            //设置用户
            ((ContactBean) bmobObject).setUser(BmobUser.getCurrentUser(UserBean.class));
        }
        BmobBatch bmobBatch = new BmobBatch();
        bmobBatch.insertBatch(contactList).doBatch(new QueryListListener<BatchResult>() {
            @Override
            public void done(List<BatchResult> list, BmobException e) {
                if (e != null) {
                    mMainView.showTip("备份失败" + e.getMessage(),0);
                } else {
                    mMainView.showTip("备份成功",0);
                }
                mMainView.hideWaitDlg();
            }
        });
    }


}

package com.jay.stelbook.restore;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jay.javabean.ContactBean;
import com.jay.javabean.UserBean;
import com.jay.stelbook.restore.base.IRestorePresenter;
import com.jay.stelbook.restore.base.IRestoreView;

import org.json.JSONArray;

import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * Created by ${jay} on ${2016/8/17
 */

public class RestorePresenter implements IRestorePresenter {

    private IRestoreView mRestoreView;

    public RestorePresenter(IRestoreView restoreView) {
        this.mRestoreView = restoreView;
    }

    @Override
    public void queryAllContactVersion(UserBean user) {
        BmobQuery<ContactBean> bmobQuery = new BmobQuery<>("Contacts");
        bmobQuery.addWhereEqualTo("user", user);
        bmobQuery.groupby(new String[]{"version"});
        bmobQuery.setLimit(1000);
        bmobQuery.order("-createdAt");
        bmobQuery.findObjectsByTable(new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                if (e == null) {
                    Gson gson = new Gson();
                    List<ContactBean> list = gson.fromJson(jsonArray.toString(), new TypeToken<List<ContactBean>>() {
                    }.getType());
                    mRestoreView.displayContactVersion(list);
                    Log.i("ttt", jsonArray.toString());
                } else {
                    mRestoreView.showTip("失败" + e.getMessage(), 0);
                    Log.i("ttt", e.getMessage());
                }
            }
        });
    }

    @Override
    public void deleteVersion(List<BmobObject> contacts) {
        BmobBatch bmobBatch = new BmobBatch();
        bmobBatch.deleteBatch(contacts);
        bmobBatch.doBatch(new QueryListListener<BatchResult>() {
            @Override
            public void done(List<BatchResult> list, BmobException e) {
                mRestoreView.updateDisplay();
                if (e == null) {
                    mRestoreView.showTip("成功", 0);
                } else {
                    mRestoreView.showTip("shibai", 0);
                }
            }
        });
    }
}

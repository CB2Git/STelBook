package com.jay.stelbook.base;

import android.app.Application;

import com.jay.stelbook.R;

import cn.bmob.v3.Bmob;

/**
 * Created by Jay on 2016/7/9.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化Bmob服务
        Bmob.initialize(getApplicationContext(), getResources().getString(R.string.app_key));
    }
}

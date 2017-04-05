package com.jay.stelbook;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;


public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "ApplicationTest";
    public ApplicationTest() {
        super(Application.class);
        ContactsTest();
    }

    public void ContactsTest(){
        Log.i(TAG,"begin of ContactsTest");
        Log.i(TAG,"end of ContactsTest");
    }
}
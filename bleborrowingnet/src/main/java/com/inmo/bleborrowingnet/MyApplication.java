package com.inmo.bleborrowingnet;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    public static Context instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = getApplicationContext();
    }
}
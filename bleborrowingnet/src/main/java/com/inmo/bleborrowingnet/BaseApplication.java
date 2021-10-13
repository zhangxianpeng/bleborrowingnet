package com.inmo.bleborrowingnet;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {

    private static BaseApplication instance;

    public BaseApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

}
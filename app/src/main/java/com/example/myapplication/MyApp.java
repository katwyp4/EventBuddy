package com.example.myapplication;

import android.app.Application;
import android.content.Context;

/** Ułatwia dostęp do kontekstu poza warstwą UI. */
public class MyApp extends Application {

    private static MyApp instance;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}

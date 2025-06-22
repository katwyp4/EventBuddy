package com.example.myapplication;

import android.app.Application;
import android.content.Context;

import com.jakewharton.threetenabp.AndroidThreeTen;

/** Ułatwia dostęp do kontekstu poza warstwą UI. */
public class MyApp extends Application {


    private static MyApp instance;

    @Override public void onCreate() {
        AndroidThreeTen.init(this);
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}

package com.taitsmith.busboy;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class BusboyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}

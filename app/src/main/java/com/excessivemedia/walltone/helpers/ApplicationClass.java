package com.excessivemedia.walltone.helpers;

import android.app.Application;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HighlightUtils.init(getApplicationContext());
        CategoryUtils.init(getApplicationContext());
        new LikeManager(this).update();
    }

}

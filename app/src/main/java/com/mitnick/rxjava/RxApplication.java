package com.mitnick.rxjava;

import android.app.Application;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by Michael Smith on 2016/7/21.
 */

public class RxApplication extends Application {

    private static RxApplication sInstance;

    public  synchronized static  RxApplication getInstance(){
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }


}

package com.mitnick.rxjava.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import com.mitnick.rxjava.net.HttpImpl;

import timber.log.Timber;

/**
 * Created by Michael Smith on 2016/7/24.
 */

public class BaseActivity extends AppCompatActivity{
    private final static String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i(TAG,"onCreate()");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i(TAG,"onResume()");
        EventBus.getDefault().register(this);
        HttpImpl.getInstance().register();
    }

    @Subscribe
    protected void onEventMainThread(Object event){

    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.i(TAG,"onPause()");
        EventBus.getDefault().unregister(this);
        HttpImpl.getInstance().unregister();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i(TAG,"onDestroy()");
    }
}

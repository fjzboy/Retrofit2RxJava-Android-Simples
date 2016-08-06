package com.mitnick.rxjava.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import com.mitnick.rxjava.net.FailedEvent;
import com.mitnick.rxjava.net.HttpImpl;
import com.mitnick.rxjava.net.MessageType;
import timber.log.Timber;

/**
 * Created by Michael Smith on 2016/7/24.
 */

public abstract class BaseActivity extends AppCompatActivity{
    protected final static String TAG = "BaseActivity";
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    public abstract void initView();

    public abstract void initData();

    public abstract void initEvent();

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        HttpImpl.getInstance().register(this);
    }

    @Subscribe
    protected  void onEventMainThread(Object event){
//        MessageType.handlerNetWorkException(this,event);
    };

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        HttpImpl.getInstance().unregister(this);
    }

    public void showProgressDialog(String message) {
        if(mProgressDialog == null){
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog(){
        if(mProgressDialog!=null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}

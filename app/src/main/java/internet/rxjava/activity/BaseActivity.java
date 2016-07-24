package internet.rxjava.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import internet.rxjava.net.HttpImpl;
import internet.rxjava.util.RxUtils;
import rx.subscriptions.CompositeSubscription;
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
        HttpImpl.getInstance(this).register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i(TAG,"onResume()");
        EventBus.getDefault().register(this);

    }

    @Subscribe
    protected void onEventMainThread(Object event){

    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.i(TAG,"onPause()");
        EventBus.getDefault().unregister(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i(TAG,"onDestroy()");
        HttpImpl.getInstance(this).unregister();
    }
}

package com.mitnick.rxjava.net;

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import com.mitnick.rxjava.bean.Profile;
import com.mitnick.rxjava.bean.Token;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Header;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by Michael Smith on 2016/7/24.
 */

public class HttpImpl {
    private final static String TAG = "HttpImpl";

    static volatile HttpImpl sInstance;

    static volatile  ServiceApi mApiClient;

    private CompositeSubscription mSubscriptions;

    int count = 0;

    static int num = 0;

    int sum = 0;

    public HttpImpl() {
    }

    public ServiceApi getApiClient(){
        if(mApiClient == null){
            synchronized (ServiceApi.class){
                sum++;
                Log.i(TAG,"ServiceApi.newInstance() excute :" + sum + "次");
                mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);
            }
        }
        return mApiClient;
    }

    //获取唯一单列
    public static HttpImpl getInstance() {
        if (sInstance == null) {
            synchronized (HttpImpl.class) {
                num++;
                Log.i(TAG,"HttpImpl.newInstance() excute :" + num + "次");
                sInstance = new HttpImpl();
            }
        }
        return sInstance;
    }

    private final void postEvent(Object object) {
        EventBus.getDefault().post(object);
    }

    //注册一个订阅者
    public void register() {
        if (mSubscriptions == null || mSubscriptions.isUnsubscribed()) {
            synchronized (this){
                count ++;
                Log.i(TAG,"register excute :" + count + "次");
                mSubscriptions = new CompositeSubscription();
            }
        }
    }

    //删除一个订阅者
    public void unregister() {
        if (mSubscriptions != null) {
            synchronized (this){
                Log.i(TAG,"unregister excute :" + count + "次");
                mSubscriptions.unsubscribe();
            }
        }
    }


    public void login(String auth) {
        mSubscriptions.add(getApiClient().login(auth)
//                              .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<Token>() {
                            @Override
                            public void onCompleted() {
                                Timber.i(TAG, "onCompleted");
                            }

                            @Override
                            public void onError(Throwable t) {
                                Timber.i(TAG, "onError：" + t.toString());
                                postEvent(new FailedEvent(FailedEvent.MessageType.LOGIN, t));
                            }

                            @Override
                            public void onNext(Token token) {
                                postEvent(token);
                            }
                        })
        );
    }

    public void getProfiles(String accessToken) {
        Call<Profile> call = getApiClient().getProfiles(accessToken);
        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful()) {
                    postEvent(response.body());
                } else {
                    postEvent(new FailedEvent(FailedEvent.MessageType.PROFILE));
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Timber.e("onFailure：" + t.toString());
                postEvent(new FailedEvent(FailedEvent.MessageType.PROFILE, t));
            }
        });
    }

    public void getProfile(String accessToken) {
        mSubscriptions.add(getApiClient().getProfile(accessToken)
//                              .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<Profile>() {
                            @Override
                            public void onCompleted() {
                                Timber.i("onCompleted");
                            }

                            @Override
                            public void onError(Throwable t) {
                                Timber.e("onError：" + t.toString());
                                postEvent(new FailedEvent(FailedEvent.MessageType.PROFILE));
                            }

                            @Override
                            public void onNext(Profile profile) {
                                postEvent(profile);
                            }
                        })
        );
    }

}

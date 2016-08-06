package com.mitnick.rxjava.net;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import com.mitnick.rxjava.bean.Profile;
import com.mitnick.rxjava.bean.RefreshRequest;
import com.mitnick.rxjava.bean.Token;
import com.mitnick.util.L;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Header;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by Michael Smith on 2016/7/24.
 */

public class HttpImpl {
    private final static String TAG = "HttpImpl";

    static volatile HttpImpl sInstance;
    static volatile ServiceApi mApiClient;

    private Context mContext;
    private CompositeSubscription mSubscriptions;


    public HttpImpl() {
    }

    public ServiceApi getApiClient() {
        if (mApiClient == null) {
            synchronized (this) {
                Log.i(TAG, "ServiceApi.newInstance() excute ");
                mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);
            }
        }
        return mApiClient;
    }

    //获取唯一单列
    public static HttpImpl getInstance() {
        if (sInstance == null) {
            synchronized (HttpImpl.class) {
                Log.i(TAG, "HttpImpl.newInstance() excute ");
                sInstance = new HttpImpl();
            }
        }
        return sInstance;
    }

    private final void postEvent(Object object) {
        EventBus.getDefault().post(object);
    }

    //注册一个订阅者
    public void register(Context context) {
        this.mContext = context;
        if (mSubscriptions == null || mSubscriptions.isUnsubscribed()) {
                L.i(TAG, "CompositeSubscription register excute");
                mSubscriptions = new CompositeSubscription();
        }
    }

    //删除一个订阅者
    public void unregister(Context context) {
        this.mContext = null;
        if (mSubscriptions != null) {
            L.i(TAG, "CompositeSubscription unregister excute");
            mSubscriptions.unsubscribe();
        }
    }

    public void login(String auth) {
        mSubscriptions.add(getApiClient().login(auth)
//                      .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<Token>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "onCompleted");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                String message = throwable.getMessage().indexOf("504") != -1 ? "请检查网络设置..." : throwable.getMessage();
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                postEvent(new FailedEvent(MessageType.LOGIN, throwable));
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
                    postEvent(new FailedEvent(MessageType.PROFILE));
                    String message = response.code() == 504 ? "请检查网络设置..." : (response.code() == 401) ? "令牌已过期，请重新登录..." : response.code() + "";
                    Toast.makeText(mContext, "请求失败！" + message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable throwable) {
                postEvent(new FailedEvent(MessageType.PROFILE, throwable));
                L.e(TAG,throwable.getMessage());
                String message = throwable.getMessage().indexOf("504") != -1 ? "请检查网络设置..." : throwable.getMessage().indexOf("401") != -1 ? "令牌已过期，请重新登录..." : throwable.getMessage();
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getProfile(String accessToken) {
        mSubscriptions.add(getApiClient().getProfile(accessToken)
        //               .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<Profile>() {
                            @Override
                            public void onCompleted() {
                                L.i("onCompleted");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                L.e("onError：" + throwable.toString());
                                postEvent(new FailedEvent(MessageType.PROFILE, throwable));
                            }

                            @Override
                            public void onNext(Profile profile) {
                                postEvent(profile);
                            }
                        })
        );
    }

    public void refresh(String refreshToken) {
        Call<Token> call = getApiClient().refresh(new RefreshRequest(refreshToken));
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    postEvent(response.body());
                } else {
                    postEvent(new FailedEvent(MessageType.REFRESH));
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable throwable) {
                postEvent(new FailedEvent(MessageType.REFRESH, throwable));
            }
        });
    }

}

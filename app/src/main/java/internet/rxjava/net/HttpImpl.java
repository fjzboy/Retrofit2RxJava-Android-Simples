package internet.rxjava.net;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import internet.rxjava.bean.Profile;
import internet.rxjava.bean.Token;
import internet.rxjava.lnterface.Observer;
import internet.rxjava.util.RxUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    private Context mContext;

    static volatile  ServiceApi mApiClient;
    private CompositeSubscription mSubscriptions;

    public HttpImpl(Context context) {
        this.mContext = context;

    }

    public ServiceApi getApiClient(){
        if(mApiClient == null){
            synchronized (ServiceApi.class){
                mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);
            }
        }
        return mApiClient;
    }

    //获取唯一单列
    public static HttpImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (HttpImpl.class) {
                sInstance = new HttpImpl(context);
            }
        }
        return sInstance;
    }

    private final void postEvent(Object object) {
        EventBus.getDefault().post(object);
    }

    //注册一个订阅者
    public synchronized void register() {
//        mSubscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(mSubscriptions);
//        mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);

        if (mSubscriptions == null || mSubscriptions.isUnsubscribed()) {
            mSubscriptions = new CompositeSubscription();
        }
    }

    //删除一个订阅者
    public synchronized void unregister() {
//        RxUtils.unSubscribeIfNotNull(mSubscriptions);
//        mApiClient = null;
        if (mSubscriptions != null) {
            mSubscriptions.unsubscribe();
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
                                postEvent(new FailedEvent(MessageType.LOGIN, t));
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
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Timber.e("onFailure：" + t.toString());
                postEvent(new FailedEvent(MessageType.PROFILE, t));
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
                                postEvent(new FailedEvent(MessageType.PROFILE));
                            }

                            @Override
                            public void onNext(Profile profile) {
                                postEvent(profile);
                            }
                        })
        );
    }


}

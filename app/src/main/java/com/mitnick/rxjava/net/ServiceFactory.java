package com.mitnick.rxjava.net;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.mitnick.rxjava.RxApplication;
import com.mitnick.rxjava.util.NetUtils;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by Michael Smith on 2016/7/21.
 */

public class ServiceFactory {

    public final static String TAG = "ServiceFactory";

    private ServiceFactory(){}

    /**
     * 有网根据过期时间重新请求
     * 有网络缓存一分钟
     * 无网读缓存，离线缓存4周
     * @param service
     * @param <T>
     * @return
     */
    public static <T>T createRetrofit2RxJavaService(final Class<T> service) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getCacheOkHttpClient())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ServiceApi.baseurl)
                .build();

        return retrofit.create(service);
    }

    private static OkHttpClient getCacheOkHttpClient() {
        //设置缓存路径
        final File httpCacheDirectory = new File(RxApplication.getInstance().getCacheDir(), "okhttpCache");

        Timber.e(TAG, httpCacheDirectory.getAbsolutePath());
        //设置缓存 10M
        Cache cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);   //缓存可用大小为10M

        final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

        /**
         * 无网直接读缓存
         * mobile network 情况下缓存一分钟,过期重新请求
         * wifi network 情况下不使用缓存
         * none network 情况下离线缓存4周
         */
        final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                //获取网络状态
                int netWorkState = NetUtils.getNetworkState(RxApplication.getInstance());
                //无网络请求强制使用缓存
                if (netWorkState == NetUtils.NETWORN_NONE) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }

                Response originalResponse = chain.proceed(request);

                switch (netWorkState) {
                    case NetUtils.NETWORN_MOBILE://mobile network 情况下缓存一分钟
                        int maxAge = 60;
                        return originalResponse.newBuilder()
                                .removeHeader("Pragma")
                                .removeHeader("Cache-Control")
                                .header("Cache-Control", "public, max-age=" + maxAge)
                                .build();

                    case NetUtils.NETWORN_WIFI://wifi network 情况下不使用缓存
                        maxAge = 0;
                        return originalResponse.newBuilder()
                                .removeHeader("Pragma")
                                .removeHeader("Cache-Control")
                                .header("Cache-Control", "public, max-age=" + maxAge)
                                .build();

                    case NetUtils.NETWORN_NONE://none network 情况下离线缓存4周
                        int maxStale = 60 * 60 * 24 * 4 * 7;
                        return originalResponse.newBuilder()
                                .removeHeader("Pragma")
                                .removeHeader("Cache-Control")
                                .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                                .build();
                    default:
                        throw new IllegalStateException("network state  is Erro!");
                }
            }
        };

        return new OkHttpClient.Builder()
                .writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                 //设置拦截器，显示日志信息
                .addInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache)
                .build();
    }

/*
    *//**
     * 日志拦截器
     *//*
    private  final Interceptor LoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();
            Log.i(TAG, String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            Log.i(TAG, String.format("Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            long  time = t2 - t1;
            Log.i(TAG, "LoggingInterceptor duration：" + time+" ms");
            return response;
        }
    };*/
}

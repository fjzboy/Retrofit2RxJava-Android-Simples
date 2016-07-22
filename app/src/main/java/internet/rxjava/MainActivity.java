package internet.rxjava;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import internet.rxjava.bean.Profile;
import internet.rxjava.bean.Token;
import internet.rxjava.util.RxUtils;
import internet.rxjava.net.ServiceApi;
import internet.rxjava.net.ServiceFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private CompositeSubscription mSubscriptions = new CompositeSubscription();
    private ServiceApi mApiClient;

    private String mAuth = "Basic dG1qMDAxOjEyMzQ1Ng==";
    private String mAccessToken = "";

    private TextView mTextView;
    private Button mRetrofitButton,mRxjavaButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);
        mRetrofitButton = (Button) findViewById(R.id.retrofitButton);
        mRxjavaButton = (Button) findViewById(R.id.rxjavaButton);

        mProgressDialog  = new ProgressDialog(this);
        mProgressDialog.setTitle("wait...");

        mSubscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(mSubscriptions);

        mRxjavaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTextView.setText("");
                mProgressDialog.show();

                mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);

                mSubscriptions.add(mApiClient.getProfile(mAccessToken)
//                              .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Profile>() {
                                    @Override
                                    public void onCompleted() {
                                        Timber.i("onCompleted");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Timber.e("onError：" + e.toString());
                                        mTextView.setText("获取用户信息onError：" + e.toString());
                                        mProgressDialog.dismiss();
                                    }

                                    @Override
                                    public void onNext(Profile profile) {
                                        mProgressDialog.dismiss();
                                        mTextView.setText("获取用户信息成功：" + profile.getUsername());
                                    }
                                })
                );
            }
        });

        mRetrofitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTextView.setText("");
                mProgressDialog.show();

                mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);

                Call<Profile> call = mApiClient.getProfiles(mAccessToken);
                call.enqueue(new Callback<Profile>() {
                    @Override
                    public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (response.isSuccessful()) {
                            mTextView.setText("获取用户信息成功：" + response.body().getUsername());
                        }else{
                            mTextView.setText("失败");
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Profile> call, Throwable t) {
                        Timber.e( "onFailure：" + t.toString());
                        mTextView.setText("获取用户信息onFailure:" + t.toString());
                        mProgressDialog.dismiss();
                    }
                });
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.show();

                mApiClient = ServiceFactory.createRetrofit2RxJavaService(ServiceApi.class);

                mSubscriptions.add(mApiClient.login(mAuth)
//                .debounce(400, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Token>() {
                                    @Override
                                    public void onCompleted() {
                                        Timber.i("onCompleted");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Timber.e("onError：" + e.toString());
                                        mTextView.setText("获取Token onError：" + e.toString());
                                        mProgressDialog.dismiss();
                                    }

                                    @Override
                                    public void onNext(Token token) {
                                        mAccessToken = token.getAccess_token();
                                        mTextView.setText("获取Token成功！" + token.getAccess_token() );
                                        mProgressDialog.dismiss();
                                    }
                                })
                );
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.unSubscribeIfNotNull(mSubscriptions);
    }
}

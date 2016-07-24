package internet.rxjava.activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import internet.rxjava.R;
import internet.rxjava.bean.Profile;
import internet.rxjava.bean.Token;
import internet.rxjava.net.FailedEvent;
import internet.rxjava.net.HttpImpl;
import internet.rxjava.net.MessageType;
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

public class MainActivity extends BaseActivity {

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

        mRxjavaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("mRxjavaButton");
                mProgressDialog.show();
                HttpImpl.getInstance(MainActivity.this).getProfiles(mAccessToken);
            }
        });

        mRetrofitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("mRetrofitButton");
                mProgressDialog.show();
                HttpImpl.getInstance(MainActivity.this).getProfile(mAccessToken);
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                HttpImpl.getInstance(MainActivity.this).login(mAuth);

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
    protected void onEventMainThread(Object event) {
        super.onEventMainThread(event);
        mProgressDialog.dismiss();
        if(event instanceof Token){
            Token token = (Token) event;
            mTextView.setText("获取Token成功！" + token.getAccess_token() );
            mAccessToken = token.getAccess_token();
        }
        if(event instanceof Profile){
            Profile profile = (Profile) event;
            mTextView.setText("获取用户信息成功：" + profile.getUsername());
        }
        if(event instanceof FailedEvent){
            int type = ((FailedEvent) event).getType();
            switch (type){
                case MessageType.LOGIN:
                    mTextView.setText("获取Token onError：");
                    break;
                case MessageType.PROFILE:
                    mTextView.setText("获取Profile onError：");
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

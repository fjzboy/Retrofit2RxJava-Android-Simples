package com.mitnick.rxjava.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mitnick.rxjava.R;
import com.mitnick.rxjava.bean.Profile;
import com.mitnick.rxjava.bean.Token;
import com.mitnick.rxjava.net.FailedEvent;
import com.mitnick.rxjava.net.HttpImpl;

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
                HttpImpl.getInstance().getProfiles(mAccessToken);
            }
        });

        mRetrofitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("mRetrofitButton");
                mProgressDialog.show();
                HttpImpl.getInstance().getProfile(mAccessToken);
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                HttpImpl.getInstance().login(mAuth);

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
            startActivity(new Intent().setClass(this,MainActivity.class));
        }
        if(event instanceof FailedEvent){
            int type = ((FailedEvent) event).getType();
            switch (type){
                case FailedEvent.MessageType.LOGIN:
                    mTextView.setText("获取Token onError：");
                    break;
                case FailedEvent.MessageType.PROFILE:
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

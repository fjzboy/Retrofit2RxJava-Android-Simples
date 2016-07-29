package com.mitnick.rxjava.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mitnick.rxjava.R;
import com.mitnick.rxjava.bean.Profile;
import com.mitnick.rxjava.bean.Token;
import com.mitnick.rxjava.net.FailedEvent;
import com.mitnick.rxjava.net.HttpImpl;
import com.mitnick.rxjava.net.MessageType;

public class MainActivity extends BaseActivity {

    private String mAuth = "Basic dG1qMDAxOjEyMzQ1Ng==";
    private String mAccessToken = "";

    private TextView mTextView;
    private Button mRetrofitButton,mRxjavaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mTextView = (TextView) findViewById(R.id.textView);
        mRetrofitButton = (Button) findViewById(R.id.retrofitButton);
        mRxjavaButton = (Button) findViewById(R.id.rxjavaButton);

        initData();


        mRxjavaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("mRxjavaButton");
                showProgressDialog("wait...");
                HttpImpl.getInstance().getProfiles(mAccessToken);
            }
        });

        mRetrofitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("mRetrofitButton");
                showProgressDialog("wait...");
                HttpImpl.getInstance().getProfile(mAccessToken);
            }
        });
    }

    public void initData(){
        if(getIntent().getExtras()!=null){
            mAccessToken = getIntent().getExtras().getString("accessToken","");
            mTextView.setText("获取Token成功！" + mAccessToken);
        }else{
            mTextView.setText("获取token失败，请重新登录！");
        }
    }

    @Override
    protected void onEventMainThread(Object event) {
        super.onEventMainThread(event);
        hideProgressDialog();
        if(event instanceof Token){
            Token token = (Token) event;
            mTextView.setText("获取Token成功！" + token.getAccess_token() );
            mAccessToken = token.getAccess_token();
        }
        if(event instanceof Profile){
            Profile profile = (Profile) event;
            mTextView.setText("获取用户信息成功：" + profile.getUsername());
            Toast.makeText(this, "Profile name is " + profile.getUsername(), Toast.LENGTH_SHORT).show();
//            startActivity(new Intent().setClass(this,MainActivity.class));
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
}

package com.mitnick.rxjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mitnick.rxjava.R;
import com.mitnick.rxjava.RxApplication;
import com.mitnick.rxjava.bean.Token;
import com.mitnick.rxjava.net.FailedEvent;
import com.mitnick.rxjava.net.HttpImpl;
import com.mitnick.rxjava.net.MessageType;
import com.mitnick.rxjava.util.PreferenceUtils;

/**
 * Created by mitnick.cheng on 2016/7/28.
 */

public class LoginActivity extends BaseActivity {
    String mAccessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //activity_login.xml 中onClick方法
    void login(View view){
        showProgressDialog("login...");
        HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
    }


    @Override
    protected void onEventMainThread(Object event) {
        if(event instanceof Token){
            hideProgressDialog();
            Token token = (Token) event;
            mAccessToken = token.getAccess_token();
            PreferenceUtils.setPrefString(RxApplication.getInstance(),"refreshToken",token.getRefresh_token());
            Toast.makeText(LoginActivity.this,"登录成功！",Toast.LENGTH_LONG).show();
            Intent intent = new Intent().setClass(LoginActivity.this,MainActivity.class);
            intent.putExtra("accessToken",mAccessToken);
            startActivity(intent);
        }
        if(event instanceof FailedEvent){
            hideProgressDialog();
            int type = ((FailedEvent) event).getType();
            String message = ((FailedEvent) event).getObject()!=null && ((Throwable) ((FailedEvent) event).getObject()).getMessage().indexOf("504")!=-1 ? "请检查网络设置...":"";
            switch (type){
                case MessageType.LOGIN:
                    Toast.makeText(LoginActivity.this,message,Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(LoginActivity.this,"应用程序异常！",Toast.LENGTH_LONG).show();
            }
        }
    }
}

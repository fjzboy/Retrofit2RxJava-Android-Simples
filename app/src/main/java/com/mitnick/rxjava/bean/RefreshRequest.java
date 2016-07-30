package com.mitnick.rxjava.bean;

/**
 * Created by WH1604025 on 2016/6/3.
 */
public class RefreshRequest {
    private String refreshToken;

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

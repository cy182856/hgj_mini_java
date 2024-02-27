package com.ej.hgj.entity.login;

public class WechatPubConfDo {
    
    private String custId;

    private String pubOrgId;

    private String pubAppId;

    private String appSecret;

    private String accessToken;

    private String stat;

    private String updTime;
    private String singleOrMutl;

    public String getSingleOrMutl() {
        return singleOrMutl;
    }

    public void setSingleOrMutl(String singleOrMutl) {
        this.singleOrMutl = singleOrMutl;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getPubOrgId() {
        return pubOrgId;
    }

    public void setPubOrgId(String pubOrgId) {
        this.pubOrgId = pubOrgId;
    }

    public String getPubAppId() {
        return pubAppId;
    }

    public void setPubAppId(String pubAppId) {
        this.pubAppId = pubAppId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getUpdTime() {
        return updTime;
    }

    public void setUpdTime(String updTime) {
        this.updTime = updTime;
    }
}
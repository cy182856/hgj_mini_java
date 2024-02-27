package com.ej.hgj.entity.login;

import lombok.Data;

@Data
public class MiniProSession {
    private String openid;
    private String sessionKey;
    private String unionid;

    public MiniProSession() {
    }



}

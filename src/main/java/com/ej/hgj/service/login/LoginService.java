package com.ej.hgj.service.login;

import com.ej.hgj.entity.login.LoginInfo;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public interface LoginService {
     LoginInfo doLogin(String code, String custId, String wxSeqId, String openId, String huSeqId) throws NoSuchAlgorithmException, IOException, KeyManagementException;

}

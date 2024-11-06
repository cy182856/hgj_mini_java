package com.ej.hgj.service.carpay;


import com.ej.hgj.vo.bill.SignInfoVo;
import okhttp3.HttpUrl;

public interface CarPayService {

    // 获取证书token,签名时Header中的Authorization
    String getToken(String method, HttpUrl url, String body, SignInfoVo signInfoVo, String proNum);

}

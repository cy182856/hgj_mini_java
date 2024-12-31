package com.ej.hgj.service.carpay;


import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.vo.bill.SignInfoVo;
import okhttp3.HttpUrl;

public interface CarPayService {

    // 获取证书token,签名时Header中的Authorization
    String getToken(String method, HttpUrl url, String body, SignInfoVo signInfoVo, String proNum);

    // 支付成功,更新订单支付状态,调用停车场支付回调接口，扣减客户停车卡时间, 插入账单
    void updateStatusSuccess(ParkPayOrder parkPayOrder);

    // 支付失败,更新订单支付状态
    void updateStatusFail(ParkPayOrder parkPayOrder);

    // 更新订单为支付中状态
    void updateStatusPro(ParkPayOrder parkPayOrder);

}

package com.ej.hgj.service.bill;



import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.role.Role;
import com.ej.hgj.vo.bill.SignInfoVo;
import okhttp3.HttpUrl;

import java.math.BigDecimal;
import java.util.List;

public interface BillService {

    // 获取证书token,签名时Header中的Authorization
    String getToken(String method, HttpUrl url, String body, SignInfoVo signInfoVo, String proNum);

    // 更新思源订单状态
    void updateSyOrderStatus(String id, String openId, String outTradeNo, String fee, String orgId,
                             String fnRevId, String payMoney, String fillProName);

    // 支付成功,更新支付记录，大订单，小订单支付状态,同时更新思源缴费状态
    void updateStatusSuccess(PaymentRecord paymentRecord);

    // 支付成功,更新支付记录，大订单，小订单支付状态,不更新思源缴费状态
    void updateStatusSuccessNotSy(PaymentRecord paymentRecord);

    // 支付失败,更新支付记录，大订单，小订单支付状态
    void updateStatusFail(PaymentRecord paymentRecord);

    // 账单合并
    void billMerge(String proNum, String wxOpenId, String cstCode,
                   BigDecimal totalAmount, String[] orderIds);

    // 更新订单为超时状态
    void updateStatusTimeOut(PaymentRecord paymentRecord_0);

    // 更新订单为支付中状态
    void updateStatusPro(PaymentRecord paymentRecord_0);

    // 订单取消
    void updateStatusCancel(String orderNo);

    // 订单超时
    Boolean updateStatusTimeOut(BillMerge billMerge);
}

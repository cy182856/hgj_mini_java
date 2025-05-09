package com.ej.hgj.service.xhparkpay;


import com.ej.hgj.entity.card.CardCstBatch;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.CarPayRequestVo;
import okhttp3.HttpUrl;

public interface XhParkPayService {

    // 车牌优惠成功,新增车牌优惠记录,扣减客户停车卡时间, 插入账单
    void carNoCouponSuccess(CarPayRequestVo carPayRequestVo);

}

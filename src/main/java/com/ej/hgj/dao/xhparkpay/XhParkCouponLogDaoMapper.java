package com.ej.hgj.dao.xhparkpay;

import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.xhparkpay.XhParkCoupon;
import com.ej.hgj.entity.xhparkpay.XhParkCouponLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface XhParkCouponLogDaoMapper {

    void save(XhParkCouponLog xhParkCouponLog);

    List<XhParkCouponLog> getList(XhParkCouponLog xhParkCouponLog);

    List<XhParkCouponLog> getListByCarCode(String carCode);


}

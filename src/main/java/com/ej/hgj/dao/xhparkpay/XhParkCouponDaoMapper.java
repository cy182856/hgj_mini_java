package com.ej.hgj.dao.xhparkpay;

import com.ej.hgj.entity.role.Role;
import com.ej.hgj.entity.xhparkpay.XhParkCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface XhParkCouponDaoMapper {

    XhParkCoupon getByCouponHour(String couponHour);

}

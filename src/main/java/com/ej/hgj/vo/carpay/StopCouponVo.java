package com.ej.hgj.vo.carpay;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StopCouponVo {

    private String couponId;

    private String couponName;

   private BigDecimal couponAmount;

}

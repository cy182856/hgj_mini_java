package com.ej.hgj.vo.carrenew;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarRenewInfoVo {

    private String carCode;

    private String carTypeNo;

    private String beginTime;

    private String endTime;

    private String userName;

    private String phone;

    private String homeAddress;

    private String monthAmount;

}

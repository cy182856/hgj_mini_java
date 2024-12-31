package com.ej.hgj.vo.carpay;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarInfoVo {

    private String carCode;

    private String inParkTime;

    private String outParkTime;

    private BigDecimal totalAmount;

    private String parkDur;

}

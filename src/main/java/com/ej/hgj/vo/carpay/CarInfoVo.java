package com.ej.hgj.vo.carpay;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarInfoVo {

    private String carCode;

    private String inParkTime;

    private String stopCarTime;

    private BigDecimal totalAmount;

}

package com.ej.hgj.vo.moncarren;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonCarRenInfoVo {

    private String carCode;

    private String beginTime;

    private String endTime;

    private String userName;

    private String userTel;

    private String userAddress;

    private String ruleID;

    private String ruleName;

    private Integer ruleType;

    private Integer ruleCount;

    private Integer ruleAmount;

    private BigDecimal monthAmount;

}

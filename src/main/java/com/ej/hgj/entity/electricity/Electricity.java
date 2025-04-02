package com.ej.hgj.entity.electricity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Electricity {

    private String userId;

    private String userName;

    private String roomId;

    private String meterId;

    private String date;

    private Double usedPower;

    private Double sumUsedPower;

    private String sumUsedPowerStr;

    private String dateTime;

    private String money;

}

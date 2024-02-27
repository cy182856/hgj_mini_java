package com.ej.hgj.vo.bill;

import lombok.Data;

import java.util.List;

@Data
public class RequestPaymentStatusVo {

    private String orderNo;


    private String cstCode;


    private String proNum;


    private String wxOpenId;


    private List<Integer> syPayStatusList;


    private List<Integer> paymentStatusList;

}

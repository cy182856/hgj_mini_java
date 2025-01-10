package com.ej.hgj.vo.carrenew;

import lombok.Data;

import java.util.List;

@Data
public class CarRenewOrderStatusVo {

    private String id;


    private String cstCode;


    private String proNum;


    private String wxOpenId;


    private List<Integer> orderStatusList;

}

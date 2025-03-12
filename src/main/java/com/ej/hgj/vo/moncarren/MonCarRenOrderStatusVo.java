package com.ej.hgj.vo.moncarren;

import lombok.Data;

import java.util.List;

@Data
public class MonCarRenOrderStatusVo {

    private String id;


    private String cstCode;


    private String proNum;


    private String wxOpenId;


    private List<Integer> orderStatusList;

}

package com.ej.hgj.vo.carpay;

import lombok.Data;

import java.util.List;

@Data
public class RequestOrderStatusVo {

    private String id;


    private String cstCode;


    private String proNum;


    private String wxOpenId;


    private List<Integer> syPayStatusList;


    private List<Integer> orderStatusList;

}

package com.ej.hgj.base;

import lombok.Data;

@Data
public class BaseRespVo {

    private String hgjOpenId;

    private String wxOpenId;

    private String userName;

    private String cstCode;

    private String cstName;

    private String proNum;

    private String proName;

    /**
     * 系统响应码
     */
    private String respCode;
    /**
     * 业务返回码
     */
    private String errCode;
    /**
     * 业务返回描述
     */
    private String errDesc;

    private Object data;

    // 入住状态
    private String isCHeck;

    /**
     * 房屋编号
     */
    private String houseId;

    /**
     * 房屋名称
     */
    private String houseName;


}

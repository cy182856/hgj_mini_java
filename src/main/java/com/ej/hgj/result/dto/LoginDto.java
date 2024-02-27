package com.ej.hgj.result.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDto implements Serializable {
    private static final long serialVersionUID = 5283425345416190705L;
    /**
     * 企业客户号
     */
    private String custId;
    /**
     * 慧管小程序用户的openid
     */
    private String hgjOpenId;
    /**
     * 住户序列号
     */
    private String huSeqId;
    /**
     * 住房序列号
     */
    private String houseSeqId;
    /**
     * 微信序号
     */
    private String wxSeqId;

    /**
     * 区域名
     */
    private String areaName;
    /**
     * 楼名称
     */
    private String buildingName;
    /**
     * 门牌号
     */
    private String houseNo;
}

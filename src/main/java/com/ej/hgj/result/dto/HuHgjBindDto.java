package com.ej.hgj.result.dto;

import lombok.Data;

/**
 * 住户慧管家绑定表
 */
@Data
public class HuHgjBindDto {
    /**
     * 慧管家OPENID
     */
    private String hgjOpenId;

    /**
     * 微信序号
     */
    private String wxSeqId;

    /**
     * 企业客户号
     */
    private String custId;

    /**
     * 住户序列号
     */
    private String huSeqId;

    /**
     * 住房序列号
     */
    private String houseSeqId;

    /**
     * 状态
     */
    private String stat;
    /**
     * 区域名
     */
    private String areaName;
    /**
     * 楼号名
     */
    private String buildingName;
    /**
     * 门牌号
     */
    private String houseNo;

    /**
     * 企业简称
     */
    private String name;

}

package com.ej.hgj.entity.hu;

import lombok.Data;

/**
    * 住户慧管家绑定表
    */
@Data
public class HuHgjBindDo {
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

}
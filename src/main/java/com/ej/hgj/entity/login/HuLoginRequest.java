package com.ej.hgj.entity.login;

import lombok.Data;
import java.io.Serializable;

@Data
public class HuLoginRequest implements Serializable {

    /**  */
    private static final long serialVersionUID = 1450019961227536030L;
    /**
     * 慧管家小程序的openId
     */
    private String openId;
    /**
     * 微信序号,多住房场景
     */
    private String wxSeqId;
    /**
     * 企业客户号
     */
    private String custId;
    /**
     * 住房序列号
     */
    private String huSeqId;

}

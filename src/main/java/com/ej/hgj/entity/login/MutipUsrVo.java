package com.ej.hgj.entity.login;

import lombok.Data;

@Data
public class MutipUsrVo {
    private String hgjOpenId;
    private String custId;
    private String code;
    private String cstCode;
    private String cstName;
    private String wxOpenId;
    /**
     * 公众号原始ID
     */
    private String pubOrgId;
    /**
     * 房屋编号
     */
    private String houseId;

}

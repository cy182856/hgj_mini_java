package com.ej.hgj.enums;

import org.apache.commons.lang3.StringUtils;

public enum PropTypeEnum {
    R("R", "住宅"),
    B("B", "商业");

    private String code;
    private String desc;

    private PropTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }

    public static PropTypeEnum getMatched(String code) {
        PropTypeEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            PropTypeEnum propTypeEnum = var1[var3];
            if (StringUtils.equals(code, propTypeEnum.getCode())) {
                return propTypeEnum;
            }
        }

        return null;
    }
}

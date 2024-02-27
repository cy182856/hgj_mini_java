package com.ej.hgj.enums;

import org.apache.commons.lang3.StringUtils;

public enum HouseUsrInfoStatEnum {
    I("I", "未认领"),
    P("P", "已认领,待审核"),
    N("N", "正常"),
    C("C", "已关闭"),
    D("D", "已销户");

    private String stat;
    private String statDesc;

    public String getStat() {
        return this.stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getStatDesc() {
        return this.statDesc;
    }

    public void setStatDesc(String statDesc) {
        this.statDesc = statDesc;
    }

    private HouseUsrInfoStatEnum(String stat, String statDesc) {
        this.stat = stat;
        this.statDesc = statDesc;
    }

    public static HouseUsrInfoStatEnum getMatched(String stat) {
        HouseUsrInfoStatEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            HouseUsrInfoStatEnum houseUsrInfoStatEnum = var1[var3];
            if (StringUtils.equals(stat, houseUsrInfoStatEnum.getStat())) {
                return houseUsrInfoStatEnum;
            }
        }

        return null;
    }
}

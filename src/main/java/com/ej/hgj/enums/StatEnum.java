package com.ej.hgj.enums;

public enum StatEnum {
    I("I", "初始"),
    N("N", "正常"),
    C("C", "关闭"),
    T("T", "临时关闭"),
    D("D", "销户");

    private String code;
    private String codeDesc;

    private StatEnum(String code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    public String getCode() {
        return this.code;
    }

    public String getCodeDesc() {
        return this.codeDesc;
    }

    public static StatEnum valueByCode(String code) {
        StatEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            StatEnum stat = var1[var3];
            if (stat.getCode().equals(code)) {
                return stat;
            }
        }

        return null;
    }
}

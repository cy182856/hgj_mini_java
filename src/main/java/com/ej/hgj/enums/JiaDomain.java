package com.ej.hgj.enums;

public enum JiaDomain {
    JIA_DOMAIN("JIA", "慧管家业务域"),
    ARCH_DOMAIN("ARCH", "底层框架域");

    private String domainCode;
    private String domainName;

    private JiaDomain(String domainCode, String domainName) {
        this.domainCode = domainCode;
        this.domainName = domainName;
    }

    public String getDomainCode() {
        return this.domainCode;
    }

    public String getDomainName() {
        return this.domainName;
    }
}

package com.ej.hgj.enums;

import com.ej.hgj.enums.JiaDomain;

public enum JiaSystem {
    JIA("JIA", "慧管家业务系统", JiaDomain.JIA_DOMAIN, "01"),
    ARCH("ARCH", "ARCH", JiaDomain.ARCH_DOMAIN, "09");

    private final JiaDomain domain;
    private final String systemCode;
    private final String systemName;
    private final String systemNo;

    private JiaSystem(String systemCode, String systemName, JiaDomain domain, String systemNo) {
        this.systemCode = systemCode;
        this.systemName = systemName;
        this.domain = domain;
        this.systemNo = systemNo;
    }

    public JiaDomain getDomain() {
        return this.domain;
    }

    public String getSystemCode() {
        return this.systemCode;
    }

    public String getSystemName() {
        return this.systemName;
    }

    public String getSystemNo() {
        return this.systemNo;
    }
}

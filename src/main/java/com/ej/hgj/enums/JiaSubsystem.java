package com.ej.hgj.enums;

public enum JiaSubsystem {
    JIABSV("JIABSV", "慧管家基础系统", JiaSystem.JIA, "01"),
    JIASV("JIASV", "慧管家小程序服务", JiaSystem.JIA, "03"),
    JIAMSV("JIAMSV", "慧管家物业小程序服务", JiaSystem.JIA, "05"),
    JIASUPSV("JIASUPSV", "慧管家供应商小程序服务", JiaSystem.JIA, "06"),
    JIAPUB("JIAPUB", "慧管家物业公众号服务", JiaSystem.JIA, "07"),
    JIASUPPUB("JIASUPPUB", "慧管家供应商公众号服务", JiaSystem.JIA, "08"),
    JIAMER("JIAMER", "慧管家物业控台", JiaSystem.JIA, "09"),
    JIASUP("JIASUP", "慧管家供应商控台", JiaSystem.JIA, "10"),
    JIAMAG("JIAMAG", "慧管家内部控台", JiaSystem.JIA, "11"),
    CONFIGCENTER("CONFIGCENTER", "配置服务", JiaSystem.ARCH, "01"),
    CAPTCHACENTER("CAPTCHACENTER", "验证码中心", JiaSystem.ARCH, "03"),
    REMOTEAG("REMOTEAG", "保障中心", JiaSystem.ARCH, "05"),
    SCHEDULER("SCHEDULER", "调度中心", JiaSystem.ARCH, "07"),
    OPS("OPS", "配置中心控台", JiaSystem.ARCH, "09");

    private final JiaSystem system;
    private final String subsystemCode;
    private final String subsystemName;
    private final String subsystemNo;

    private JiaSubsystem(String subsystemCode, String subsystemName, JiaSystem system, String subsystemNo) {
        this.subsystemCode = subsystemCode;
        this.subsystemName = subsystemName;
        this.system = system;
        this.subsystemNo = subsystemNo;
    }

    public static JiaSubsystem getMatchedSubsystemBySubsystemCode(String subsystemCode) {
        JiaSubsystem[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            JiaSubsystem subsystem = var1[var3];
            if (subsystem.getSubsystemCode().equals(subsystemCode)) {
                return subsystem;
            }
        }

        return null;
    }

    public static JiaSubsystem getMatchedSubsystemByWholesystemNo(String wholeSystemNo) {
        String systemNo = wholeSystemNo.substring(0, 2);
        String subSystemNo = wholeSystemNo.substring(2, 4);
        JiaSystem[] var3 = JiaSystem.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            JiaSystem system = var3[var5];
            if (system.getSystemNo().equals(systemNo)) {
                JiaSubsystem[] var7 = values();
                int var8 = var7.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    JiaSubsystem subSystem = var7[var9];
                    if (system == subSystem.getSystem() && subSystem.getSubsystemNo().equals(subSystemNo)) {
                        return subSystem;
                    }
                }
            }
        }

        return null;
    }

    public String getWholeSystemNo() {
        return this.getSystem().getSystemNo().concat(this.getSubsystemNo());
    }

    public String getSubsystemCode() {
        return this.subsystemCode;
    }

    public JiaSystem getSystem() {
        return this.system;
    }

    public String getSubsystemName() {
        return this.subsystemName;
    }

    public String getSubsystemNo() {
        return this.subsystemNo;
    }
}

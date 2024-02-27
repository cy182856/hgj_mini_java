package com.ej.hgj.enums;

public enum JiasvRespEnum {
    //    0000-0019  定义系统基本返回码
    SUCCESS("0000","成功"),
    SYS_ERROR("0001","系统异常"),
    RESULT_FAIL("0002","业务处理失败"),
    DATA_MISS("0003","数据缺失"),

    //    0020-9999 其他业务
    KEY_IS_NULL("0020","关键信息为空"),
    RESP_IS_NULL("0021","接口未响应"),


    //缴费0050-0100
    MONTH_TOO_LONG("0050", "查询月份超过12个月"),

    ;


    private String respCode;
    private String respDesc;

    JiasvRespEnum(String respCode, String respDesc) {
        this.respCode = respCode;
        this.respDesc = respDesc;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespDesc() {
        return respDesc;
    }

    public void setRespDesc(String respDesc) {
        this.respDesc = respDesc;
    }
}

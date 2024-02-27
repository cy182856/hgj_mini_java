package com.ej.hgj.enums;

public enum MonsterBasicRespCode {
    SUCCESS("000", "成功"),
    REQUEST_DATA_NOT_EXIST("070", "请求数据不存在"),
    REPEAT_TRADE("100", "重复交易"),
    RESULT_UNCERTAIN("101", "交易结果不确定"),
    RESULT_FAILED("102", "交易失败"),
    RESULT_PROCESSING("103", "交易处理中"),
    SESSION_INVALID("104", "会话超时"),
    SYSTEM_ERROR("151", "系统错误"),
    REQ_IP_ILLEGAL("152", "请求IP非法"),
    REQ_DATA_NULL("153", "请求参数包含空数据"),
    REQ_DATA_FORMAT_ERROR("154", "请求参数格式错误"),
    INFO_EXIST("155", "记录项已经存在"),
    INFO_NOT_EXIST("156", "记录项不存在"),
    ACCT_FRZ("200", "账户被冻结"),
    ACCT_CLOSE("201", "账户被关闭"),
    ACCT_NO_EXIST("242", "账户不存在"),
    ACCT_BAL_NOT_ENOUGH("251", "账户余额不足"),
    USER_EXIST("300", "用户已存在"),
    USER_CLOSE("301", "用户被关闭"),
    USER_CANCELED("302", "用户已销户"),
    USER_NO_EXIST("342", "会员不存在"),
    PWD_ERROR("355", "密码错误"),
    OPER_LOGIN_LOCKED("356", "用户登录被锁定"),
    TRAN_FORBIDDEN("457", "该交易被禁止"),
    WX_OPENID_FAILED("901", "微信号获取失败");

    private String returnCode;
    private String codeDesc;

    private MonsterBasicRespCode(String returnCode, String codeDesc) {
        this.returnCode = returnCode;
        this.codeDesc = codeDesc;
    }

    public String getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getCodeDesc() {
        return this.codeDesc;
    }

    public void setCodeDesc(String codeDesc) {
        this.codeDesc = codeDesc;
    }
}

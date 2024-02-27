/**
 * 
 * 上海慧管信息技术服务有限公司
 * Copyright (c) 2020 YunCF,Inc.All Rights Reserved.
 */
package com.ej.hgj.utils.exception;

/**
 * 
 * @author Scofield.hu
 * @version $Id: BusinessException.java, v 0.1 2020年8月20日 上午10:46:38 Scofield.hu Exp $
 */
public class BusinessException extends RuntimeException{

    /**  */
    private static final long serialVersionUID = 3331156912418350436L;

    private String respCode;
    private String errCode;
    private String errDesc;
    
    public BusinessException(String respCode, String errCode, String errDesc) {
        this.respCode = respCode;
        this.errCode = errCode;
        this.errDesc = errDesc;
    }
    public String getMessage() {
        return "respCode: " + respCode + ", errCode: " + errCode + ", errDesc: " + errDesc;
    }
    public String getRespCode() {
        return respCode;
    }
    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }
    public String getErrCode() {
        return errCode;
    }
    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
    public String getErrDesc() {
        return errDesc;
    }
    public void setErrDesc(String errDesc) {
        this.errDesc = errDesc;
    }
}

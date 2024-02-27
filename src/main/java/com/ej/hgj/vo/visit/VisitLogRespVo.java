package com.ej.hgj.vo.visit;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisitLogRespVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1274366450038116412L;

	private String visitDate;
	
	private String visitSeqId;
	
    private String custId;

    private String huSeqId;

    private String houseSeqId;

    private String visitName;

    private String carNum;

    private Short expCnt;
    
    private Short avlCnt;

    private String expTime;
    
    //二维码组成参数
    private String qrCodeParams;
    
    //条形码
    private String barCode;

    //验证码类型 Q-快速通行码  V-访客通行证
    private String visitType;
    
    private String visitCode;
    
}

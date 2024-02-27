package com.ej.hgj.result.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class DtAcctInfoDto implements Serializable{

	private static final long serialVersionUID = 2605890650093098868L;

    private String custId;

    private String subAcctId;
	
    private String capType;

    private String acctType;

    private String acctName;

    private String acctAlias;

    private String custInfo;

    private String acctStat;

    private BigDecimal avlBal;

    private BigDecimal frzBal;

    private BigDecimal acctBal;

    private String lastUpdDate;

    private BigDecimal lastAvlBal;

    private BigDecimal lastFrzBal;

    private BigDecimal todayAvlBal;

    private BigDecimal todayFrzBal;

    private String bdepId;

    private String saleId;

    private String bgroupId;

    private String openDate;

    private String openTime;

    private String level0Mag;

    private String level1Mag;

    private String level2Mag;

    private String busiInq1;

    private String busiInq2;

    private String refundAmtOnline;
    
    private String refundAmtOffline;
    
}

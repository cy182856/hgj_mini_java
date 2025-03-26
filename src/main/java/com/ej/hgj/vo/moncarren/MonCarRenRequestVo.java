package com.ej.hgj.vo.moncarren;

import com.ej.hgj.base.BaseReqVo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MonCarRenRequestVo extends BaseReqVo implements Serializable {

	private static final long serialVersionUID = 3377363860357666927L;

	private String orderId;

	private String carCode;

	private String cstCode;

	private String proNum;

	private String wxOpenId;

	private Integer orderStatus;

	private BigDecimal payAmount;

	private Integer monthNum;

	private String endTime;

	private String invoiceType;

	private String buyerName;

	private String buyerTaxNo;

	private String searchText;

	private String pushEmail;

}

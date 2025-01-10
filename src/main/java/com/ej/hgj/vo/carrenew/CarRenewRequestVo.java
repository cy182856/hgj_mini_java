package com.ej.hgj.vo.carrenew;

import com.ej.hgj.base.BaseReqVo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CarRenewRequestVo extends BaseReqVo implements Serializable {

	private static final long serialVersionUID = 3377363860357666927L;

	private String carCode;

	private String cstCode;

	private String proNum;

	private String wxOpenId;

	private Integer orderStatus;

	private BigDecimal payAmount;

	private Integer monthNum;

}

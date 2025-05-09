package com.ej.hgj.vo.carpay;

import com.ej.hgj.base.BaseReqVo;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CarPayRequestVo extends BaseReqVo implements Serializable {

	private static final long serialVersionUID = 3377363860357666927L;

	private String carCode;

	private String cstCode;

	private String proNum;

	private String wxOpenId;

	private String cardCstBatchId;

	private Boolean radioChecked;

	private Integer orderStatus;

	private Integer hourNumValue;

	private String orderId;

	// 车牌优惠参数
	private String couponNo;

}

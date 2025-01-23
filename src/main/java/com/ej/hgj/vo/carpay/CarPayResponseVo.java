package com.ej.hgj.vo.carpay;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.visit.VisitLog;
import com.ej.hgj.vo.bill.SignInfoVo;
import lombok.Data;

import java.util.List;

@Data
public class CarPayResponseVo extends BaseRespVo {

	private String carCode;

	private Boolean payFeeStatus;

	private CarInfoVo carInfoVo;

	private ParkCardVo parkCardVo;

	private String orderId;

	private SignInfoVo signInfoVo;

}

package com.ej.hgj.vo.carpay;

import com.ej.hgj.base.BaseRespVo;
import lombok.Data;

@Data
public class CarPayResponseVo extends BaseRespVo {

	private String carCode;

	private Boolean payFeeStatus;

	private CarInfoVo carInfoVo;

	private StopCouponVo stopCouponVo;

}

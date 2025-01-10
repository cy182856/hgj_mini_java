package com.ej.hgj.vo.carrenew;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.CarInfoVo;
import com.ej.hgj.vo.carpay.ParkCardVo;
import lombok.Data;

@Data
public class CarRenewResponseVo extends BaseRespVo {

	private String carCode;

	private CarRenewInfoVo carRenewInfoVo;

	private String orderId;

	private SignInfoVo signInfoVo;


}

package com.ej.hgj.vo.moncarren;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import lombok.Data;

@Data
public class MonCarRenResponseVo extends BaseRespVo {

	private String carCode;

	private MonCarRenInfoVo monCarRenInfoVo;

	private String orderId;

	private SignInfoVo signInfoVo;


}

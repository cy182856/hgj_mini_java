package com.ej.hgj.vo.card;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.coupon.CouponGrant;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import lombok.Data;

import java.util.List;

@Data
public class CardResponseVo extends BaseRespVo {

	private String cardCstId;

	private String cardName;

	private String cardCode;

	private Integer cardExpNum;

	private String startTime;

	private String endTime;

}

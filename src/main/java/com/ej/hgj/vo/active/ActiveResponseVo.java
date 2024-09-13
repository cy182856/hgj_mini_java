package com.ej.hgj.vo.active;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.BillYear;
import com.ej.hgj.entity.coupon.CouponGrant;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.vo.bill.SignInfoVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActiveResponseVo extends BaseRespVo {

	private Integer pageNum;//页码数
	private Integer pageSize;//页码大小
	private Integer totalNum;//总记录数
	private Integer pages;//总页数

	// 客户名称
	private String cstName;
	// 券总次数
	private Integer couponTotalNum;
	// 扫码进入记录
	private List<OpenDoorLog> openDoorLogList;
	// 券明细
	private List<CouponGrant> couponGrantList;



}

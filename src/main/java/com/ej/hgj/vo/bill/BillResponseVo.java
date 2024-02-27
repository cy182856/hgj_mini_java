package com.ej.hgj.vo.bill;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.bill.Bill;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.bill.BillYear;
import com.ej.hgj.entity.repair.RepairLog;
import com.ej.hgj.entity.workord.Material;
import com.ej.hgj.vo.repair.RepairDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillResponseVo extends BaseRespVo {

	private Integer pageNum;//页码数
	private Integer pageSize;//页码大小
	private Integer totalNum;//总记录数
	private Integer pages;//总页数

	private List<Bill> billList;

	private List<BillYear> menuTree;

	private List<BillMerge> billMergeList;

	private SignInfoVo signInfoVo;

	// 应收金额
	private BigDecimal priRevAmount;

	// 月账单详细头部显示-月
	private String month;

	// 月账单详细头部显示-年月
	private String yearMonth;

	// 月账单详细头部显示-账单总金额
	private BigDecimal billMonthAmountTotal;

	// 月账单详细头部显示-已缴金额
	private BigDecimal billMonthAmountPaidIn;

}

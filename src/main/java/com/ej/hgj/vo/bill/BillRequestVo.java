package com.ej.hgj.vo.bill;

import com.ej.hgj.entity.bill.Bill;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BillRequestVo implements Serializable {

	private static final long serialVersionUID = 3377363860357666927L;

	private Integer pageNum = 1;//页数
	private Integer pageSize = 10;//页大小

	private String id;

	private String[] orderIds;

	private String cstId;

	private String cstCode;

	private String proNum;

	private String wxOpenId;

	private String lockLogo;

	// 所属账期
	private String repYears;

	// 本金应收
	private BigDecimal priRev;

	// 本金已收
	private BigDecimal priPaid;

	// 收付项目
	private String ipItemName;

	private BigDecimal totalAmount;

	// 账单ID集合
	private List<String> billIds;

	// 缴费状态
	private Integer billStatus;

	// 账单状态集合
	List<Integer> billStatusList;

	// 思源账单ID集合
	List<String> billOrderNoList;

	// 房屋ID集合
	List<String> houseIdList;

	// 被选中账单
	List<Bill> checkData;

	// 费用类型名称集合
	List<String> billItemNameList;

}

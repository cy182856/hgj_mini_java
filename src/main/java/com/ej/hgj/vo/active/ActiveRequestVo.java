package com.ej.hgj.vo.active;

import com.ej.hgj.entity.bill.Bill;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ActiveRequestVo implements Serializable {

	private static final long serialVersionUID = 3377363860357666927L;

	private Integer pageNum = 1;//页数
	private Integer pageSize = 10;//页大小

	private String cstCode;

	private String proNum;

	private String proName;

	private String wxOpenId;

	private Integer couponType;

	private String couponId;

	private Integer status;

}

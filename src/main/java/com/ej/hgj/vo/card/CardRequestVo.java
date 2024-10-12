package com.ej.hgj.vo.card;

import lombok.Data;

import java.io.Serializable;

@Data
public class CardRequestVo implements Serializable {

	private static final long serialVersionUID = 3377363860357666927L;

	private Integer pageNum = 1;//页数
	private Integer pageSize = 10;//页大小

	private String cstCode;

	private String proNum;

	private String proName;

	private String wxOpenId;

	private String cardCstId;


}

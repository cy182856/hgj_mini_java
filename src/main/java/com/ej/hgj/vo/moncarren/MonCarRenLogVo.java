package com.ej.hgj.vo.moncarren;

import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.moncarren.MonCarRenOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MonCarRenLogVo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1071066153864116677L;

	private String cstCode;

	private String wxOpenId;

    private String proNum;

    private String proName;

    private Integer pageNum = 1;//页数
    private Integer pageSize = 10;//页大小
    private Integer totalNum;//总记录数
    private Integer pages;//总页数
    private List<MonCarRenOrder> list;
    private String respCode;

}

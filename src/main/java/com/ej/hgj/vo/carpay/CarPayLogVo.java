package com.ej.hgj.vo.carpay;

import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.visit.VisitLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CarPayLogVo implements Serializable{

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
    private List<ParkPayOrder> list;
    private String respCode;

}

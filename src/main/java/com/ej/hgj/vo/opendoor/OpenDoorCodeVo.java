package com.ej.hgj.vo.opendoor;

import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.entity.opendoor.OpenDoorQuickCode;
import com.ej.hgj.entity.visit.VisitLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class OpenDoorCodeVo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1071066153864116677L;

	private String cstCode;

	private String wxOpenId;

    private String proNum;

    private String proName;

    private String expDate;

    private String houseId;

    private String couponId;

    private Integer pageNum = 1;//页数
    private Integer pageSize = 10;//页大小
    private Integer totalNum;//总记录数
    private Integer pages;//总页数
    private List<OpenDoorLog> list;
    private List<OpenDoorQuickCode> quickCodeList;
    private String respCode;

    private String visitName;

}

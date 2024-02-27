package com.ej.hgj.vo.visit;

import com.ej.hgj.entity.visit.VisitLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class VisitLogVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1071066153864116677L;
	
	private String cstCode;
	
	private String wxOpenId;
	
    private String proNum;

    private String proName;

    private String visitName;

    private String houseId;

    private String carNum;

    private Integer expCnt;

    // 验证码类型 Q-快速通行码  V-访客通行证
    private String visitType;

    private String houseName;

    // 失效日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expDate;

    // 生效日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date effectuateDate;

    private Integer pageNum = 1;//页数
    private Integer pageSize = 10;//页大小
    private Integer totalNum;//总记录数
    private Integer pages;//总页数
    private List<VisitLog> list;
    private String respCode;

}

package com.ej.hgj.vo.electricity;

import com.ej.hgj.entity.electricity.Electricity;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.entity.opendoor.OpenDoorQuickCode;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ElectricityVo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1071066153864116677L;

	private String cstCode;

	private String wxOpenId;

    private String proNum;

    private String proName;

    private List<Electricity> list;

    private String respCode;

	private String errDesc;

	private String totalUsedPower;

	private String roomId;

	private String startDate;

	private String endDate;


}

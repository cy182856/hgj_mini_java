package com.ej.hgj.vo.hu;

import lombok.Data;

@Data
public class PropHouseInfoVo {

    private String custId;

    private String areaId;

    private String buildingId;

    private String houseNo;

    private String stat;

    private Integer pageNum;

    private Integer pageSize;

    private String houseSeqId;
    private String isForTest;
    private String isNeedFollow;
    private String houseRemark;

}

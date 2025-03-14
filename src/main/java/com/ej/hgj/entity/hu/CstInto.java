package com.ej.hgj.entity.hu;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class CstInto {
    private String id;

    private String projectNum;

    private String unionId;

    private String wxOpenId;

    private String userName;

    private String phone;

    private String cstCode;

    private String houseId;

    private Integer intoRole;

    private Integer intoStatus;

    private String createBy;

    private String updateBy;

    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    private String cstIntoHouseId;

    private Integer houseIntoStatus;

    private String intoRoleName;

    private Boolean swimCardChecked;

    private Boolean parkCardChecked;
}

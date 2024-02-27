package com.ej.hgj.vo.repair;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.repair.RepairLog;
import com.ej.hgj.entity.workord.Material;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RepairResponseVo extends BaseRespVo {
	private String shortName;//企业信息表（USR_INFO）企业简称(SHORT_NAME)
	private String usrName;//企业信息表（USR_INFO）企业全称(USR_NAME)
	private String ewCorpId;//企业信息表（USR_INFO）企业微信id(EW_CORP_ID)
	private String pubOrgId;//企业信息表（USR_INFO）公众号原始ID(PUB_ORG_ID)
	private String pubAppId;//企业信息表（USR_INFO）公众号开发者ID(PUB_APP_ID)
	private String uiPoSeqId;//企业信息表（USR_INFO）物管人员序号(PO_SEQ_ID)
	private String magMp;//企业信息表（USR_INFO）管理员手机(MAG_MP)
	private String magEmail;//企业信息表（USR_INFO）管理员邮箱(MAG_EMAIL)
	private String magTel;//企业信息表（USR_INFO）管理员电话(MAG_TEL)
	private String magName;//企业信息表（USR_INFO）管理员姓名(MAG_NAME)
	private String urgentMp;//企业信息表（USR_INFO）紧急联系人手机(URGENT_MP)
	private String expDate;//企业配置表（USR_CONF）有效日期(EXP_DATE)
	private String propType;//企业配置表（USR_CONF）物业类型(PROP_TYPE)
	private String repairWorkTime;//企业配置表（USR_CONF）维修人员工作时间(REPAIR_WORK_TIME)
	private String urgentTel;//企业配置表（USR_CONF）紧急联系电话(URGENT_TEL)
	private String repairAssign;//企业配置表（USR_CONF）报修分配方式(REPAIR_ASSIGN)
	private String repairTimeCnt;//企业配置表（USR_CONF）更改到场次数(REPAIR_TIME_CNT)
	private String repairDate;//报修日志表新增接口新增成功时返回
	private String repairSeqId;//报修日志表新增接口新增成功时返回
	private Integer pageNum;//页码数
	private Integer pageSize;//页码大小
	private Integer totalNum;//总记录数
	private Integer pages;//总页数
	private List<RepairDto> repairDtos;//报修日志表（REPAIR_LOG）及报修留言明细表（REPAIR_MSG_DTL）部分查询结果

	private List<RepairLog> repairLogList;

	private String[] fileList;

	private BigDecimal labourCost;

	private BigDecimal materialCost;

	private List<Material> materialList;
}

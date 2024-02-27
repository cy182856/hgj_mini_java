package com.ej.hgj.entity.login;

import lombok.Data;

/**
 * 
 * @author lx
 * @version $Id: WechatPubConfExtDo.java, v 0.1 2021-1-21 下午12:48:38 lx Exp $
 */
@Data
public class WechatPubConfExtDo {
	//2.2.4微信公众号配置表（WECHAT_PUB_CONF）
	private String pubOrgId;//公众号原始ID.如鼎刷：gh_36bebcefccd9
	private String pubAppId;//公众号开发者ID.如：wx25d81ed3986353e4
	private String custId;//客户号.多小区存放主小区
	private String appSecret;//公众号开发者密码
	private String accessToken;//公众号调用凭据.定时任务每小时刷新一次
	private String stat;//状态.‘N’ – 正常,‘C’ – 被关闭
	private String updTime;//更新时间
	private String singleOrMutl;//单小区或多小区.公众号下挂单个小区或多个小区.‘S’ – 单小区,‘M’ – 多小区
	//2.1.1企业信息表（USR_INFO）
	private String shortName;//企业简称.社区简称
	private String usrName;//企业全称.营业执照全称
	private String ewCorpId;//企业微信id.企业微信绑定信息
	private String usrStat;//用户状态.‘N’ – 正常,‘C’ – 被关闭，所有操作员不能登陆
	private String usrProv;//所在省份代号.参照prov_Area表
	private String usrArea;//所在城市代号.
	private String openDate;//开户日期.
	private String openTime;//开户时间.
	private String branchId;//分支机构.内部管理需要，如，总部->销售一部->陈建国
	private String bdepId;//部门
	private String saleId;//销售
	private String poSeqId;//物管人员序号.企业入驻后，需要设定一个管理员
	private String magMp;//管理员手机.管理员，也就是负责人，也就是创建者，是平台和企业的联系人
	private String magEmail;//管理员邮箱
	private String magTel;//管理员电话
	private String magName;//管理员姓名
	private String urgentMp;//紧急联系人手机.和USR_MP不能相同
	private String loginAction;//登录后动作.全局管理员登录后需要采取的动作
	private String updDatetime;//修改时间
	//其他查询条件
	private Integer pageNum;//页数（查询第几页）
	private Integer pageSize;//每页记录数
	private int count;//总记录数
	private Integer startIndex;//开始查询的索引

}

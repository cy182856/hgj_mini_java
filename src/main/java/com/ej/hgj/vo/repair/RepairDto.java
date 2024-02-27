package com.ej.hgj.vo.repair;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RepairDto implements Serializable {
	private static final long serialVersionUID = 5949818969472227451L;
	//3.6.1	报修日志表（REPAIR_LOG）字段
	private String repairDate;//报修日期
	private String repairSeqId;//报修单号
	private String custId;//企业客户号
	private String huSeqId;//住户序列号
	private String houseSeqId;//住房序列号
	private String buildingId;//楼号
	private String areaId;//区域ID
	private String repairType;//报修类型
	private String repairDesc;//报修简要描述
	private String imgCnt;//报修日志表（REPAIR_LOG）图片数量
	private String poSeqId;//报修日志表（REPAIR_LOG）物管人员序号,维修人员
	private String procStat;//处理状态
	private String arvTimeCnt;//更改到场时间次数.到场时间可多次更改
	private String expArvTime;//期望到场时间区间.格式：YYYYDDHHMMHHMM
	private String unkArvTime;//不确定的到场时间.格式：YYYYMMDDHHMMHHMM.协商到场时间，保存在该字段，双方达成一致后，更新到上一个字段
	private String recvSpan;//接单时间段.分钟单位，接单时间-报修申请时间
	private String arvTime;//到场时间点.实际到场的时间
	private String arvSpan;//到场时间段.分钟单位，实际到场时间-期望到场时间
	private String doneDate;//维修完成日期.用于系统自动确认
	private String repairTime;//维修时间.分钟单位，维修完成时间-实际到场时间
	private String rateScore;//评价分数。业主评价，1分到5分，-1表示未评价。对公维修的评价仅针对事件，不针对个人
	private String huScore;//对公报修物管奖励业主分，-1表示还未奖励得分，0表示未奖励，>0表示奖励分值
	private String payType;//收费方式
	private String ordAmt;//订单金额
	private String payStat;//支付状态
	private String payDate;//支付日期
	private String paySeqId;//支付序列号
	private String requestTime;//请求时间.报修请求时间
	private String isLogin;//是否登录.‘Y’ – 是，登录用户,‘N’ – 否，未登录，匿名用户
	private String wxOpenId;//微信openId.微信公众号的绑定微信openId
	private String repairObjId;//维修标的代号.具体的维修标的，如电梯，门禁，19号楼
	private String itemRepType;//维修项报修类型。‘S’ – 私人报修；‘P’ – 公共报修
	private String itemBtype;//维修项大类.由维修接单人员分类；事后出统计报表
	private String itemStype;//维修项小类.由维修接单人员分类；事后出统计报表
	private String isRepairTime;//报修时是否显示期望到场时间。冗余到该表
	private String repairDateDesc;//报修日期描述
	private String repairTypeDesc;//报修类型描述
	private String repairDescAbbr;//报修简要描述缩略
	private String procStatDesc;//处理状态描述
	private String expArvTimeDesc;//期望到场时间区间描述
	private String payTypeDesc;//收费方式描述
	private String payStatDesc;//支付状态描述
	private String payDateDesc;//支付日期描述
	private String itemBtypeDesc;//维修项大类描述
	private String itemStypeDesc;//维修项小类描述
	//3.6.2	报修留言明细表（REPAIR_MSG_DTL）字段
	private String dtlId;//维修明细。每个维修单从0001开始编号
	private String dtlDateTime;//明细时间
	private String msgSource;//留言来源。‘S’ -- 系统,‘P’ -- 物管人员,‘H’ -- 房屋住户人员
	private String replyStat;//回复状态。部分留言需要对方回复确认：‘N’ -- 无需回复；‘I’ -- 未回复；‘C’ -- 已回复
	private String replyType;//回复类型。需要回复的类型：‘01’,’02’ -- 期望到场时间协商请求和确认；‘03’,’04’  -- 到场请求和确认；‘05’,’06’  -- 维修完成请求和确认；
	private String rmdImgCnt;//报修留言明细表（REPAIR_MSG_DTL）图片数量。最多不超过3张
	private String msgBody;//消息内容
	private String repairTimeDesc;//报修时间描述
	private String msgSourceDesc;//留言来源描述
	private String replyStatDesc;//回复状态描述
	private String replyTypeDesc;//回复类型描述
	//3.5.1	住房信息表（HOUSE_INFO）字段
	private String houseNo;//门牌号
	private String buildingName;//楼名称
	private String areaName;//区域名称
	private String hiStat;//状态。‘I’ – 初始，住房信息导入,‘P’ – 已认领(pack)，未认证,‘N’ – 正常,‘C’ – 被关闭
	private String ownerSeqId;//户主序列号.每套房子仅能有唯一的户主
	private String propFee;//物业费.每月的物业费
	private String poKeeperSeq;//物业管家.每个房子对应的物业管家
	private String scoreSum;//得分总计
	private String isParkSpace;//是否拥有停车位.‘Y’ – 有停车位；‘N’ – 没有；‘U’ – 未知
	private String setBitmap;//设置位图.户主给某些房屋的设置
	private String authMaskBm;//权限屏蔽位图.对特殊房屋屏蔽某些功能权限，1代表打开,详见USR_CONF.MODULE_BITMAP
	private String pfeePayMon;//缴清月份.截止该月（含本月）之前的物业费已缴清
	private String packCode;//房屋认领码.认领码和房号批量一起导入
	private String houseDesc;//报修地址。区域名称楼名称门牌号，如东区1号楼1号楼201
	//3.5.2	房屋住户信息表（HOUSE_USR_INFO）字段
	private String nickName;//昵称。住户的昵称，初始通过小程序从微信导入
	private String huRole;//住户角色。‘U’ -- 未知，在认领模式下，未认证之前,‘O’ -- 户主(owner )，由物管人员创建,‘M’ -- 成员( member)，由户主创建,‘T’ -- 房客（tenant），由户主创建。对于商业地产，角色分为管理员和文员
	private String huiStat;//状态。‘I’ – 初始，未认领,‘P’ – 已认领，未认证(pack),‘N’ – 正常,‘C’ – 临时关闭，可以再次打开,‘D’ – 销户，微信绑定清空
	private String huiWxOpenId;//公众号。微信公众号的绑定微信openId
	private String huiHgjOpenId;//慧管家openId。慧管家小程序绑定的微信openId
	private String headImgUrl;//头像URL。保存小程序获取的头像链接
	private String authBitmap;//权限位图。户主给成员或房客开放的权限位图，可选模块根据企业需要是否选配，没有配置的模块，业主小程序不显示。第1位，预约；第2位，缴费；第3位，邻里圈；第4位，问卷调查；第5位，水电煤抄表；第6位，业主码；第7位，访客管理；第8位，店铺收款；
	private String notifyBitmap;//通知位图。户主给住户的通知设置。
	private String huiOpenDate;//开户日期。
	private String isQueryOther;//是否可查阅他人事务进度。对于报修事务，能否查询到其他成员的处理进度。‘Y’ – 能查询；‘N’ – 不能查询。
	//3.4.1	物管人员信息表（PROP_OPER_INFO）字段
	private String poName;//物管人员名称
	private String deptName;//部门名称
	private String poiStat;//状态。‘N’ – 正常,‘C’ – 被关闭
	private String poWxId;//物管微信号
	private String poMp;//物管手机号
	private String isInfoPub;//是否可信息公开
	private String poiHgjOpenId;//慧管家openId
	private String poiRateScore;//物管人员信息表（PROP_OPER_INFO）评价总分数
	private String rateCnt;//评价总次数
	private String poiOpenDate;//开户日期。
	private String avgPoScore;//物管人员评价平均分
	//2.3.6	物业标的列表（PROP_OBJ_LIST）
	private String objName;//标的名称。
	private String objType;//标的类型。‘01’ – 重要公共设备,‘02’ – 一般公共设备,‘09’ – 楼宇位置
	private String polStat;//状态。‘N’ – 正常,‘C’ – 关闭
	private String isCheck;//是否需要巡检。‘Y’ – 是,‘N’ – 否
	private String repairCnt;//报修总次数。用于汇总统计
	private String wxQrCode;//微信二维码。调用微信生成的二维码
	private String objTypeDesc;//标的描述。
	/**
	 * 报修详情的图片
	 */
	private List<ImgDto> detailUrlList;
	/**
	 * 留言中到场的图片
	 */
	private List<ImgDto> msgOneUrlList;
	/**
	 * 留言中报修完成的图片
	 */
	private List<ImgDto> msgTwoUrlList;

}

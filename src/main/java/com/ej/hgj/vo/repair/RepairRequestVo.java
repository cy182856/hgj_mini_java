package com.ej.hgj.vo.repair;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class RepairRequestVo implements Serializable {
	private static final long serialVersionUID = 1548330339323601219L;
	private String repairDate;//报修日期.如需在线缴费时，作为支付模块的订单日期和订单流水号
	private String repairSeqId;//报修单号.如需在线缴费时，作为支付模块的订单日期和订单流水号
	private String custId;//企业客户号
	private String huSeqId;//住户序列号
	private String houseSeqId;//住房序列号
	private String buildingId;//楼号
	private String areaId;//区域ID
	private String repairType;//报修类型.‘S’ – 私人报修,‘P’ – 公共报修
	private String repairDesc;//报修简要描述
	private Integer imgCnt;//图片数量
	private String poSeqId;//物管人员序号
	private String procStat;//处理状态.‘01’ – 维修申请中（待分配）,‘03’ – 维修受理（暂不实现）,‘05’ – 维修中,‘07’ – 物管维修完成,‘09’ – 住户维修确认完成,‘11’ – 业主评价完成
	private String expArvTime;//期望到场时间区间.格式：YYYYMMDDHHMMHHMM
	private Integer rateScore;//评价分数。业主评价，1分到5分，-1表示未评价。对公维修的评价仅针对事件，不针对个人
	private String huScore;//业主得分。对公维修，业主得分，-1表示未得分
	private String payType;//收费方式.‘N’ – 无费用,‘C’ – 现金,‘O’ – 线上缴费
	private String ordAmt;//订单金额
	private String payStat;//支付状态.‘N’ – 无需费用,‘I’ – 初始，需要付费，待支付,‘S’ – 付费完成,‘F’ – 支付失败
	private String payDate;//支付日期.由支付模块生成
	private String paySeqId;//支付序列号.由支付模块生成
	private String wxOpenId;//微信openId。未登录用户不能为空,登录用户为空
	private String repairObjId;//维修标的代号。具体的维修标的，如电梯，门禁，19号楼
	private String itemBtype;//维修项大类。由维修接单人员分类；事后出统计报表


	/**
		维修项小类	Char(2)	Default ‘ ’	由维修接单人员分类；事后出统计报表
		电：‘0101’ -- 电灯；‘0111’ -- 电线；‘0121’ -- 开关；‘0131’ -- 配电箱；
		水：‘0201’ -- 水龙头；‘0211’ -- 水管；‘0221’ -- 漏水；
		煤：‘030’ -- 煤气管；‘031’ -- 煤气阀；‘0321’ -- 煤气灶；
		投诉：‘0301’ -- 漏水；‘031’ -- 窗户；‘0321’ -- ；
		房屋：‘0301’ -- 漏水；‘031’ -- 窗户；‘0321’ -- ；
		消防：‘0401’ -- 消防栓；‘0411’ -- 烟气阀；‘0421’ -- 其它消防；
		公共：‘1001’ -- 电梯；‘1011’ -- 楼道灯；‘1021’ -- 路灯；‘1011’ -- 绿化；‘1021’ -- 道路；‘1031’ -- 墙面；‘1041’ -- 马桶；‘1051’ -- 盥洗盆；
	 */
	private String itemStype;
	private Integer pageNum = 1;//页数
	private Integer pageSize = 10;//页大小
	private String startRepairDate;//起始报修日期
	private String endRepairDate;//结束报修日期
	private String msgBody;//消息内容
	private String replyStat;//留言的回复状态
	private String unkArvTime;//不确定到场时间
	private String dtlId;//留言ID
	private String replyType;//回复类型
	private String msgBusiType;//消息推送类型


	private String fileList[];

	private String cstCode;

	private String houseId;

	private String proNum;

	private String repairNum;

	private String repairStatus;

	private String repairScore;

	private String repairMsg;

	private String satisFaction;

}

package com.ej.hgj.entity.user;

import lombok.Data;

/**
 * 2.1.2企业配置表（USR_CONF）。企业基本信息
 * @author lx
 * @version $Id: UsrConfDO.java, v 0.1 2021-6-1 下午4:12:41 lx Exp $
 */
@Data
public class UsrConfDO {
    /**
     * 客户号。组成：’3049’+5位序列号+1位校验
     */
    private String custId;
    /**
     * 物业类型。‘R’ — 住宅（residence），‘B’ — 商业（business）
     */
    private String propType;
    /**
     * 有效日期。如果超过有效期，则不能使用，引导付费
     */
    private String expDate;
    /**
     * 房屋数。小区最多支持的住房数
     */
    private Integer houseCnt;
    /**
     * 最大物管人员数量。小区最多支持的物管人员数
     */
    private Short maxPoCnt;
    /**
     * 最大住户数量。每户最多住户(house user)数
     */
    private Short maxHuCnt;
    /**
     * 模块位图。可选模块根据企业需要是否选配，没有配置的模块，业主小程序不显示
     * ‘0’代表不开通；’1’代表开通；’2’代表房屋为认领状态时功能受限，无法使用，房屋状态正常时，功能可用；
     * 第1位，预约；第2位，缴费；第3位，邻里圈；第4位，问卷调查；第5位，水电煤抄表；第6位，店铺收款；第7位，访客管理；第8位，天气订阅；第9位，打招呼；
     * 第10位，建议和投诉；第11位，生活服务；
     * 第12位，车辆缴费；第13位，文件公示；
     */
    private String moduleBitmap;
    /**
     * 住户开通模式。住户开通账户模式：‘P’ – 认领(pack)模式，认领后，房屋状态为认领状态，功能受限，线下手工认证才能完全使用
     * ‘O’ – 认领后房屋即正常可通，无需线下手工认证；
     * ‘H’ – 手工开通，仅支持线下手工认证开通
     */
    private String huOpenMode;
    /**
     * 是否需要认领码。P和O开通模式下，认领是否需要认领码。认领码和房号批量一起导入。‘Y’ – 是，认领时需要验证认领码，‘N’ – 否，无需输入认领码，直接认领
     */
    private String isPackCode;
    /**
     * 维修人员工作时间。展示在维修界面。维修人员的上下班时间，格式为：HHMMHHMM
     */
    private String repairWorkTime;
    /**
     * 工作时间联系电话
     */
    private String wkTimeTel;
    /**
     * 紧急联系电话，即非工作时间联系电话；应用要判断2个字段为必填项
     */
    private String urgentTel;
    /**
     * 报修分配方式。住户报修时，维修人员的分配方式：‘P’ – 认领模式，维修人员主动认领，‘A’ – 由分配员分配
     */
    private String repairAssign;
    /**
     * 更改到场次数。维修时间可以再次更改，最多更改的次数，<=该值
     */
    private Integer repairTimeCnt;
    /**
     * 审核位图。部分功能是否需要另外人员审核
     * 第1位，邻里圈发布；
     */
    private String auditBitmap;
    /**
     * 首页展示图数量。业主小程序首页的展示图数量，-1表示采用平台默认的展示图
     */
    private Integer homeImgCnt;
    /**
     * 微信公众号问候语。住户关注公众号时的问候语
     */
    private String wxPubGreet;
    /**
     * 修改时间
     */
    private String updDatetime;
    /**
     * 认领页面提示。认领页面的提示信息
     */
    private String packPageTip;
    /**
     * 登录失败描述。如果登录失败，增加失败报错提示
     */
    private String loginErrDesc;
    /**
     * 快捷面板模块位图。双重作用：决定快捷面板主界面功能模块、决定建议和投诉的细分类别
     * 1-8位图分别为，建议、投诉、表扬、私人报修、公共报修、缴费、便民信息、预约
     */
    private String qpadBitmap;
    /**
     * 提前交纳物业费月。业主可以提前预缴纳几个月物业费
     */
    private Integer advPfeeMon;
    /**
     * 物业费账单开始月份。仅能查询该月（含本月）后的物业费账单
     */
    private String pfeeStartMon;
    /**
     * 报修时是否显示期望到场时间。住户报修时，是否显示期望时间：‘Y’ – 是，报修界面上显示期望时间，‘N’ – 否，报修界面上不显示期望时间
     */
    private String isRepairTime;
    /**
     * 报修类别ID。由平台配置，为了配置的方便，可以引用其它小区的报修分类配置
     */
    private String repairBtypeId;
    /**
     * 提前交纳车辆费月。业主可以提前预缴纳几个月停车费
     */
    private String advCfeeMon;
    /**
     * 物业费缴纳周期。物业费缴纳是按月还是按季度：‘M’ – 按月缴费；‘Q’ – 按季度缴费，‘H’ – 按半年缴费；‘Y’ – 按年度缴费
     */
    private String pfeePayCyc;
    /**
     * 物业费是否可跨缴。物业费缴纳是否可以跨月缴纳：‘Y’ – 是；‘N’ – 否；
     */
    private String isPfeeSpan;
    /**
     * 物业费备注。物业费缴费说明备注
     */
    private String pfeeRemark;
    /**
     * 邻里圈的业主留言是否放开：‘Y’ – 是，所有业主可以看到留言；‘N’ – 否，仅贴主能看到留言
     */
    private String heoMsgOpen;
    /**
     * 物业费账单截止月份：仅能缴费该月（含本月）之前的物业费账单
     */
    private String pfeeEndMon;

}

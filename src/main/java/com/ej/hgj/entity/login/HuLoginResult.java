package com.ej.hgj.entity.login;

import com.ej.hgj.result.base.BaseResult;
import com.ej.hgj.result.dto.LoginDto;
import lombok.Data;

import java.util.List;

@Data
public class HuLoginResult extends BaseResult {
    /**  */
    private static final long serialVersionUID = 4127496591611320509L;
    /**
     * 企业客户号
     */
    private String custId;
    /**
     * 慧管小程序用户的openid
     */
    private String hgjOpenId;
    /**
     * 住户序列号
     */
    private String huSeqId;
    /**
     * 住房序列号
     */
    private String houseSeqId;
    /**
     * 微信序号
     */
    private String wxSeqId;

    /**
     * 权限位图
     */
    private String authBitMap;
    /**
     * 楼号
     */
    private String buildingId;
    private String buildingName;
    /**
     * 区域
     */
    private String areaId;
    private String areaName;
    /**
     * 企业简称
     */
    private String commanyShortName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 头像
     */
    private String headImgUrl;
    /**
     * 房屋认领状态
     */
    private String houseStat;
    /**
     * 得分总计
     */
    private Integer scoreSum;

    /**
     * 门牌号
     */
    private String houseNo;
    /**
     * 维修人员工作时间
     */
    private String repairWorkTime;
    /**
     * 紧急联系电话
     */
    private String urgentTel;
    /**
     * 住户角色
     */
    private String huRole;
    /**
     * 修改预期到场次数
     */
    private String repairTimeCnt;

    private String moduleBitmap;

    /**
     * 物业类型 R：住宅，B：商业
     */
    private String propType;
    /**
     * 房屋住户状态
     */
    private String huStat;
    /**
     * 工作时间电话
     */
    private String wkTimeTel;
    /**
     * 登录报错信息描述
     */
    private String loginErrDesc;
    /**
     * 管家序列号
     */
    private String poWxId;
    /**
     * 管家电话
     */
    private String poMp;
    /**
     * 管家信息是否公开
     */
    private String isInfoPub;

    private String pubOrgId;

    private String qpadBitmap;
    private String isRepairTime;

    private String advCfeeMon;

    /**
     * 多用户数据
     */
    private List<LoginDto> huLoginResultList;











}

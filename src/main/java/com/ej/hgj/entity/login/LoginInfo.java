package com.ej.hgj.entity.login;

import com.alibaba.fastjson.JSONArray;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.entity.menu.mini.MenuMini;
import lombok.Data;

import java.util.List;

@Data
public class LoginInfo extends BaseRespVo {
    private String cstCode;
    private String wxOpenId;
    private String proNum;
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
     * sessionId
     */
    private String sessionId;
    /**
     * 菜单功能
     */
    //private JSONArray funList;

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
     * 主页的图片张数
     */
    private String imgNum = "1";//首页的图片张数,默认是1张

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

    private String appId;

    private String code;
    /**
     * 预期时间修改次数
     */
    private String repairTimeCnt;
    
    /**
     * 物业类型 R：住宅，B：商业
     */
    private String propType;
    
    /**
     * 功能模块位图
     */
    private String moduleBitmap;
    /**
     * 房屋住户状态
     */
    private String huStat;
    /**
     * 登录失败的错误描述语
     */
    private String loginErrDesc;
    /**
     * 工作时间电话
     */
    private String wkTimeTel;
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
    /**
     * 完整地址
     */
    private String completeAddr;
    /**
     * 隐藏地址
     */
    private String hideAddr;
    private String pubOrgId;
    /**
     * 快捷面板的位图数据集
     */
    private JSONArray qpadBitmapList;

    private String isRepairTime;
    
    /**
     * 提前缴纳车辆费用月份
     */
    private String advCfeeMon;

    /**
     * 住户角色
     */
    private String huRole;

    /**
     * 模版消息功能
     */
    private JSONArray ModuleValues;


    private List<MenuMini> funList;


}

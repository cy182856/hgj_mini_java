package com.ej.hgj.entity.ext;

import com.ej.hgj.entity.hu.HuHgjBindDo;

/**
 * @author tty
 * @version 1.0 2020-08-27 18:20
 */
public class HuHgjBindExtDo extends HuHgjBindDo {
    /**
     * 权限位图
     */
    private String authBitmap;
    /**
     * 通知位图
     */
    private String notifyBitmap;
    /**
     * 楼号
     */
    private String buildingId;
    /**
     * 楼名称
     */
    private String buildingName;
    /**
     * 区域ID
     */
    private String areaId;
    /**
     * 区域名
     */
    private String areaName;
    /**
     * 门牌号
     */
    private String houseNo;
    /**
     * 头像
     */
    private String headImgUrl;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 房屋认领状态
     */
    private String houseStat;
    /**
     * 得分总计
     */
    private Integer scoreSum;
    /**
     * 住户角色
     */
    private String huRole;
    /**
     * 企业简称
     */
    private String commanyShortName;
    /**
     * 功能权限位图
     */
    private String moduleBitmap;
    /**
     * 工作维修时间
     */
    private String repairWorkTime;
    /**
     * 紧急联系电话
     */
    private String urgentTel;
    /**
     * 预期时间修改次数
     */
    private String repairTimeCnt;
    /**
     * 物业类型 R：住宅，B：商业
     */
    private String propType;
    /**
     * 房屋住户状态
     */
    private String huStat;
    /**
     * 权限屏蔽位图
     */
    private String authMaskBm;
    /**
     * 企业状态
     */
    private String usrStat;
    /**
     * 工作时间电话
     */
    private String wkTimeTel;
    /**
     * 管家序列号
     */
    private String poKeeperSeq;

    private String loginErrDesc;

    private String pubOrgId;
    /**
     * 快捷模板功能的位图
     */
    private String qpadBitmap;

    private String isRepairTime;
    
    private String advCfeeMon;

    public String getIsRepairTime() {
        return isRepairTime;
    }

    public void setIsRepairTime(String isRepairTime) {
        this.isRepairTime = isRepairTime;
    }

    public String getQpadBitmap() {
        return qpadBitmap;
    }

    public void setQpadBitmap(String qpadBitmap) {
        this.qpadBitmap = qpadBitmap;
    }

    public String getPubOrgId() {
        return pubOrgId;
    }

    public void setPubOrgId(String pubOrgId) {
        this.pubOrgId = pubOrgId;
    }

    public String getPoKeeperSeq() {
        return poKeeperSeq;
    }

    public void setPoKeeperSeq(String poKeeperSeq) {
        this.poKeeperSeq = poKeeperSeq;
    }

    public String getWkTimeTel() {
        return wkTimeTel;
    }

    public void setWkTimeTel(String wkTimeTel) {
        this.wkTimeTel = wkTimeTel;
    }

    public String getUsrStat() {
        return usrStat;
    }

    public void setUsrStat(String usrStat) {
        this.usrStat = usrStat;
    }

    public String getLoginErrDesc() {
        return loginErrDesc;
    }

    public void setLoginErrDesc(String loginErrDesc) {
        this.loginErrDesc = loginErrDesc;
    }

    public String getAuthMaskBm() {
        return authMaskBm;
    }

    public void setAuthMaskBm(String authMaskBm) {
        this.authMaskBm = authMaskBm;
    }

    public String getHuStat() {
        return huStat;
    }

    public void setHuStat(String huStat) {
        this.huStat = huStat;
    }

    public String getRepairTimeCnt() {
        return repairTimeCnt;
    }

    public void setRepairTimeCnt(String repairTimeCnt) {
        this.repairTimeCnt = repairTimeCnt;
    }

    public String getCommanyShortName() {
        return commanyShortName;
    }

    public void setCommanyShortName(String commanyShortName) {
        this.commanyShortName = commanyShortName;
    }

    public String getModuleBitmap() {
        return moduleBitmap;
    }

    public void setModuleBitmap(String moduleBitmap) {
        this.moduleBitmap = moduleBitmap;
    }

    public String getRepairWorkTime() {
        return repairWorkTime;
    }

    public void setRepairWorkTime(String repairWorkTime) {
        this.repairWorkTime = repairWorkTime;
    }

    public String getUrgentTel() {
        return urgentTel;
    }

    public void setUrgentTel(String urgentTel) {
        this.urgentTel = urgentTel;
    }

    public String getHuRole() {
        return huRole;
    }

    public void setHuRole(String huRole) {
        this.huRole = huRole;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getAuthBitmap() {
        return authBitmap;
    }

    public void setAuthBitmap(String authBitmap) {
        this.authBitmap = authBitmap;
    }

    public String getNotifyBitmap() {
        return notifyBitmap;
    }

    public void setNotifyBitmap(String notifyBitmap) {
        this.notifyBitmap = notifyBitmap;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHouseStat() {
        return houseStat;
    }

    public void setHouseStat(String houseStat) {
        this.houseStat = houseStat;
    }

    public Integer getScoreSum() {
        return scoreSum;
    }

    public void setScoreSum(Integer scoreSum) {
        this.scoreSum = scoreSum;
    }

	public String getPropType() {
		return propType;
	}

	public void setPropType(String propType) {
		this.propType = propType;
	}

	public String getAdvCfeeMon() {
		return advCfeeMon;
	}

	public void setAdvCfeeMon(String advCfeeMon) {
		this.advCfeeMon = advCfeeMon;
	}
	
}

package com.ej.hgj.request.hu;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author  xia
 * @version $Id: HuHouseBindRequest.java, v 0.1 2020年8月27日 下午4:20:26 xia Exp $
 */
@Data
public class HuHouseBindRequest implements Serializable {

    /**  */
    private static final long serialVersionUID = -5070867191871423139L;
    
    // 社区公众号用户openid
    private String wxOpenid;

    // 社区客户号
    private String custId;

    private String cstCode;
    
    // 登录凭证，获取获取小程序openid
    private String code;
    
    // 住房信息  areaId|buildingId|houseNo|huRole|huOpenMode|checkCode|usrSeqId|usrType
    private String houseInfo;
    
    // 签名
    private String sign;
    
    // 住户角色 携带房屋信息一定会存在住户角色
    private String huRole;
    
    /***********以下参数可空**********/
    
    private String version; // 签名版本：认领页面存在签名版本，区分认领页面签名和模板链接中的
    
    // 门牌号
    private String houseNo;
    
    // 区域ID
    private String areaId;
    
    // 楼号
    private String buildingId;
    
    // 绑定模式：P-住户认领模式，H-扫物业码模式，O-认领即开通，无需线下手工审核
    private String bindMode;
    
    // 扫码开通校验checkCode
    private String checkCode;
    
    // 认领码
    private String packCode;
    
    // 慧管家小程序openid, 和code不能同时为空
    private String hgjOpenid;
    
    // 企业信息获取公众号原始ID
    private String pubOrgId;
    
    private String usrSeqId;
    
    private String usrType;
    
    // 小区简称
    private String shortName;
    
    // 用户昵称
    private String nickName;
    

}

package com.ej.hgj.vo.bill;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * 签名信息
 *
 */
@Data
public class SignInfoVo {

	// 服务商小程序appId
	private String spAppId;
	// 特约商户小程序appId
	private String subAppId;
	// 时间戳
	private String timeStamp;
	// 随机字符串，不长于32位
	private String nonceStr;
	// 下单返回
	private String repayId;
	// 签名方式
	private String signType;
	// 签名
	private String paySign;
	// 服务商户号
	private String spMchId;
	// 子服务商户号
	private String subMchId;
	// 证书序列号
	private String serialNo;
	// 直连商户号
	private String mchId;

}

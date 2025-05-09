package com.ej.hgj.controller.xhparkpay;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstBillDaoMapper;
import com.ej.hgj.dao.card.CardCstDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayInvoiceDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoCardMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.xhparkpay.XhParkCouponDaoMapper;
import com.ej.hgj.dao.xhparkpay.XhParkCouponLogDaoMapper;
import com.ej.hgj.entity.card.CardCst;
import com.ej.hgj.entity.card.CardCstBatch;
import com.ej.hgj.entity.card.CardCstBill;
import com.ej.hgj.entity.carpay.ParkPayInvoice;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoCard;
import com.ej.hgj.entity.moncarren.CompanyInfo;
import com.ej.hgj.entity.xhparkpay.XhParkCoupon;
import com.ej.hgj.entity.xhparkpay.XhParkCouponLog;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.service.xhparkpay.XhParkPayService;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.SecurityUtil;
import com.ej.hgj.utils.bill.*;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.*;
import com.ej.hgj.vo.moncarren.MonCarRenRequestVo;
import com.ej.hgj.vo.xhparkpay.XhParkCouponLogVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import okhttp3.HttpUrl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 新弘北外滩停车缴费-只抵扣时间不缴费
 */
@Controller
public class XhParkPayController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${private.key}")
	private String privateKey;

	@Value("${private.key.yy}")
	private String privateKeyYy;

	@Autowired
	private CarPayService carPayService;

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private CardCstBatchDaoMapper cardCstBatchDaoMapper;

	@Autowired
	private CardCstDaoMapper cardCstDaoMapper;

	@Autowired
	private ParkPayOrderDaoMapper parkPayOrderDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private ParkPayOrderTempDaoMapper parkPayOrderTempDaoMapper;

	@Autowired
	private CstIntoCardMapper cstIntoCardMapper;

	@Autowired
	private CstIntoMapper cstIntoMapper;

	@Autowired
	private ParkPayInvoiceDaoMapper parkPayInvoiceDaoMapper;

	@Autowired
	private XhParkCouponDaoMapper xhParkCouponDaoMapper;

	@Autowired
	private CardCstBillDaoMapper cardCstBillDaoMapper;

	@Autowired
	private XhParkPayService xhParkPayService;

	@Autowired
	private XhParkCouponLogDaoMapper xhParkCouponLogDaoMapper;

	/**
	 * 车牌号查询停车订单费用接口
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/xhParkPay/queryCarNum")
	public CarPayResponseVo queryCarNum(@RequestBody CarPayRequestVo carPayRequestVo) {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String carCode = carPayRequestVo.getCarCode();
		// 调用车牌号查询停车订单费用接口
		ConstantConfig zhtc_api_url = constantConfDaoMapper.getByKey(Constant.ZHTC_API_URL);
		String apiUrl = zhtc_api_url.getConfigValue();
		ConstantConfig zhtc_api_app_id = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_ID);
		String appId = zhtc_api_app_id.getConfigValue();
		ConstantConfig zhtc_api_app_secret = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_SECRET);
		String appSecret = zhtc_api_app_secret.getConfigValue();
		ConstantConfig zhtc_api_auth_code = constantConfDaoMapper.getByKey(Constant.ZHTC_API_AUTH_CODE);
		String authCode = zhtc_api_auth_code.getConfigValue();
		Random random = new Random();
		// 生成一个9位小数的随机数
		double randomNumber = random.nextDouble();
		// 保留9位小数
		randomNumber = Math.round(randomNumber * 1e9) / 1e9;
		String start = "{";
		String appid = "\"appid\":\"" + appId + "\",";
		String carNo = "\"carNo\":\"" + carCode + "\",";
		String parkKey = "\"parkKey\":\"" + authCode + "\",";
		String rand = "\"rand\":\"" + randomNumber + "\",";
		String version = "\"version\":\"v1.0\"";
		String end = "}";
		String signData = "appid=" + appId + "&carNo=" + carCode + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
		String stringSignTemp = signData + appSecret;
		String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
		String signJson = ",\"sign\":\"" + sign + "\"";
		String pramJson = start + appid + carNo + parkKey + rand + version + signJson + end;
		try{
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inquire/GetCarNoOrderFee", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");
			// 失败
			if("0".equals(code)){
				carPayResponseVo.setRespCode("999");
				carPayResponseVo.setErrDesc(msg);
				return carPayResponseVo;
			}
			// 成功
			if("1".equals(code)){
				Boolean payFeeStatus = false;
				JSONObject jsonData = resultJson.getJSONObject("data");
				String totalAmount = jsonData.getString("totalAmount");
				// 接口返回支付金额
				BigDecimal payFee = new BigDecimal(totalAmount);

				if(payFee.compareTo(BigDecimal.ZERO) > 0){
					payFeeStatus = true;
					String inParkTime = jsonData.getString("enterTime");
					String outPartTime = DateUtils.strYmdHms();
					Date inDate = DateUtils.strDate(inParkTime);
					Date outDate = DateUtils.strDate(outPartTime);
					long[] distanceTimes = DateUtils.getDistanceTimes(inDate, outDate);
					String parkDur = "";
					if(distanceTimes[0] > 0){
						parkDur =  parkDur + distanceTimes[0] +"天";
					}
					parkDur =  parkDur + distanceTimes[1] +"时";
					parkDur =  parkDur + distanceTimes[2] +"分";
					parkDur =  parkDur + distanceTimes[3] +"秒";

					// 查询车辆信息
					CarInfoVo carInfoVo = new CarInfoVo();
					carInfoVo.setCarCode(carCode);
					carInfoVo.setInParkTime(inParkTime);
					carInfoVo.setOutParkTime(outPartTime);
					carInfoVo.setTotalAmount(payFee);
					carInfoVo.setParkDur(parkDur);

					// 返回数据
					carPayResponseVo.setCarInfoVo(carInfoVo);
					carPayResponseVo.setPayFeeStatus(payFeeStatus);
					carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
					carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
					carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
					return carPayResponseVo;
				}else {
					carPayResponseVo.setPayFeeStatus(payFeeStatus);
					carPayResponseVo.setRespCode("999");
					carPayResponseVo.setErrDesc("该车牌目前无需付费，可直接出场");
					return carPayResponseVo;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			carPayResponseVo.setRespCode("999");
			carPayResponseVo.setErrDesc(e.toString());
			logger.info("------车牌号查询失败----------");
		}
		return carPayResponseVo;
	}

	/**
	 * 车牌时间优惠-未用
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/xhParkPay/carNoFreeTimeCoupon")
	public CarPayResponseVo carNoFreeTimeCoupon(@RequestBody CarPayRequestVo carPayRequestVo) {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String carCode = carPayRequestVo.getCarCode();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String expDate = DateUtils.strYm(new Date());
		Integer hourNumValue = carPayRequestVo.getHourNumValue();
		// 查询当月停车卡信息
		CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, "2", expDate);
		// 调用车牌号查询停车订单费用接口
		ConstantConfig zhtc_api_url = constantConfDaoMapper.getByKey(Constant.ZHTC_API_URL);
		String apiUrl = zhtc_api_url.getConfigValue();
		ConstantConfig zhtc_api_app_id = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_ID);
		String appId = zhtc_api_app_id.getConfigValue();
		ConstantConfig zhtc_api_app_secret = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_SECRET);
		String appSecret = zhtc_api_app_secret.getConfigValue();
		ConstantConfig zhtc_api_auth_code = constantConfDaoMapper.getByKey(Constant.ZHTC_API_AUTH_CODE);
		String authCode = zhtc_api_auth_code.getConfigValue();
		String expMinus = (hourNumValue * 60) + "";
		Random random = new Random();
		// 生成一个9位小数的随机数
		double randomNumber = random.nextDouble();
		// 保留9位小数
		randomNumber = Math.round(randomNumber * 1e9) / 1e9;
		// 19位随机数
		String couponKeyRandomNumber = TimestampGenerator.generateSerialNumber();
		couponKeyRandomNumber = couponKeyRandomNumber.substring(0,couponKeyRandomNumber.length()-1);
		String start = "{";
		String appid = "\"appid\":\"" + appId + "\",";
		String carNo = "\"carNo\":\"" + carCode + "\",";
		String couponKey = "\"couponKey\":\"" + couponKeyRandomNumber + "\",";
		String freeTimeMin = "\"freeTimeMin\":\"" + expMinus + "\",";
		String parkKey = "\"parkKey\":\"" + authCode + "\",";
		String rand = "\"rand\":\"" + randomNumber + "\",";
		String version = "\"version\":\"v1.0\"";
		String end = "}";
		String signData = "";
		String pramJson = "";
		// 停车卡抵扣
		if(cardInfo != null && hourNumValue != null && hourNumValue > 0){
			signData = "appid=" + appId + "&carNo=" + carCode + "&couponKey=" + couponKeyRandomNumber + "&freeTimeMin=" + expMinus + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
			String stringSignTemp = signData + appSecret;
			String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
			String signJson = ",\"sign\":\"" + sign + "\"";
			pramJson = start + appid + carNo + couponKey + freeTimeMin + parkKey + rand + version + signJson + end;
		}
		try{
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inform/CarNoFreeTimeCoupon", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");
			// 失败
			if("0".equals(code)){
				carPayResponseVo.setRespCode("999");
				carPayResponseVo.setErrDesc(msg);
				return carPayResponseVo;
			}
			// 成功
			if("1".equals(code)){
				logger.info("车牌时间优惠接口调用成功：" + "msg:" + msg +"||pramJson:" + pramJson);
				carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
				carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
				carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
				return carPayResponseVo;
			}
		}catch (Exception e){
			e.printStackTrace();
			carPayResponseVo.setRespCode("999");
			carPayResponseVo.setErrDesc(e.toString());
			logger.info("------车牌号查询失败----------");
		}
		return carPayResponseVo;
	}

	/**
	 * 车牌优惠
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/xhParkPay/carNoCoupon")
	public CarPayResponseVo carNoCoupon(@RequestBody CarPayRequestVo carPayRequestVo) {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String carCode = carPayRequestVo.getCarCode();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String expDate = DateUtils.strYm(new Date());
		Integer hourNumValue = carPayRequestVo.getHourNumValue();
		if(hourNumValue == null){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.RESULT_FAILED.getRespCode());
			carPayResponseVo.setErrDesc("抵扣时长不能为空");
			return carPayResponseVo;
		}
		if(StringUtils.isBlank(carPayRequestVo.getCardCstBatchId())){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.RESULT_FAILED.getRespCode());
			carPayResponseVo.setErrDesc("停车优惠数据不能为空");
			return carPayResponseVo;
		}
		if(StringUtils.isBlank(carCode)){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.RESULT_FAILED.getRespCode());
			carPayResponseVo.setErrDesc("车牌号不能为空");
			return carPayResponseVo;
		}
		// 校验车牌号当天抵扣次数
		List<XhParkCouponLog> listByCarCode = xhParkCouponLogDaoMapper.getListByCarCode(carCode);
		// 查询当天抵扣上线数
		ConstantConfig byProNumAndKey = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.XH_PARK_COUPON_DAY_MAX_NUM);
		if(!listByCarCode.isEmpty() && listByCarCode.size() >= Integer.valueOf(byProNumAndKey.getConfigValue())){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.RESULT_FAILED.getRespCode());
			carPayResponseVo.setErrDesc("当天抵扣次数已用完");
			return carPayResponseVo;
		}
		// 查询当月停车卡信息
		CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, "2", expDate);
		// 调用车牌号查询停车订单费用接口
		ConstantConfig zhtc_api_url = constantConfDaoMapper.getByKey(Constant.ZHTC_API_URL);
		String apiUrl = zhtc_api_url.getConfigValue();
		ConstantConfig zhtc_api_app_id = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_ID);
		String appId = zhtc_api_app_id.getConfigValue();
		ConstantConfig zhtc_api_app_secret = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_SECRET);
		String appSecret = zhtc_api_app_secret.getConfigValue();
		ConstantConfig zhtc_api_auth_code = constantConfDaoMapper.getByKey(Constant.ZHTC_API_AUTH_CODE);
		String authCode = zhtc_api_auth_code.getConfigValue();
		// 查询优惠券方案编号
		XhParkCoupon byCouponHour = xhParkCouponDaoMapper.getByCouponHour("hour_" + hourNumValue);
		String couponNo = byCouponHour.getCouponNo();
		Random random = new Random();
		// 生成一个9位小数的随机数
		double randomNumber = random.nextDouble();
		// 保留9位小数
		randomNumber = Math.round(randomNumber * 1e9) / 1e9;
		String start = "{";
		String appid = "\"appid\":\"" + appId + "\",";
		String carNo = "\"carNo\":\"" + carCode + "\",";
		String couponsolutionNo = "\"couponsolutionNo\":\"" + couponNo + "\",";
		String parkKey = "\"parkKey\":\"" + authCode + "\",";
		String rand = "\"rand\":\"" + randomNumber + "\",";
		String version = "\"version\":\"v1.0\"";
		String end = "}";
		String signData = "";
		String pramJson = "";
		// 停车卡抵扣
		if(cardInfo != null && hourNumValue != null && hourNumValue > 0){
			signData = "appid=" + appId + "&carNo=" + carCode + "&couponsolutionNo=" + couponNo + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
			String stringSignTemp = signData + appSecret;
			String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
			String signJson = ",\"sign\":\"" + sign + "\"";
			pramJson = start + appid + carNo + couponsolutionNo + parkKey + rand + version + signJson + end;
		}
		try{
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inform/CarNoCoupon", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");
			// 失败
			if("0".equals(code)){
				carPayResponseVo.setRespCode("999");
				carPayResponseVo.setErrDesc(msg);
				return carPayResponseVo;
			}
			// 成功
			if("1".equals(code)){
				logger.info("车牌优惠接口调用成功：" + "msg:" + msg +"||pramJson:" + pramJson);
				carPayRequestVo.setCouponNo(couponNo);
				xhParkPayService.carNoCouponSuccess(carPayRequestVo);
				carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
				carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
				carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
				return carPayResponseVo;
			}
		}catch (Exception e){
			e.printStackTrace();
			carPayResponseVo.setRespCode("999");
			carPayResponseVo.setErrDesc(e.toString());
			logger.info("------车牌号查询失败----------");
		}
		return carPayResponseVo;
	}

	/**
	 * 查询停车卡时长
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/xhParkPay/queryCardExpNum")
	public JSONObject queryCardExpNum(@RequestBody CarPayRequestVo carPayRequestVo) {
		JSONObject jsonObject = new JSONObject();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String expDate = DateUtils.strYm(new Date());
		String wxOpenId = carPayRequestVo.getWxOpenId();
		// 查询登录人身份
		CstInto byWxOpenIdAndStatus_1 = cstIntoMapper.getByWxOpenIdAndStatus_1(wxOpenId);
		// 查询登录人停车卡权限
		CstIntoCard cstIntoCard = new CstIntoCard();
		cstIntoCard.setWxOpenId(wxOpenId);
		cstIntoCard.setCardId(2);
		List<CstIntoCard> cstIntoCardList = cstIntoCardMapper.getList(cstIntoCard);
		try{
			// 查询当月停车卡信息
			CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, "2", expDate);
			if(cardInfo != null && byWxOpenIdAndStatus_1 != null && (byWxOpenIdAndStatus_1.getIntoRole() == 2 || !cstIntoCardList.isEmpty())){
				Integer expNum = cardInfo.getTotalNum() - cardInfo.getApplyNum();
				if(expNum <= 0) {
					expNum = 0;
				}
				// 查询停车抵扣说明文字
				ConstantConfig byProNumAndKey = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.XH_PARK_COUPON_DESC);
				jsonObject.put("cardCstBatchId", cardInfo.getCardCstBatchId());
				jsonObject.put("expNum", expNum);
				jsonObject.put("isCard", true);
				jsonObject.put("xhParkCouponDesc", byProNumAndKey.getConfigValue());
			}else {
				jsonObject.put("expNum", "0");
				jsonObject.put("isCard", false);
			}
			jsonObject.put("respCode", Constant.SUCCESS);
		}catch (Exception e){
			e.printStackTrace();
			jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
			jsonObject.put("errDesc", e.toString());
			logger.info("------停车卡查询失败----------");
		}
		return jsonObject;
	}

	/**
	 * 查询选择停车卡时长数组
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/xhParkPay/queryHourNum")
	public JSONObject queryHourNum(@RequestBody CarPayRequestVo carPayRequestVo) {
		JSONObject jsonObject = new JSONObject();
		String carCode = carPayRequestVo.getCarCode();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String expDate = DateUtils.strYm(new Date());
		// 调用车牌号查询停车订单费用接口
		ConstantConfig zhtc_api_url = constantConfDaoMapper.getByKey(Constant.ZHTC_API_URL);
		String apiUrl = zhtc_api_url.getConfigValue();
		ConstantConfig zhtc_api_app_id = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_ID);
		String appId = zhtc_api_app_id.getConfigValue();
		ConstantConfig zhtc_api_app_secret = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_SECRET);
		String appSecret = zhtc_api_app_secret.getConfigValue();
		ConstantConfig zhtc_api_auth_code = constantConfDaoMapper.getByKey(Constant.ZHTC_API_AUTH_CODE);
		String authCode = zhtc_api_auth_code.getConfigValue();
		Random random = new Random();
		// 生成一个9位小数的随机数
		double randomNumber = random.nextDouble();
		// 保留9位小数
		randomNumber = Math.round(randomNumber * 1e9) / 1e9;
		String start = "{";
		String appid = "\"appid\":\"" + appId + "\",";
		String carNo = "\"carNo\":\"" + carCode + "\",";
		String parkKey = "\"parkKey\":\"" + authCode + "\",";
		String rand = "\"rand\":\"" + randomNumber + "\",";
		String version = "\"version\":\"v1.0\"";
		String end = "}";
		// 是否选择停车卡抵扣
		String signData = "appid=" + appId + "&carNo=" + carCode + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
		String stringSignTemp = signData + appSecret;
		String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
		String signJson = ",\"sign\":\"" + sign + "\"";
		String pramJson =  start + appid + carNo + parkKey + rand + version + signJson + end;;
		try{
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inquire/GetCarNoOrderFee", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");
			// 失败
			if("0".equals(code)){
				jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
				jsonObject.put("errDesc", msg);
				return jsonObject;
			}
			// 成功
			if("1".equals(code)){
				JSONObject jsonData = resultJson.getJSONObject("data");
				String inParkTime = jsonData.getString("enterTime");
				String outPartTime = DateUtils.strYmdHms();
				Date inDate = DateUtils.strDate(inParkTime);
				Date outDate = DateUtils.strDate(outPartTime);
				long[] distanceTimes = DateUtils.getDistanceTimes(inDate, outDate);
				long maxHour = distanceTimes[0] * 24 + distanceTimes[1];
				if(distanceTimes[2] > 0){
					maxHour = maxHour + 1;
				}
				// 查询当月停车卡信息
				CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, "2", expDate);
				if(cardInfo != null){
					Integer expNum = cardInfo.getTotalNum() - cardInfo.getApplyNum();
					if(expNum < maxHour){
						maxHour = expNum;
					}
				}
				List<Integer> hourNumArray = new ArrayList<>();
				for(int i = 1; i <= maxHour; i++){
					hourNumArray.add(i);
				}
				jsonObject.put("respCode", Constant.SUCCESS);
				jsonObject.put("hourNumArray",hourNumArray);
				return jsonObject;
			}
		}catch (Exception e){
			e.printStackTrace();
			jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
			jsonObject.put("errDesc", (e.toString()));
			logger.info("------车牌号查询失败----------");
		}
		return jsonObject;
	}


	@RequestMapping("/xhParkPay/parkDeductionLog.do")
	@ResponseBody
	public XhParkCouponLogVo parkDeductionLog(@RequestBody XhParkCouponLogVo xhParkCouponLogVo) {
		PageHelper.offsetPage((xhParkCouponLogVo.getPageNum()-1) * xhParkCouponLogVo.getPageSize(),xhParkCouponLogVo.getPageSize());
		XhParkCouponLog xhParkCouponLog = new XhParkCouponLog();
		xhParkCouponLog.setWxOpenId(xhParkCouponLogVo.getWxOpenId());
		List<XhParkCouponLog> list = xhParkCouponLogDaoMapper.getList(xhParkCouponLog);
		PageInfo<XhParkCouponLog> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)xhParkCouponLogVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		xhParkCouponLogVo.setPages(pageNumTotal);
		xhParkCouponLogVo.setTotalNum((int) pageInfo.getTotal());
		xhParkCouponLogVo.setPageSize(xhParkCouponLogVo.getPageSize());
		xhParkCouponLogVo.setList(list);
		xhParkCouponLogVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return xhParkCouponLogVo;
	}


}

package com.ej.hgj.controller.carpay;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoCardMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import java.math.RoundingMode;
import com.ej.hgj.entity.card.CardCst;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoCard;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.bill.*;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.*;
import com.ej.hgj.vo.opendoor.OpenDoorCodeVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import okhttp3.HttpUrl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.dfp.DfpField;
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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class CarPayController extends BaseController {

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

	/**
	 * 车牌号查询停车订单费用接口
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/carpay/queryCarNum")
	public CarPayResponseVo queryCarNum(@RequestBody CarPayRequestVo carPayRequestVo) {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String carCode = carPayRequestVo.getCarCode();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String expDate = DateUtils.strYm(new Date());
		Boolean radioChecked = carPayRequestVo.getRadioChecked();
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
		String signData = "";
//		String expMinus = "";
		// 是否选择停车卡抵扣
		//if(radioChecked == true && cardInfo != null){
//			Integer expNum = cardInfo.getTotalNum() - cardInfo.getApplyNum();
//			expMinus = (expNum * 60)+"";
//			expMinus = (hourNumValue * 60) + "";
//			signData = "appid=" + appId + "&carNo=" + carCode + "&freeMinutes=" + expMinus + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
		//}else {
			signData = "appid=" + appId + "&carNo=" + carCode + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
		//}
		String stringSignTemp = signData + appSecret;
		String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
		String signJson = ",\"sign\":\"" + sign + "\"";
		String pramJson = "";
		//if(radioChecked == true && cardInfo != null){
		//	String freeMinutes = "\"freeMinutes\":\"" + expMinus + "\",";
		//	pramJson = start + appid + carNo + freeMinutes + parkKey + rand + version + signJson + end;
	//	}else {
			pramJson = start + appid + carNo + parkKey + rand + version + signJson + end;
	//	}
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
				// 测试用
//				totalAmount = "0.01";
//				if(radioChecked == true){
//					totalAmount = "0.0";
//				}
				// 接口返回支付金额
				BigDecimal payFee = new BigDecimal(totalAmount);

				// 计算扣减后实付金额
				if(radioChecked == true && cardInfo != null){
					// 获取每小时停车费
					ConstantConfig zhtc_hour_park_cost = constantConfDaoMapper.getByKey(Constant.ZHTC_HOUR_PARK_COST);
					BigDecimal hourCost = new BigDecimal(zhtc_hour_park_cost.getConfigValue());
					BigDecimal subtract = payFee.subtract(hourCost.multiply(new BigDecimal(hourNumValue)));
					if(subtract.compareTo(BigDecimal.ZERO) < 0){
						payFee = BigDecimal.ZERO;
					}else {
						payFee = subtract;
					}
				}

				// todo 测试
				payFee = payFee.divide(new BigDecimal("800"));

				if((payFee.compareTo(BigDecimal.ZERO) > 0) || (payFee.compareTo(BigDecimal.ZERO) == 0 && radioChecked == true)){
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

					// 计算客户选择抵扣小时数组 ，最小为1，最大为停车小时数，如果有分钟，则小时加1 , 最大数如果超出客户停车卡剩余时长，则取剩余时长为最大数。
//					Long maxHour = distanceTimes[0] * 60 + distanceTimes[1];
//					if(distanceTimes[2] > 0){
//						maxHour = maxHour + 1;
//					}
//					if(cardInfo != null){
//						Integer expNum = cardInfo.getTotalNum() - cardInfo.getApplyNum();
//						if(expNum < maxHour){
//							maxHour = expNum.longValue();
//						}
//					}
//					List<Integer> hourNumArray = new ArrayList<>();
//					for(int i = 1; i <= maxHour; i++){
//						hourNumArray.add(i);
//					}

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
	 * 查询停车卡时长
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/carpay/queryCardExpNum")
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
				if(expNum > 0) {
					jsonObject.put("cardCstBatchId", cardInfo.getCardCstBatchId());
					jsonObject.put("expNum", expNum);
					jsonObject.put("isCard", true);
				}
			}else {
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
	@RequestMapping("/carpay/queryHourNum")
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
				String totalAmount = jsonData.getString("totalAmount");
				BigDecimal payFee = new BigDecimal(totalAmount);
				// 计算客户选择抵扣小时数组 ，最小为1，最大为停车小时数(停车应付金额/每小时停车费)，如果有余数，则小时加1 , 最大数如果超出客户停车卡剩余时长，则取剩余时长为最大数。
				// 获取每小时停车费
				ConstantConfig zhtc_hour_park_cost = constantConfDaoMapper.getByKey(Constant.ZHTC_HOUR_PARK_COST);
				BigDecimal hourCost = new BigDecimal(zhtc_hour_park_cost.getConfigValue());
				// 通过停车应付金额计算小时,应付金额除以每小时停车费，向上取整
				BigDecimal divide = payFee.divide(hourCost, 10, RoundingMode.HALF_UP);
				BigDecimal resultCeiling = divide.setScale(0, RoundingMode.CEILING);
				Integer maxHour = resultCeiling.intValue();
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

	/**
	 * 停车缴费
	 * @param carPayRequestVo
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/carpay/carPayment.do")
	public CarPayResponseVo carPayment(@RequestBody CarPayRequestVo carPayRequestVo) throws Exception {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String cstCode = carPayRequestVo.getCstCode();
		String wxOpenId = carPayRequestVo.getWxOpenId();
		String proNum = carPayRequestVo.getProNum();
		String cardCstBatchId = carPayRequestVo.getCardCstBatchId();
		String carCode = carPayRequestVo.getCarCode();
		Boolean radioChecked = carPayRequestVo.getRadioChecked();
		Integer hourNumValue = carPayRequestVo.getHourNumValue();
		// 参数校验
		if(StringUtils.isBlank(cstCode)){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespDesc());
			return carPayResponseVo;
		}
		if(StringUtils.isBlank(wxOpenId)){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespDesc());
			return carPayResponseVo;
		}
		if(StringUtils.isBlank(proNum)){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespDesc());
			return carPayResponseVo;
		}
		if(StringUtils.isBlank(carCode)){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.CAR_PAY_CAR_CODE_NULL.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.CAR_PAY_CAR_CODE_NULL.getRespDesc());
			return carPayResponseVo;
		}
		try {
			// 根据车牌号查询缴费信息
			String expDate = DateUtils.strYm(new Date());
			CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, "2", expDate);
			if(radioChecked == true && cardInfo == null){
				carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
				carPayResponseVo.setErrCode("6001");
				carPayResponseVo.setErrDesc("停车卡查询失败");
				return carPayResponseVo;
			}
			// 停车卡剩余时长
			Integer expNum = cardInfo.getTotalNum() - cardInfo.getApplyNum();
			// 停车卡时长校验
			if(radioChecked == true && expNum <= 0){
				carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
				carPayResponseVo.setErrCode("6001");
				carPayResponseVo.setErrDesc("抵扣失败，请返回重试");
				return carPayResponseVo;
			}
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
			String signData = "";
		//	String expMinus = "";
			// 是否选择停车卡抵扣
			//if (radioChecked == true) {
				//expMinus = (expNum * 60) + "";
			//	expMinus = (hourNumValue * 60) + "";
			//	signData = "appid=" + appId + "&carNo=" + carCode + "&freeMinutes=" + expMinus + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
		//	} else {
				signData = "appid=" + appId + "&carNo=" + carCode + "&parkKey=" + authCode + "&rand=" + randomNumber + "&version=v1.0&";
		//	}
			String stringSignTemp = signData + appSecret;
			String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
			String signJson = ",\"sign\":\"" + sign + "\"";
			String pramJson = "";
		//	if (radioChecked == true) {
		//		String freeMinutes = "\"freeMinutes\":\"" + expMinus + "\",";
		//		pramJson = start + appid + carNo + freeMinutes + parkKey + rand + version + signJson + end;
		//	} else {
				pramJson = start + appid + carNo + parkKey + rand + version + signJson + end;
		//	}
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inquire/GetCarNoOrderFee", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");

			// 订单号
			String orderId = TimestampGenerator.generateSerialNumber();
			Date sysDate = new Date();
			int intTotalAmount = 0;
			// 应付金额，接口返回
			BigDecimal payAmount = null;
			// 实付金额,抵扣以后金额
			BigDecimal actAmount = null;
			// 1-成功 0-失败
			if ("1".equals(code)) {
				JSONObject jsonData = resultJson.getJSONObject("data");
				String totalAmount = jsonData.getString("totalAmount");
				// 测试用
//				totalAmount = "0.01";
//				if(radioChecked == true){
//					totalAmount = "0.0";
//				}
				payAmount = new BigDecimal(totalAmount);

				// 校验提交订单时无费用发生，提示离场
				if(radioChecked == false && payAmount.compareTo(BigDecimal.ZERO) <= 0){
					carPayResponseVo.setRespCode("999");
					carPayResponseVo.setErrDesc("该车牌目前无需付费，可直接出场");
					return carPayResponseVo;
				}

				// 计算扣减后实付金额
				if(radioChecked == true && cardInfo != null){
					// 获取每小时停车费
					ConstantConfig zhtc_hour_park_cost = constantConfDaoMapper.getByKey(Constant.ZHTC_HOUR_PARK_COST);
					BigDecimal hourCost = new BigDecimal(zhtc_hour_park_cost.getConfigValue());
					BigDecimal subtract = payAmount.subtract(hourCost.multiply(new BigDecimal(hourNumValue)));
					if(subtract.compareTo(BigDecimal.ZERO) < 0){
						actAmount = BigDecimal.ZERO;
					}else {
						actAmount = subtract;
					}
				}else {
					actAmount = payAmount;
				}

				// todo 测试
				actAmount = actAmount.divide(new BigDecimal("800"));

				// 入场时间
				String enterTime = jsonData.getString("enterTime");
				// 停车场订单号
				String parkOrderNo = jsonData.getString("orderNo");
				// 创建微信支付订单
				ParkPayOrder parkPayOrder = new ParkPayOrder();
				parkPayOrder.setId(orderId);
				parkPayOrder.setProNum(proNum);
				parkPayOrder.setParkOrderNo(parkOrderNo);
				if (radioChecked == true) {
					parkPayOrder.setIsDeduction(1);
					parkPayOrder.setDeductionNum(hourNumValue);
					parkPayOrder.setCardCstBatchId(cardCstBatchId);
				} else {
					parkPayOrder.setIsDeduction(0);
				}
				parkPayOrder.setCarCode(carCode);
				parkPayOrder.setWxOpenId(wxOpenId);
				parkPayOrder.setCstCode(cstCode);
				HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
				parkPayOrder.setCstName(hgjCst.getCstName());
				parkPayOrder.setPayAmount(payAmount);
				parkPayOrder.setActAmount(actAmount);
				BigDecimal multiply = actAmount.multiply(new BigDecimal("100"));
				intTotalAmount = multiply.intValue();
				parkPayOrder.setAmountTotal(intTotalAmount);
				parkPayOrder.setIpItemName(orderId);
				parkPayOrder.setInTime(enterTime);
				parkPayOrder.setOutTime(DateUtils.strYmdHms(sysDate));
				parkPayOrder.setOrderStatus(0);
				parkPayOrder.setCreateTime(sysDate);
				parkPayOrder.setUpdateTime(sysDate);
				parkPayOrder.setDeleteFlag(Constant.DELETE_FLAG_NOT);
				parkPayOrderDaoMapper.save(parkPayOrder);
				logger.info("缴费订单号:" + orderId + "||缴费金额:" + payAmount);

				// 调用接口创建临停车支付订单
				double orderCreateRandomNumber = Math.round(randomNumber * 1e9) / 1e9;
				String orderCreateRand = "\"rand\":\"" + orderCreateRandomNumber + "\",";
				String orderAmount = "\"orderAmount\":\"" + payAmount + "\",";
				String orderNo = "\"orderNo\":\"" + parkOrderNo + "\",";
				String payScene = "\"payScene\":\"" + 2 + "\",";
				String orderCreateSignData = "appid=" + appId + "&orderAmount=" + payAmount + "&orderNo=" + parkOrderNo + "&parkKey=" + authCode + "&payScene=2" + "&rand=" + orderCreateRandomNumber + "&version=v1.0&";
				String orderCreateSignTemp = orderCreateSignData + appSecret;
				String orderCreateSign = DigestUtils.md5Hex(orderCreateSignTemp).toUpperCase();
				String orderCreateSignJson = ",\"sign\":\"" + orderCreateSign + "\"";
				String orderCreatePramJson = start + appid + orderAmount + orderNo + parkKey + payScene + orderCreateRand + version + orderCreateSignJson + end;
				JSONObject orderCreateResultJson = HttpClientUtil.sendPost(apiUrl + "/Inform/OrderPayCreate", orderCreatePramJson);
				String orderCreateCode = orderCreateResultJson.get("code").toString();
				String orderCreateMsg = resultJson.getString("msg");
				// 成功
				if ("1".equals(orderCreateCode)) {
					if (parkPayOrder != null && !"".equals(parkPayOrder.getId())) {
						JSONObject jsonObject = orderCreateResultJson.getJSONObject("data");
						String payOrderNo = jsonObject.getString("payOrderNo");
						// 保存临停车支付订单
						ParkPayOrderTemp parkPayOrderTemp = new ParkPayOrderTemp();
						parkPayOrderTemp.setId(TimestampGenerator.generateSerialNumber());
						parkPayOrderTemp.setOrderId(orderId);
						parkPayOrderTemp.setParkOrderNo(parkOrderNo);
						parkPayOrderTemp.setCreateCode(orderCreateCode);
						parkPayOrderTemp.setCreateMsg(orderCreateMsg);
						parkPayOrderTemp.setPayOrderNo(payOrderNo);
						parkPayOrderTemp.setCreateTime(new Date());
						parkPayOrderTemp.setUpdateTime(new Date());
						parkPayOrderTemp.setDeleteFlag(0);
						parkPayOrderTempDaoMapper.save(parkPayOrderTemp);
					} else {
						carPayResponseVo.setRespCode("999");
						carPayResponseVo.setErrDesc("订单创建失败");
						return carPayResponseVo;
					}

				} else {
					carPayResponseVo.setRespCode("999");
					carPayResponseVo.setErrDesc(orderCreateMsg);
					return carPayResponseVo;
				}
			} else {
				carPayResponseVo.setRespCode("999");
				carPayResponseVo.setErrDesc(msg);
				return carPayResponseVo;
			}

			if (actAmount != null && actAmount.compareTo(BigDecimal.ZERO) > 0) {
				// 服务商小程appId
				ConstantConfig spMiniProgramApp = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_WYGJ);
				// 特约商户小程appId
				ConstantConfig subMiniProgramApp = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
				// 设置签名对象
				SignInfoVo signInfo = new SignInfoVo();
				// 服务商小程序appId
				signInfo.setSpAppId(spMiniProgramApp.getAppId());
				// 特约商户小程序appId
				signInfo.setSubAppId(subMiniProgramApp.getAppId());
				// 时间戳
				signInfo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
				// 随机串
				signInfo.setNonceStr(RandomStringUtils.getRandomStringByLength(32));
				// 签名方式
				signInfo.setSignType("RSA");

				// --------------------根据项目号选择商户号--------------
				// 服务商模式-东方渔人码头
				if ("10000".equals(proNum)) {
					// 服务商户号-宜悦
					ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
					signInfo.setSpMchId(spMchId.getConfigValue());
					// 子服务商户号-东方渔人码头
					ConstantConfig subMchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.SUB_MCH_ID);
					signInfo.setSubMchId(subMchId.getConfigValue());
					// 服务商模式-大连路项目
				} else if ("10001".equals(proNum)) {
					// 服务商户号-宜悦
					ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
					signInfo.setSpMchId(spMchId.getConfigValue());
					// 子服务商户号-凡享
					ConstantConfig subMchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.SUB_MCH_ID_FX);
					signInfo.setSubMchId(subMchId.getConfigValue());
				}
				// 证书序列号
				ConstantConfig serialNo = constantConfDaoMapper.getByKey(Constant.SERIAL_NO_YY);
				signInfo.setSerialNo(serialNo.getConfigValue());
				String params = "";
				String prePayUrl = "";

				prePayUrl = Constant.PREPAY_URL;

				params = buildPlaceOrder(signInfo.getSpAppId(), signInfo.getSubAppId(), signInfo.getSpMchId(),
						signInfo.getSubMchId(), orderId, orderId,
						Constant.CARPAY_CALLBACK_URL, intTotalAmount, wxOpenId);
				HttpUrl httpurl = HttpUrl.parse(prePayUrl);
				// 获取证书token
				String authorization = carPayService.getToken("POST", httpurl, params, signInfo, proNum);
				// JSAPI下单
				authorization = "WECHATPAY2-SHA256-RSA2048" + " " + authorization;
				String result = HttpRequestUtils.post(prePayUrl, params, authorization);
				// 下单返回值转json
				JSONObject jsonObject = JSONObject.parseObject(result);
				String prepay_id = jsonObject.getString("prepay_id");
				String sinPrepayId = prepay_id;
				// 下单成功
				if (StringUtils.isNotBlank(prepay_id)) {
					signInfo.setRepayId("prepay_id=" + prepay_id);
					logger.info("下单成功返回prepay_id:" + prepay_id + "||单号:" + orderId + "||支付金额:" + intTotalAmount + "分");
				} else {
					// 下单失败
					carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
					carPayResponseVo.setErrCode(jsonObject.getString("code"));
					carPayResponseVo.setErrDesc(jsonObject.getString("message"));
					logger.info("下单失败单号:" + orderId + "||message:" + jsonObject.getString("message"));
					return carPayResponseVo;
				}

				// 生成签名
				String paySign = getPaySign(signInfo, sinPrepayId, proNum);
				if (StringUtils.isNotBlank(paySign)) {
					logger.info("生成签名成功返回paySign:" + paySign + "||单号:" + orderId);
					signInfo.setPaySign(paySign);
				} else {
					carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
					carPayResponseVo.setErrCode(JiasvBasicRespCode.SIGNATURE_FAILED.getRespCode());
					carPayResponseVo.setErrDesc(JiasvBasicRespCode.SIGNATURE_FAILED.getRespDesc());
					logger.info("生成签名失败单号:" + orderId);
					return carPayResponseVo;
				}
				carPayResponseVo.setOrderId(orderId);
				carPayResponseVo.setSignInfoVo(signInfo);
			}else {
				// 停车卡抵扣完后支付金额为0时处理
				List<Integer> paymentStatusList_01 = new ArrayList<>();
				paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRE);
				paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRO);
				CarPayOrderStatusVo requestOrderStatusVo_01 = new CarPayOrderStatusVo();
				requestOrderStatusVo_01.setId(orderId);
				requestOrderStatusVo_01.setOrderStatusList(paymentStatusList_01);
				ParkPayOrder parkPayOrder = parkPayOrderDaoMapper.getParkPayOrder(requestOrderStatusVo_01);
				parkPayOrder.setUpdateTime(new Date());
				carPayService.updateStatusSuccess(parkPayOrder);
				logger.info("----------订单金额为0订单处理成功----------");
			}
		}catch(Exception e){
			e.printStackTrace();
			carPayResponseVo.setErrCode("999");
			carPayResponseVo.setErrDesc(e.toString());
			return carPayResponseVo;
		}

		carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return carPayResponseVo;
	}

//	public JSONObject selectAmount(JSONObject resultJson, String orderId, String proNum, Boolean radioChecked, String cardCstBatchId,
//									String carCode, String wxOpenId, String cstCode, int intTotalAmount, Date sysDate, double randomNumber,
//									String appId, String authCode, String appSecret, String start, String appid, String parkKey, String version,
//									String end, String apiUrl){
//		JSONObject jsonData = resultJson.getJSONObject("data");
//		String totalAmount = jsonData.getString("totalAmount");
//		// 测试用
//		totalAmount = "0.01";
//		BigDecimal payAmount = new BigDecimal(totalAmount);
//
//		// 入场时间
//		String enterTime = jsonData.getString("enterTime");
//		// 停车场订单号
//		String parkOrderNo = jsonData.getString("orderNo");
//		// 创建微信支付订单
//		ParkPayOrder parkPayOrder = new ParkPayOrder();
//		parkPayOrder.setId(orderId);
//		parkPayOrder.setProNum(proNum);
//		parkPayOrder.setParkOrderNo(parkOrderNo);
//		if (radioChecked == true) {
//			parkPayOrder.setIsDeduction(1);
//			parkPayOrder.setCardCstBatchId(cardCstBatchId);
//		} else {
//			parkPayOrder.setIsDeduction(0);
//		}
//		parkPayOrder.setCarCode(carCode);
//		parkPayOrder.setWxOpenId(wxOpenId);
//		parkPayOrder.setCstCode(cstCode);
//		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
//		parkPayOrder.setCstName(hgjCst.getCstName());
//		parkPayOrder.setPayAmount(payAmount);
//		BigDecimal multiply = payAmount.multiply(new BigDecimal("100"));
//		intTotalAmount = multiply.intValue();
//		parkPayOrder.setAmountTotal(intTotalAmount);
//		parkPayOrder.setIpItemName(orderId);
//		parkPayOrder.setInTime(enterTime);
//		parkPayOrder.setOutTime(DateUtils.strYmdHms(sysDate));
//		parkPayOrder.setOrderStatus(0);
//		parkPayOrder.setCreateTime(sysDate);
//		parkPayOrder.setUpdateTime(sysDate);
//		parkPayOrder.setDeleteFlag(Constant.DELETE_FLAG_NOT);
//		parkPayOrderDaoMapper.save(parkPayOrder);
//		logger.info("缴费订单号:" + orderId + "||缴费金额:" + payAmount);
//
//		// 调用接口创建临停车支付订单
//		double orderCreateRandomNumber = Math.round(randomNumber * 1e9) / 1e9;
//		String orderCreateRand = "\"rand\":\"" + orderCreateRandomNumber + "\",";
//		String orderAmount = "\"orderAmount\":\"" + payAmount + "\",";
//		String orderNo = "\"orderNo\":\"" + parkOrderNo + "\",";
//		String payScene = "\"payScene\":\"" + 2 + "\",";
//		String orderCreateSignData = "appid=" + appId + "&orderAmount=" + payAmount + "&orderNo=" + parkOrderNo + "&parkKey=" + authCode + "&payScene=2" + "&rand=" + orderCreateRandomNumber + "&version=v1.0&";
//		String orderCreateSignTemp = orderCreateSignData + appSecret;
//		String orderCreateSign = DigestUtils.md5Hex(orderCreateSignTemp).toUpperCase();
//		String orderCreateSignJson = ",\"sign\":\"" + orderCreateSign + "\"";
//		String orderCreatePramJson = start + appid + orderAmount + orderNo + parkKey + payScene + orderCreateRand + version + orderCreateSignJson + end;
//		JSONObject orderCreateResultJson = HttpClientUtil.sendPost(apiUrl + "/Inform/OrderPayCreate", orderCreatePramJson);
//		return orderCreateResultJson;
//	}

	// 组装参数下单参数-服务商  小程序主体是特约商户
	public String buildPlaceOrder(String sp_appid, String sub_appid, String sp_mchid, String sub_mchid,
								  String description,String out_trade_no,
								  String notify_url,int total, String sub_openid){
		String spAppid = "{ \"sp_appid\":\"" + sp_appid + "\",";
		String subAppid = "\"sub_appid\":\""+sub_appid+"\",";
		String spMchid = "\"sp_mchid\":\"" + sp_mchid + "\",";
		String subMchid = "\"sub_mchid\":\""+sub_mchid+"\",";
		String desc = "\"description\":\"" + description + "\",";
		String outTradeNo = "\"out_trade_no\":\"" + out_trade_no + "\",";
		String notifyUrl = "\"notify_url\":\"" + notify_url + "\",";
		String amount = "\"amount\":{ \"total\":"+ total +",\"currency\":\"CNY\"},";
		String subOpenid = "\"payer\":{ \"sub_openid\":\"" +sub_openid + "\"}}";
		return spAppid + subAppid + spMchid + subMchid + desc  + outTradeNo + notifyUrl + amount + subOpenid;
	}

	//使用私钥对数据进行SHA256withRSA加密，并且进行Base64编码。
	public String getPaySign(SignInfoVo signInfoVo, String sinPrepayId, String proNum)  {
		//从下往上依次生成
		String signatureStr  = buildSignatureStr(signInfoVo.getSubAppId(), signInfoVo.getTimeStamp(),
				signInfoVo.getNonceStr(), signInfoVo.getRepayId());
		//签名方式
		Signature sign = null;
		try {
			sign = Signature.getInstance("SHA256withRSA");
			//私钥，通过MyPrivateKey来获取，这是个静态类可以接调用方法 ，需要的是_key.pem文件的绝对路径配上文件名
			if("10000".equals(proNum)){
				sign.initSign(MyPrivatekey.getPrivateKey(privateKey));
			}else if("10001".equals(proNum)){
				//sign.initSign(MyPrivatekey.getPrivateKey(privateKeyBus));
				sign.initSign(MyPrivatekey.getPrivateKey(privateKeyYy));
			}
			sign.update(signatureStr.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(sign.sign());
		} catch (NoSuchAlgorithmException | IOException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 *  按照前端签名文档规范进行排序，\n是换行
	 * @param appid
	 * @param timestamp
	 * @param nonceStr
	 * @param prepay_id
	 * @return
	 */
	public String buildSignatureStr (String appid, String timestamp,String nonceStr,String prepay_id) {
		return appid + "\n"
				+ timestamp + "\n"
				+ nonceStr + "\n"
				+ prepay_id + "\n";
	}

	/**
	 * 前端支付完成-修改状态为支付中
	 */
	@ResponseBody
	@RequestMapping("/parkPayOrderStatusUpdate.do")
	public BaseRespVo parkPayOrderStatusUpdate(@RequestBody CarPayOrderStatusVo requestOrderStatusVo) throws Exception {
		BaseRespVo baseRespVo = new BaseRespVo();
		String orderNo = requestOrderStatusVo.getId();
		// 根据订单号查询支付记录表预支付成功的订单，修改为支付中
		List<Integer> orderStatusList_0 = new ArrayList<>();
		orderStatusList_0.add(Constant.ORDER_STATUS_PRE);
		CarPayOrderStatusVo requestOrderStatusVo_0 = new CarPayOrderStatusVo();
		requestOrderStatusVo_0.setId(orderNo);
		requestOrderStatusVo_0.setOrderStatusList(orderStatusList_0);
		ParkPayOrder parkPayOrder_0 = parkPayOrderDaoMapper.getParkPayOrder(requestOrderStatusVo_0);
		if(parkPayOrder_0 != null){
			// 更新支付订单 为支付中
			carPayService.updateStatusPro(parkPayOrder_0);
		}
		baseRespVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		baseRespVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		baseRespVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		logger.info("订单orderNo:" + orderNo + "支付状态修改为支付中成功");
		return baseRespVo;
	}

	@PostMapping(value = "/carPay/callBack")
	@ResponseBody
	public Map<String, String> callBack(@RequestBody JSONObject jsonObject) {
		logger.info("微信支付回调开始 jsonObject:" + jsonObject.toString());
		String method = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			//登录微信商户平台，进入【账户中心 > API安全 】目录，设置APIV3密钥
			//String key = "mqr5afng3sqvhpp4rycmg2fzx2dabncq";
			String key = "hree2eu34o9m4j0soc6pstbzb8y6580h";
			String json = jsonObject.toString();
			String associated_data = (String) JSONUtil.getByPath(JSONUtil.parse(json), "resource.associated_data");
			String ciphertext = (String) JSONUtil.getByPath(JSONUtil.parse(json), "resource.ciphertext");
			String nonce = (String) JSONUtil.getByPath(JSONUtil.parse(json), "resource.nonce");
			String decryptData = new AesUtil(key.getBytes(StandardCharsets.UTF_8)).decryptToString(associated_data.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
			logger.info("验签返回数据:"+ decryptData);
			//验签成功
			JSONObject decryptDataObj = JSONObject.parseObject(decryptData, JSONObject.class);
			// 微信支付订单号
			String transactionId = decryptDataObj.getString("transaction_id");
			// 交易类型
			String tradeType = decryptDataObj.getString("trade_type");
			// 交易状态
			String tradeState = decryptDataObj.getString("trade_state");
			// 交易状态描述
			String tradeStateDesc = decryptDataObj.getString("trade_state_desc");
			// 付款银行
			String bankType = decryptDataObj.getString("bank_type");
			// 支付完成时间
			String successTime = decryptDataObj.getString("success_time");
			// 商户订单号
			String outTradeNo = decryptDataObj.getString("out_trade_no");
			List<Integer> paymentStatusList_01 = new ArrayList<>();
			paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRE);
			paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRO);
			CarPayOrderStatusVo requestOrderStatusVo_01 = new CarPayOrderStatusVo();
			requestOrderStatusVo_01.setId(outTradeNo);
			requestOrderStatusVo_01.setOrderStatusList(paymentStatusList_01);
			ParkPayOrder parkPayOrder = parkPayOrderDaoMapper.getParkPayOrder(requestOrderStatusVo_01);
			if(parkPayOrder != null){
				parkPayOrder.setTransactionId(transactionId);
				parkPayOrder.setTradeType(tradeType);
				parkPayOrder.setTradeState(tradeState);
				parkPayOrder.setTradeStateDesc(tradeStateDesc);
				parkPayOrder.setBankType(bankType);
				parkPayOrder.setSuccessTime(successTime);
				parkPayOrder.setUpdateTime(new Date());
				if("SUCCESS".equals(tradeState)){
					carPayService.updateStatusSuccess(parkPayOrder);
				}else {
					carPayService.updateStatusFail(parkPayOrder);
				}
			}

		}catch (Exception e){
			logger.info("{} ,parms{}, 异常:", method, jsonObject.toJSONString(), e);
		}

		Map<String, String> res = new HashMap<>();
		res.put("code", "SUCCESS");
		res.put("message", "成功");
		logger.info("微信支付回调结束!");
		return res;
	}

	@RequestMapping("/carpay/queryCarPayLog.do")
	@ResponseBody
	public CarPayLogVo queryCarPayLog(@RequestBody CarPayLogVo carPayLogVo) {
		PageHelper.offsetPage((carPayLogVo.getPageNum()-1) * carPayLogVo.getPageSize(),carPayLogVo.getPageSize());
		ParkPayOrder parkPayOrder = new ParkPayOrder();
		parkPayOrder.setWxOpenId(carPayLogVo.getWxOpenId());
		List<ParkPayOrder> list = parkPayOrderDaoMapper.getList(parkPayOrder);
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		for(ParkPayOrder p : list){
			if(StringUtils.isNotBlank(p.getSuccessTime())) {
				ZonedDateTime zonedDateTime = ZonedDateTime.parse(p.getSuccessTime(), formatter);
				String formatted = zonedDateTime.format(DateUtils.formatter_ymd_hms);
				p.setSuccessTime(formatted);
			}
		}
		PageInfo<ParkPayOrder> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)carPayLogVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		carPayLogVo.setPages(pageNumTotal);
		carPayLogVo.setTotalNum((int) pageInfo.getTotal());
		carPayLogVo.setPageSize(carPayLogVo.getPageSize());
		carPayLogVo.setList(list);
		carPayLogVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return carPayLogVo;
	}

	public static void main(String[] args) {
//		String stringA="appid=ym5e3ad2743739c30a&carNo=川A55D67&parkKey=m3kgkktp&rand=5.394985805&version=v1.0&";
//		String stringSignTemp = stringA+"cb5ea7750f464bd1ac3976e80d9865e0";
//		String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
//		System.out.println(sign);
		BigDecimal a = new BigDecimal("10");
		BigDecimal b = new BigDecimal("3");
		BigDecimal divide = a.divide(b, 10, RoundingMode.HALF_UP);
		BigDecimal resultCeiling = divide.setScale(0, RoundingMode.CEILING);

		System.out.println(resultCeiling);
	}

}

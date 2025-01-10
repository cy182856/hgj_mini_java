package com.ej.hgj.controller.carrenew;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.carrenew.CarRenewOrderDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayOrderTemp;
import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.service.carrenew.CarRenewService;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.bill.*;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.*;
import com.ej.hgj.vo.carrenew.CarRenewInfoVo;
import com.ej.hgj.vo.carrenew.CarRenewOrderStatusVo;
import com.ej.hgj.vo.carrenew.CarRenewRequestVo;
import com.ej.hgj.vo.carrenew.CarRenewResponseVo;
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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
public class CarRenewController extends BaseController {

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
	private CarRenewOrderDaoMapper carRenewOrderDaoMapper;

	@Autowired
	private CarRenewService carRenewService;

	/**
	 * 车牌号获取月租车信息
	 * @param carRenewRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/carrenew/queryCarInfoByCarNum")
	public CarRenewResponseVo queryCarInfoCarNum(@RequestBody CarRenewRequestVo carRenewRequestVo) {
		CarRenewResponseVo carRenewResponseVo = new CarRenewResponseVo();
		String carCode = carRenewRequestVo.getCarCode();
		// 调用车牌号查询停车订单费用接口
		ConstantConfig zhtc_api_url = constantConfDaoMapper.getByKey(Constant.ZHTC_API_URL);
		String apiUrl = zhtc_api_url.getConfigValue();
		ConstantConfig zhtc_api_app_id = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_ID);
		String appId = zhtc_api_app_id.getConfigValue();
		ConstantConfig zhtc_api_app_secret = constantConfDaoMapper.getByKey(Constant.ZHTC_API_APP_SECRET);
		String appSecret = zhtc_api_app_secret.getConfigValue();
		ConstantConfig zhtc_api_auth_code = constantConfDaoMapper.getByKey(Constant.ZHTC_API_AUTH_CODE);
		ConstantConfig zhtc_month_cost = constantConfDaoMapper.getByKey(Constant.ZHTC_MONTH_COST);
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
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inquire/GetMthCarInfo", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");
			// 成功
			if("1".equals(code)){
				JSONObject jsonData = resultJson.getJSONObject("data");
				String carTypeNo = jsonData.getString("carTypeNo");
				String beginTime = jsonData.getString("beginTime");
				String endTime = jsonData.getString("endTime");
				String userName = jsonData.getString("userName");
				String phone = jsonData.getString("phone");
				String homeAddress = jsonData.getString("homeAddress");
				// 查询月租车信息
				CarRenewInfoVo carRenewInfoVo = new CarRenewInfoVo();
				carRenewInfoVo.setCarCode(carCode);
				carRenewInfoVo.setCarTypeNo(carTypeNo);
				carRenewInfoVo.setBeginTime(beginTime);
				carRenewInfoVo.setEndTime(endTime);
				carRenewInfoVo.setUserName(userName);
				carRenewInfoVo.setPhone(phone);
				carRenewInfoVo.setHomeAddress(homeAddress);
				carRenewInfoVo.setMonthAmount(zhtc_month_cost.getConfigValue());
				// 返回数据
				carRenewResponseVo.setCarRenewInfoVo(carRenewInfoVo);
				carRenewResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
				carRenewResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
				carRenewResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
				return carRenewResponseVo;
			}else {
				carRenewResponseVo.setRespCode("999");
				carRenewResponseVo.setErrDesc(msg);
				return carRenewResponseVo;
			}
		}catch (Exception e){
			e.printStackTrace();
			carRenewResponseVo.setRespCode("999");
			carRenewResponseVo.setErrDesc(e.toString());
			logger.info("------车牌号查询失败----------");
		}
		return carRenewResponseVo;
	}

	/**
	 * 车辆续费
	 * @param carRenewRequestVo
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/carrenew/carRenew.do")
	public CarRenewResponseVo carRenew(@RequestBody CarRenewRequestVo carRenewRequestVo) throws Exception {
		CarRenewResponseVo carRenewResponseVo = new CarRenewResponseVo();
		String cstCode = carRenewRequestVo.getCstCode();
		String wxOpenId = carRenewRequestVo.getWxOpenId();
		String proNum = carRenewRequestVo.getProNum();
		BigDecimal payAmount = carRenewRequestVo.getPayAmount();
		Integer monthNum = carRenewRequestVo.getMonthNum();
		String carCode = carRenewRequestVo.getCarCode();
		// 参数校验
		if(StringUtils.isBlank(cstCode)){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespDesc());
			return carRenewResponseVo;
		}
		if(StringUtils.isBlank(wxOpenId)){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespDesc());
			return carRenewResponseVo;
		}
		if(StringUtils.isBlank(proNum)){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespDesc());
			return carRenewResponseVo;
		}
		if(StringUtils.isBlank(carCode)){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.CAR_PAY_CAR_CODE_NULL.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.CAR_PAY_CAR_CODE_NULL.getRespDesc());
			return carRenewResponseVo;
		}
		if(monthNum == null){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.CAR_RENEW_MONTH_NUM_NULL.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.CAR_RENEW_MONTH_NUM_NULL.getRespDesc());
			return carRenewResponseVo;
		}
		if(payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespDesc());
			return carRenewResponseVo;
		}
		// 订单金额校验
		ConstantConfig zhtc_month_cost = constantConfDaoMapper.getByKey(Constant.ZHTC_MONTH_COST);
		String monthCost = zhtc_month_cost.getConfigValue();
		BigDecimal cost = new BigDecimal(monthCost);
		if((cost.multiply(new BigDecimal(monthNum)).subtract(payAmount)).compareTo(BigDecimal.ZERO) != 0 ){
			carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carRenewResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespCode());
			carRenewResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespDesc());
			return carRenewResponseVo;
		}
		try {
			// 调用车牌号查询月租车信息接口
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
			JSONObject resultJson = HttpClientUtil.sendPost(apiUrl + "/Inquire/GetMthCarInfo", pramJson);
			String code = resultJson.get("code").toString();
			String msg = resultJson.getString("msg");
			// 订单号
			String orderId = TimestampGenerator.generateSerialNumber();
			Date sysDate = new Date();
			int intTotalAmount = 0;
			// 1-成功 0-失败
			if ("1".equals(code)) {
				JSONObject jsonData = resultJson.getJSONObject("data");
				String carTypeNo = jsonData.getString("carTypeNo");
				String beginTime = jsonData.getString("beginTime");
				String endTime = jsonData.getString("endTime");
				String userName = jsonData.getString("userName");
				String phone = jsonData.getString("phone");
				String homeAddress = jsonData.getString("homeAddress");
				// 解析日期-开始日期
				LocalDate dateBeginTime = LocalDate.parse(beginTime, DateUtils.formatter_ymd_hms);
				// 解析日期-结束日期
				LocalDate dateEndTime = LocalDate.parse(endTime, DateUtils.formatter_ymd_hms);
				// 根据车位有效截止日期计算下一个月的日期，续费开始时间
				String renewBeginTime = dateBeginTime.format(DateUtils.formatter_ymd).substring(0,7) + "-01" + " 00:00:00";
				// 根据车位有效截止日期与续费月数计算续费截止时间
				String renewEndTime = dateEndTime.plusMonths(monthNum).format(DateUtils.formatter_ymd).substring(0,7);
				String[] renewEndTimes = renewEndTime.split("-");
				YearMonth yearMonth = YearMonth.of(Integer.valueOf(renewEndTimes[0]), Integer.valueOf(renewEndTimes[1]));
				LocalDate lastDay = yearMonth.atEndOfMonth();
				renewEndTime = lastDay + " 23:59:59";
				// 创建微信支付订单
				CarRenewOrder carRenewOrder = new CarRenewOrder();
				carRenewOrder.setId(orderId);
				carRenewOrder.setProNum(proNum);
				carRenewOrder.setCarCode(carCode);
				carRenewOrder.setCarTypeNo(carTypeNo);
				carRenewOrder.setWxOpenId(wxOpenId);
				carRenewOrder.setCstCode(cstCode);
				HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
				carRenewOrder.setCstName(hgjCst.getCstName());
				carRenewOrder.setPayAmount(payAmount);
				BigDecimal multiply = payAmount.multiply(new BigDecimal("100"));
				intTotalAmount = multiply.intValue();
				carRenewOrder.setAmountTotal(intTotalAmount);
				carRenewOrder.setIpItemName(orderId);
				carRenewOrder.setBeginTime(beginTime);
				carRenewOrder.setEndTime(endTime);
				carRenewOrder.setMonthNum(monthNum);
				carRenewOrder.setRenewBeginTime(renewBeginTime);
				carRenewOrder.setRenewEndTime(renewEndTime);
				carRenewOrder.setPhone(phone);
				carRenewOrder.setUserName(userName);
				carRenewOrder.setHomeAddress(homeAddress);
				carRenewOrder.setOrderStatus(0);
				carRenewOrder.setCreateTime(sysDate);
				carRenewOrder.setUpdateTime(sysDate);
				carRenewOrder.setDeleteFlag(Constant.DELETE_FLAG_NOT);
				carRenewOrderDaoMapper.save(carRenewOrder);
				logger.info("缴费订单号:" + orderId + "||缴费金额:" + payAmount);
			} else {
				carRenewResponseVo.setRespCode("999");
				carRenewResponseVo.setErrDesc(msg);
				return carRenewResponseVo;
			}

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
					Constant.CARRENEW_CALLBACK_URL, intTotalAmount, wxOpenId);
			HttpUrl httpurl = HttpUrl.parse(prePayUrl);
			// 获取证书token
			String authorization = carRenewService.getToken("POST", httpurl, params, signInfo, proNum);
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
				carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
				carRenewResponseVo.setErrCode(jsonObject.getString("code"));
				carRenewResponseVo.setErrDesc(jsonObject.getString("message"));
				logger.info("下单失败单号:" + orderId + "||message:" + jsonObject.getString("message"));
				return carRenewResponseVo;
			}

			// 生成签名
			String paySign = getPaySign(signInfo, sinPrepayId, proNum);
			if (StringUtils.isNotBlank(paySign)) {
				logger.info("生成签名成功返回paySign:" + paySign + "||单号:" + orderId);
				signInfo.setPaySign(paySign);
			} else {
				carRenewResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
				carRenewResponseVo.setErrCode(JiasvBasicRespCode.SIGNATURE_FAILED.getRespCode());
				carRenewResponseVo.setErrDesc(JiasvBasicRespCode.SIGNATURE_FAILED.getRespDesc());
				logger.info("生成签名失败单号:" + orderId);
				return carRenewResponseVo;
			}
			carRenewResponseVo.setOrderId(orderId);
			carRenewResponseVo.setSignInfoVo(signInfo);

		}catch(Exception e){
			e.printStackTrace();
			carRenewResponseVo.setErrCode("999");
			carRenewResponseVo.setErrDesc(e.toString());
			return carRenewResponseVo;
		}

		carRenewResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		carRenewResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		carRenewResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return carRenewResponseVo;
	}

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
	@RequestMapping("/carRenewOrderStatusUpdate.do")
	public BaseRespVo carRenewOrderStatusUpdate(@RequestBody CarRenewOrderStatusVo carRenewOrderStatusVo) throws Exception {
		BaseRespVo baseRespVo = new BaseRespVo();
		String orderNo = carRenewOrderStatusVo.getId();
		// 根据订单号查询支付记录表预支付成功的订单，修改为支付中
		List<Integer> orderStatusList_0 = new ArrayList<>();
		orderStatusList_0.add(Constant.ORDER_STATUS_PRE);
		CarRenewOrderStatusVo carRenewOrderStatusVoo_0 = new CarRenewOrderStatusVo();
		carRenewOrderStatusVoo_0.setId(orderNo);
		carRenewOrderStatusVoo_0.setOrderStatusList(orderStatusList_0);
		CarRenewOrder carRenewOrder_0 = carRenewOrderDaoMapper.getCarRenewOrder(carRenewOrderStatusVoo_0);
		if(carRenewOrder_0 != null){
			// 更新支付订单 为支付中
			carRenewService.updateStatusPro(carRenewOrder_0);
		}
		baseRespVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		baseRespVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		baseRespVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		logger.info("订单orderNo:" + orderNo + "支付状态修改为支付中成功");
		return baseRespVo;
	}

	@PostMapping(value = "/carRenew/callBack")
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
			CarRenewOrderStatusVo carRenewOrderStatusVo_01 = new CarRenewOrderStatusVo();
			carRenewOrderStatusVo_01.setId(outTradeNo);
			carRenewOrderStatusVo_01.setOrderStatusList(paymentStatusList_01);
			CarRenewOrder carRenewOrder = carRenewOrderDaoMapper.getCarRenewOrder(carRenewOrderStatusVo_01);
			if(carRenewOrder != null){
				carRenewOrder.setTransactionId(transactionId);
				carRenewOrder.setTradeType(tradeType);
				carRenewOrder.setTradeState(tradeState);
				carRenewOrder.setTradeStateDesc(tradeStateDesc);
				carRenewOrder.setBankType(bankType);
				carRenewOrder.setSuccessTime(successTime);
				carRenewOrder.setUpdateTime(new Date());
				if("SUCCESS".equals(tradeState)){
					carRenewService.updateStatusSuccess(carRenewOrder);
				}else {
					carRenewService.updateStatusFail(carRenewOrder);
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


	public static void main(String[] args) {
		String stringA="appid=ym5e3ad2743739c30a&carNo=川A55D67&parkKey=m3kgkktp&rand=5.394985805&version=v1.0&";
		String stringSignTemp = stringA+"cb5ea7750f464bd1ac3976e80d9865e0";
		String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
		System.out.println(sign);
	}

}

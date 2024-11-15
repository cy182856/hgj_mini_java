package com.ej.hgj.controller.carpay;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayRecordDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.entity.bill.PaymentRecord;
import com.ej.hgj.entity.card.CardCst;
import com.ej.hgj.entity.carpay.ParkPayOrder;
import com.ej.hgj.entity.carpay.ParkPayRecord;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.bill.HttpRequestUtils;
import com.ej.hgj.utils.bill.MyPrivatekey;
import com.ej.hgj.utils.bill.RandomStringUtils;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.BillResponseVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carpay.CarInfoVo;
import com.ej.hgj.vo.carpay.CarPayRequestVo;
import com.ej.hgj.vo.carpay.CarPayResponseVo;
import com.ej.hgj.vo.carpay.ParkCardVo;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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
	private ParkPayRecordDaoMapper parkPayRecordDaoMapper;

	/**
	 * 车牌号查询
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/carpay/queryCarNum")
	public CarPayResponseVo queryCarNum(@RequestBody CarPayRequestVo carPayRequestVo) {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String wxOpenId = carPayRequestVo.getWxOpenId();
		String carCode = carPayRequestVo.getCarCode();

		// 查询车辆欠费信息
		Boolean payFeeStatus = false;
		BigDecimal payFee = new BigDecimal(10);
		if(payFee.compareTo(BigDecimal.ZERO) > 0){
			payFeeStatus = true;
		}
		carPayResponseVo.setPayFeeStatus(payFeeStatus);
		carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return carPayResponseVo;
	}


	/**
	 * 车辆缴费信息查询
	 * @param carPayRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/carpay/queryCarPayInfo")
	public CarPayResponseVo queryCarPayInfo(@RequestBody CarPayRequestVo carPayRequestVo) {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String proNum = carPayRequestVo.getProNum();
		String cstCode = carPayRequestVo.getCstCode();
		String wxOpenId = carPayRequestVo.getWxOpenId();
		String carCode = carPayRequestVo.getCarCode();

		String expDate = DateUtils.strYm(new Date());

		// 查询车辆信息
		CarInfoVo carInfoVo = new CarInfoVo();
		carInfoVo.setCarCode("沪A898888");
		carInfoVo.setInParkTime("2024-10-23 08:08:08");
		carInfoVo.setStopCarTime("10小时34分");
		carInfoVo.setTotalAmount(new BigDecimal("0.5"));
		// 查询当月停车卡信息
		CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, "2", expDate);
		ParkCardVo parkCardVo = new ParkCardVo();
		parkCardVo.setCardCstBatchId(cardInfo.getCardCstBatchId());
		Integer expNum = cardInfo.getTotalNum() - cardInfo.getApplyNum();
		parkCardVo.setExpNum(expNum);
		parkCardVo.setCardAmount(new BigDecimal(0.1));
		// 创建缴费订单
		carPayResponseVo.setCarInfoVo(carInfoVo);
		carPayResponseVo.setParkCardVo(parkCardVo);
		carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return carPayResponseVo;
	}

	/**
	 * 缴费
	 */
	@ResponseBody
	@RequestMapping("/carpay/carPayment.do")
	public CarPayResponseVo carPayment(@RequestBody CarPayRequestVo carPayRequestVo) throws Exception {
		CarPayResponseVo carPayResponseVo = new CarPayResponseVo();
		String cstCode = carPayRequestVo.getCstCode();
		String wxOpenId = carPayRequestVo.getWxOpenId();
		String proNum = carPayRequestVo.getProNum();
		String cardCstBatchId = carPayRequestVo.getCardCstBatchId();
		BigDecimal priRev = carPayRequestVo.getPriRev();
		String carCode = carPayRequestVo.getCarCode();
		Boolean radioChecked = carPayRequestVo.getRadioChecked();
		BigDecimal cardAmount = carPayRequestVo.getCardAmount();
		String orderId = carPayRequestVo.getOrderId();
		if(radioChecked == true){
			// 应收金额=总金额-停车卡金额
			BigDecimal actAmount = priRev.subtract(cardAmount);
			priRev = actAmount;
		}
		BigDecimal multiply = priRev.multiply(new BigDecimal("100"));
		int intTotalAmount = multiply.intValue();

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

		if(priRev == null || priRev.compareTo(BigDecimal.ZERO) <= 0 ){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespDesc());
			return carPayResponseVo;
		}

		// 预支付交易会话标识,该值有效期为2小时
		String prepayId = null;
		// 预下单成功返回，签名用
		String sinPrepayId = null;
		logger.info("缴费订单号:" + orderId + "||缴费金额:" + priRev);

		// 查询预支付成功订单
		CarPayRequestVo carPay = new CarPayRequestVo();
		carPay.setOrderId(orderId);
		carPay.setOrderStatus(0);
		ParkPayRecord parkPayRecord = parkPayRecordDaoMapper.getParkPayRecord(carPay);
		if(parkPayRecord != null){
			prepayId = parkPayRecord.getPrepayId();
			orderId = parkPayRecord.getOrderNo();
		}else {
			// 创建支付订单
			ParkPayOrder parkPayOrder = new ParkPayOrder();
			orderId = TimestampGenerator.generateSerialNumber();
			parkPayOrder.setId(orderId);
			parkPayOrder.setProNum(proNum);
			parkPayOrder.setCardCstBatchId(cardCstBatchId);
			parkPayOrder.setCarCode(carCode);
			parkPayOrder.setWxOpenId(wxOpenId);
			parkPayOrder.setCstCode(cstCode);
			parkPayOrder.setPriRev(priRev);
			parkPayOrder.setPriPaid(priRev);
			parkPayOrder.setAmountTotal(intTotalAmount);
			parkPayOrder.setIpItemName(orderId);
			parkPayOrder.setOrderStatus(0);
			parkPayOrder.setCreateTime(new Date());
			parkPayOrder.setUpdateTime(new Date());
			parkPayOrder.setDeleteFlag(Constant.DELETE_FLAG_NOT);
			parkPayOrderDaoMapper.save(parkPayOrder);
		}

		if(StringUtils.isBlank(orderId)){
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
			return carPayResponseVo;
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
		signInfo.setTimeStamp(String.valueOf(System.currentTimeMillis()/1000));
		// 随机串
		signInfo.setNonceStr(RandomStringUtils.getRandomStringByLength(32));
		// 签名方式
		signInfo.setSignType("RSA");

		if(StringUtils.isNotBlank(prepayId)){
			// 两小时内未支付订单不要重新下单，直接用原有的prepayId
			signInfo.setRepayId("prepay_id=" + prepayId);
		}else {
			// JSAPI下单-获取prepay_id

			// --------------------根据项目号选择商户号--------------
			// 服务商模式-东方渔人码头
			if("10000".equals(proNum)){
				// 服务商户号-宜悦
				ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
				signInfo.setSpMchId(spMchId.getConfigValue());
				// 子服务商户号-东方渔人码头
				ConstantConfig subMchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.SUB_MCH_ID);
				signInfo.setSubMchId(subMchId.getConfigValue());
				// 服务商模式-大连路项目
			}else if("10001".equals(proNum)){
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
					Constant.CALLBACK_URL, intTotalAmount, wxOpenId);
			HttpUrl httpurl = HttpUrl.parse(prePayUrl);
			// 获取证书token
			String authorization = carPayService.getToken("POST", httpurl, params, signInfo, proNum);
			// JSAPI下单
			authorization =  "WECHATPAY2-SHA256-RSA2048" + " " + authorization;
			String result = HttpRequestUtils.post(prePayUrl, params, authorization);
			// 下单返回值转json
			JSONObject jsonObject = JSONObject.parseObject(result);
			String prepay_id = jsonObject.getString("prepay_id");
			sinPrepayId = prepay_id;
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
		}
		// 生成签名
		String paySign = getPaySign(signInfo,sinPrepayId, proNum);
		if(StringUtils.isNotBlank(paySign)){
			logger.info("生成签名成功返回paySign:" + paySign + "||单号:" + orderId);
			signInfo.setPaySign(paySign);
		}else {
			carPayResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			carPayResponseVo.setErrCode(JiasvBasicRespCode.SIGNATURE_FAILED.getRespCode());
			carPayResponseVo.setErrDesc(JiasvBasicRespCode.SIGNATURE_FAILED.getRespDesc());
			logger.info("生成签名失败单号:" + orderId);
			return carPayResponseVo;
		}
		carPayResponseVo.setOrderId(orderId);
		carPayResponseVo.setSignInfoVo(signInfo);
		carPayResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		carPayResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		carPayResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		// 插入缴费记录 - 没有预下单或者预下单超出两个小时
		if(StringUtils.isBlank(prepayId)) {
			savePayment(carPayRequestVo, orderId, sinPrepayId, priRev, intTotalAmount);
		}
		return carPayResponseVo;
	}

	public void savePayment(CarPayRequestVo carPayRequestVo, String orderId, String sinPrepayId, BigDecimal priPev, int intTotalAmount){
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(carPayRequestVo.getCstCode());
		ParkPayRecord parkPayRecord = new ParkPayRecord();
		parkPayRecord.setId(TimestampGenerator.generateSerialNumber());
		parkPayRecord.setOrderNo(orderId);
		parkPayRecord.setPrepayId(sinPrepayId);
		parkPayRecord.setProNum(carPayRequestVo.getProNum());
		parkPayRecord.setWxOpenId(carPayRequestVo.getWxOpenId());
		parkPayRecord.setCstCode(carPayRequestVo.getCstCode());
		parkPayRecord.setCstName(hgjCst.getCstName());
		parkPayRecord.setAmountTotal(intTotalAmount);
		parkPayRecord.setPriRev(priPev);
		parkPayRecord.setPriPaid(priPev);
		parkPayRecord.setIpItemName(orderId);
		parkPayRecord.setPaymentStatus(Constant.PAYMENT_STATUS_PRE);
		parkPayRecord.setCreateTime(new Date());
		parkPayRecord.setUpdateTime(new Date());
		parkPayRecord.setDeleteFlag(0);
		parkPayRecordDaoMapper.save(parkPayRecord);
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

}

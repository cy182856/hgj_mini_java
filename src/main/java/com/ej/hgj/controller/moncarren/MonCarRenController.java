package com.ej.hgj.controller.moncarren;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderDaoMapper;
import com.ej.hgj.dao.carpay.ParkPayOrderTempDaoMapper;
import com.ej.hgj.dao.carrenew.CarRenewOrderDaoMapper;
import com.ej.hgj.dao.carrenew.CarTypeDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.moncarren.MonCarRenInvoiceDaoMapper;
import com.ej.hgj.dao.moncarren.MonCarRenOrderDaoMapper;
import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.electricity.Electricity;
import com.ej.hgj.entity.moncarren.CompanyInfo;
import com.ej.hgj.entity.moncarren.InvoiceInfo;
import com.ej.hgj.entity.moncarren.MonCarRenInvoice;
import com.ej.hgj.entity.moncarren.MonCarRenOrder;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.carpay.CarPayService;
import com.ej.hgj.service.carrenew.CarRenewService;
import com.ej.hgj.service.moncarren.MonCarRenService;
import com.ej.hgj.utils.AESUtils;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.SecurityUtil;
import com.ej.hgj.utils.bill.*;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.ej.hgj.vo.carrenew.*;
import com.ej.hgj.vo.moncarren.*;
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
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class MonCarRenController extends BaseController {

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
	private MonCarRenService monCarRenService;

	@Autowired
	private CarTypeDaoMapper carTypeDaoMapper;

	@Autowired
	private MonCarRenOrderDaoMapper monCarRenOrderDaoMapper;

	@Autowired
	private MonCarRenInvoiceDaoMapper monCarRenInvoiceDaoMapper;
	/**
	 * 车牌号获取月租车信息
	 * @param monCarRenInfoVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/queryMonCarInfoByCarNum")
	public MonCarRenResponseVo queryMonCarInfoByCarNum(@RequestBody MonCarRenInfoVo monCarRenInfoVo) {
		MonCarRenResponseVo monCarRenResponseVo = new MonCarRenResponseVo();
		String carCode = monCarRenInfoVo.getCarCode();
		if(StringUtils.isBlank(carCode)){
			monCarRenResponseVo.setRespCode("999");
			monCarRenResponseVo.setErrDesc("车牌号不能为空");
			return monCarRenResponseVo;
		}
		try{
			// 调用长期车查询接口
			ConstantConfig lftc_api_url = constantConfDaoMapper.getByKey(Constant.LFTC_API_URL);
			ConstantConfig lftc_api_key = constantConfDaoMapper.getByKey(Constant.LFTC_API_KEY);
			Key k = AESUtils.toKey(org.apache.commons.codec.binary.Base64.decodeBase64(lftc_api_key.getConfigValue()));
			String data = "{\"carCode\":\"" + carCode + "\"}";
			byte[] encryptData = AESUtils.encrypt(data.getBytes("utf-8"), k);
			String encryptStr = AESUtils.parseByte2HexStr(encryptData);
			String resultData = HttpClientUtil.sendPostParking(lftc_api_url.getConfigValue() + "/GetLongUserInfo", encryptStr);
			byte[] decryptData = AESUtils.decrypt(AESUtils.parseHexStr2Byte(resultData), k);
			String strData = new String(decryptData, "utf-8");
			JSONObject jsonResult = JSONObject.parseObject(strData);
			String resCode = jsonResult.getString("resCode");
			String resMsg = jsonResult.getString("resMsg");
			// 成功
			if("0".equals(resCode)){
				String beginTime = jsonResult.getString("beginTime");
				String endTime = jsonResult.getString("endTime");
				String userName = jsonResult.getString("userName");
				String userTel = jsonResult.getString("userTel");
				String userAddress = jsonResult.getString("userAddress");
				String rule = jsonResult.getString("rule");
				JSONObject ruleJson = JSONObject.parseObject(rule);
				String ruleID = ruleJson.getString("ruleID");
				String ruleName = ruleJson.getString("ruleName");
				String ruleType = ruleJson.getString("ruleType");
				String ruleCount = ruleJson.getString("ruleCount");
				String ruleAmount = ruleJson.getString("ruleAmount");
				// 分转换为元
				Integer ruleAmountInt = Integer.valueOf(ruleAmount)/100;
				BigDecimal monthAmount = new BigDecimal(ruleAmountInt);
				// todo 测试用
				//monthAmount = new BigDecimal("0.01");

				if(!"2".equals(ruleType)){
					monCarRenResponseVo.setRespCode("999");
					monCarRenResponseVo.setErrDesc("按期类型不符合");
					return monCarRenResponseVo;
				}
				if("0".equals(ruleAmount)){
					monCarRenResponseVo.setRespCode("999");
					monCarRenResponseVo.setErrDesc("非客户车辆，请联系客服续费");
					return monCarRenResponseVo;
				}
				// 月租车信息
				MonCarRenInfoVo monCarRenInfo = new MonCarRenInfoVo();
				monCarRenInfo.setCarCode(carCode);
				monCarRenInfo.setBeginTime(beginTime);
				monCarRenInfo.setEndTime(endTime);
				monCarRenInfo.setUserName(userName);
				monCarRenInfo.setUserTel(userTel);
				monCarRenInfo.setUserAddress(userAddress);
				monCarRenInfo.setRuleID(ruleID);
				monCarRenInfo.setRuleName(ruleName);
				monCarRenInfo.setRuleType(Integer.valueOf(ruleType));
				monCarRenInfo.setRuleCount(Integer.valueOf(ruleCount));
				monCarRenInfo.setMonthAmount(monthAmount);
				// 返回数据
				monCarRenResponseVo.setMonCarRenInfoVo(monCarRenInfo);
				monCarRenResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
				monCarRenResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
				monCarRenResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
				return monCarRenResponseVo;
			}else {
				monCarRenResponseVo.setRespCode("999");
				monCarRenResponseVo.setErrDesc(resMsg);
				return monCarRenResponseVo;
			}
		}catch (Exception e){
			e.printStackTrace();
			monCarRenResponseVo.setRespCode("999");
			monCarRenResponseVo.setErrDesc(e.toString());
			logger.info("------车牌号查询失败----------");
		}
		return monCarRenResponseVo;
	}

	/**
	 * 根据选择月份计算开始日期、结束日期
	 * @param monCarRenRequestVo
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/queryMonCarRenDate.do")
	public JSONObject queryMonCarRenDate(@RequestBody MonCarRenRequestVo monCarRenRequestVo){
		JSONObject jsonObject = new JSONObject();
		// 解析日期-结束日期
		LocalDate dateEndTime = LocalDate.parse(monCarRenRequestVo.getEndTime(), DateUtils.formatter_ymd_hms);
		// 续费开始时间，结束日期加一天
		LocalDate dateNextDay = dateEndTime.plusDays(1);
		// 续费开始日期拼接时分秒
		String renewBeginTime = dateNextDay.format(DateUtils.formatter_ymd) + " 00:00:00";
		// 续费结束时间，根据dateNextDay加monthNum月
		LocalDate localDateEndTime = dateNextDay.plusMonths(monCarRenRequestVo.getMonthNum());
		// 续费结束日期减一天
		LocalDate dateBeforeDay = localDateEndTime.minusDays(1);
		// 续费结束日期拼接时分秒
		String renewEndTime = dateBeforeDay.format(DateUtils.formatter_ymd) + " 23:59:59";
		jsonObject.put("respCode", "000");
		jsonObject.put("renewBeginTime",renewBeginTime);
		jsonObject.put("renewEndTime",renewEndTime);
		return jsonObject;
	}

	/**
	 * 前端下拉框续费月数数组
	 * @param monCarRenRequestVo
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/queryRenewMonthArray.do")
	public JSONObject queryRenewMonthArray(@RequestBody MonCarRenRequestVo monCarRenRequestVo){
		JSONObject jsonObject = new JSONObject();
		ConstantConfig byProNumAndKey = constantConfDaoMapper.getByProNumAndKey(monCarRenRequestVo.getProNum(), Constant.MON_CAR_REN_NUM);
		String monthNum = byProNumAndKey.getConfigValue();
		String[] renewMonthArray = monthNum.split(",");
		jsonObject.put("respCode", "000");
		jsonObject.put("renewMonthArray",renewMonthArray);
		return jsonObject;
	}

	/**
	 * 开票验证
	 * @param monCarRenRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/monCarInvoiceCheck.do")
	public JSONObject monCarInvoiceCheck(@RequestBody MonCarRenRequestVo monCarRenRequestVo){
		JSONObject jsonObject = new JSONObject();
		// 查询订单信息
		MonCarRenOrder monCarRenOrder = monCarRenOrderDaoMapper.getById(monCarRenRequestVo.getOrderId());
		if(monCarRenOrder == null){
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "订单不存在");
			return jsonObject;
		}
		if(monCarRenOrder.getOrderStatus() != 2){
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "订单状态错误");
			return jsonObject;
		}
		// 计算N个月前的日期
		ConstantConfig byProNumAndKey = constantConfDaoMapper.getByProNumAndKey(monCarRenRequestVo.getProNum(), Constant.MON_CAR_REN_INVOICE_BEFORE_MONTH);
		LocalDate creationBeforeMonth = LocalDate.now().minusMonths(Integer.valueOf(byProNumAndKey.getConfigValue()));
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(monCarRenOrder.getSuccessTime(), formatter);
		String successTime = zonedDateTime.format(DateUtils.formatter_ymd_hms);
		// 判断支付时间是否是N个月前的
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime localDateSuccessTime = LocalDateTime.parse(successTime, dateTimeFormatter);
		LocalDate paymentTime = localDateSuccessTime.toLocalDate();
		if (paymentTime.isBefore(creationBeforeMonth)) {
			// 支付时间超过N个月
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "订单已超过" + byProNumAndKey.getConfigValue() + "个月无法开票");
			return jsonObject;
		}
		jsonObject.put("respCode", "000");
		jsonObject.put("errDesc", "成功");
		return jsonObject;
	}

	/**
	 * 发票查看
	 * @param monCarRenRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/viewInvoice.do")
	public JSONObject viewInvoice(@RequestBody MonCarRenRequestVo monCarRenRequestVo){
		JSONObject jsonObject = new JSONObject();
		// 查询开票信息
		MonCarRenInvoice monCarRenInvoice = monCarRenInvoiceDaoMapper.getByOrderId(monCarRenRequestVo.getOrderId());
		if(monCarRenInvoice == null){
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "无开票信息");
			return jsonObject;
		}
		if(monCarRenInvoice.getInvoiceStatus() != 2){
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "开票状态错误");
			return jsonObject;
		}
		jsonObject.put("monCarRenInvoice", monCarRenInvoice);
		jsonObject.put("respCode", "000");
		jsonObject.put("errDesc", "成功");
		return jsonObject;
	}

	/**
	 * 根据句抬头获取单位全称及税号
	 * @param monCarRenRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/companySearch.do")
	public JSONObject companySearch(@RequestBody MonCarRenRequestVo monCarRenRequestVo){
		JSONObject jsonObject = new JSONObject();

		// 调用开票接口获取token
		String tokenUrl = "http://api.yuxtech.com/invoice/getToken";
		String tokenParam = "{\"username\":\"admin_91310110MA1G8DCX9W\",\"password\":\"ixLRI4ef\"}";
		JSONObject tokenResultJson = HttpClientUtil.sendPost(tokenUrl, tokenParam);
		String tokenResponse = tokenResultJson.getString("response");
		JSONObject jsonResponse = JSONObject.parseObject(tokenResponse);
		String access_token = jsonResponse.getString("access_token");
		// 获取当前时间戳
		long timeStamp = System.currentTimeMillis();
		// 调用云台头获取接口
		String companyUrlParam = "{\"companyName\":\"" +
				monCarRenRequestVo.getSearchText() +
				"\"}";
		// 对参数加密获取MD5签名
		String salt = "jrPrts7ovwg1uQM5";
		String sign = SecurityUtil.encodeByMD5(salt + companyUrlParam);
		String companyUrl = "http://api.yuxtech.com/invoice/bizinfoCompanySearch?token=" +
				access_token +
				"&method=bizinfoCompanySearch&timeStamp=" +
				timeStamp +
				"&appKey=1010043&version=1.0&sign=" +
				sign +
				"";
		JSONObject companyResultJson = HttpClientUtil.sendPost(companyUrl, companyUrlParam);
		boolean success = Boolean.valueOf(companyResultJson.getString("success"));
		if(success == true){
			String requestId = companyResultJson.getString("requestId");
			String result = companyResultJson.getString("result");
			List<CompanyInfo> companyInfoList = JSON.parseArray(result, CompanyInfo.class);
			jsonObject.put("companyInfoList", companyInfoList);
		}else {
			String errorResponse = companyResultJson.getString("errorResponse");
			JSONObject errorResponseJson = JSONObject.parseObject(errorResponse);
			String code = errorResponseJson.getString("code");
			String message = errorResponseJson.getString("message");
			String subCode = errorResponseJson.getString("subCode");
			String subMessage = errorResponseJson.getString("subMessage");
			jsonObject.put("respCode", code);
			jsonObject.put("errDesc", message);
			return jsonObject;
		}
		jsonObject.put("respCode", "000");
		jsonObject.put("errDesc", "成功");
		return jsonObject;
	}

	/**
	 * 开票
	 * @param monCarRenRequestVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/monCarInvoice.do")
	public JSONObject monCarInvoice(@RequestBody MonCarRenRequestVo monCarRenRequestVo){
		JSONObject jsonObject = new JSONObject();
		// 根据开票类型传参1-单位开票  2-个人开票 个人开票不需要税号
		String invoiceType = monCarRenRequestVo.getInvoiceType();
		if(StringUtils.isBlank(invoiceType)) {
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "开票类型不能为空");
			return jsonObject;
		}
		String buyerName = monCarRenRequestVo.getBuyerName();
		if(StringUtils.isBlank(buyerName)) {
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "发票抬头不能为空");
			return jsonObject;
		}
		String buyerTaxNo = monCarRenRequestVo.getBuyerTaxNo();
		if(StringUtils.isBlank(buyerTaxNo) && "1".equals(invoiceType)) {
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "税号不能为空");
			return jsonObject;
		}
		String pushEmail = monCarRenRequestVo.getPushEmail();
		if(StringUtils.isBlank(pushEmail)) {
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "邮箱不能为空");
			return jsonObject;
		}
		// 查询订单信息
		MonCarRenOrder monCarRenOrder = monCarRenOrderDaoMapper.getById(monCarRenRequestVo.getOrderId());
		if(monCarRenOrder == null){
			jsonObject.put("respCode", "999");
			jsonObject.put("errDesc", "订单不存在");
			return jsonObject;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(monCarRenOrder.getSuccessTime(), formatter);
		String successTime = zonedDateTime.format(DateUtils.formatter_ymd_hms);
		// 调用开票接口获取token
		String tokenUrl = "http://api.yuxtech.com/invoice/getToken";
		String tokenParam = "{\"username\":\"admin_913101183420070187\",\"password\":\"9SJxjgyi\"}";
		JSONObject tokenResultJson = HttpClientUtil.sendPost(tokenUrl, tokenParam);
		String tokenResponse = tokenResultJson.getString("response");
		JSONObject jsonResponse = JSONObject.parseObject(tokenResponse);
		String access_token = jsonResponse.getString("access_token");
		// 获取当前时间戳
		long timeStamp = System.currentTimeMillis();
		String invoiceParam = "";
		if("1".equals(invoiceType)) {
			// 调用开票接口
			invoiceParam = "{\"invoiceType\":\"1\",\"invoiceTypeCode\":\"02\",\"taxNo\":\"913101183420070187\",\"orderNo\":\"" +
					monCarRenOrder.getId() +
					"\",\"orderDateTime\":\"" +
					successTime +
					"\",\"invoiceSpecialMark\":\"00\",\"priceTaxMark\":\"1\",\"invoiceDetailList\":[{\"goodsCode\":\"3040801010000000000\",\"goodsName\":\"停车费\",\"goodsQuantity\":1,\"goodsPrice\":" +
					monCarRenOrder.getPayAmount() +
					",\"goodsTotalPrice\":" +
					monCarRenOrder.getPayAmount() +
					",\"goodsTaxRate\":0.06}],\"leaseInfo\":{\"leasePropertyNo\":\"沪房地杨字（2015）第009043号\",\"leaseAddress\":\"上海市&杨浦区\",\"leaseDetailAddress\":\"杨树浦路1088号，江浦路39号\",\"leaseCrossSign\":\"否\",\"leaseAreaUnit\":\"2\",\"leaseHoldDateStart\":\"2007-03-31\",\"leaseHoldDateEnd\":\"2057-03-30\"},\"buyerName\":\"" +
					buyerName +
					"\",\"buyerTaxNo\":\"" +
					buyerTaxNo +
					"\",\"pushEmail\":\"" +
					pushEmail +
					"\",\"callBackUrl\":\"" +
					Constant.MONCARREN_INVOICE_CALLBACK_URL +
					"\"}";
		}
		if("2".equals(invoiceType)) {
			// 调用开票接口
			invoiceParam = "{\"invoiceType\":\"1\",\"invoiceTypeCode\":\"02\",\"taxNo\":\"913101183420070187\",\"orderNo\":\"" +
					monCarRenOrder.getId() +
					"\",\"orderDateTime\":\"" +
					successTime +
					"\",\"invoiceSpecialMark\":\"00\",\"priceTaxMark\":\"1\",\"invoiceDetailList\":[{\"goodsCode\":\"3040801010000000000\",\"goodsName\":\"停车费\",\"goodsQuantity\":1,\"goodsPrice\":" +
					monCarRenOrder.getPayAmount() +
					",\"goodsTotalPrice\":" +
					monCarRenOrder.getPayAmount() +
					",\"goodsTaxRate\":0.06}],\"leaseInfo\":{\"leasePropertyNo\":\"沪房地杨字（2015）第009043号\",\"leaseAddress\":\"上海市&杨浦区\",\"leaseDetailAddress\":\"杨树浦路1088号，江浦路39号\",\"leaseCrossSign\":\"否\",\"leaseAreaUnit\":\"2\",\"leaseHoldDateStart\":\"2007-03-31\",\"leaseHoldDateEnd\":\"2057-03-30\"},\"buyerName\":\"" +
					buyerName +
					"\",\"pushEmail\":\"" +
					pushEmail +
					"\",\"callBackUrl\":\"" +
					Constant.MONCARREN_INVOICE_CALLBACK_URL +
					"\"}";
		}
		// 对参数加密获取MD5签名
		String salt = "oITcOlxUOW3cEfp3";
		String sign = SecurityUtil.encodeByMD5(salt + invoiceParam);
		String invoiceUrl = "http://api.yuxtech.com/invoice/xw?token=" +
				access_token +
				"&method=baiwang.s.outputinvoice.invoice&timeStamp=" +
				timeStamp +
				"&appKey=1010062&version=1.0&sign=" +
				sign +
				"";
		JSONObject invoiceResultJson = HttpClientUtil.sendPost(invoiceUrl, invoiceParam);
		boolean success = Boolean.valueOf(invoiceResultJson.getString("success"));
		if(success == true){
			String requestId = invoiceResultJson.getString("requestId");
			String invoiceResponse = invoiceResultJson.getString("response");
			JSONObject invoiceJson = JSONObject.parseObject(invoiceResponse);
			String serialNo = invoiceJson.getString("serialNo");
/**
			// 开票成功获取发票pdf链接，调用开票结果查询接口
			String pdfUrl = "";
			String resCode = "";
			String resMsg = "";
			String queryInvoiceParam = "{\"taxNo\":\"913101183420070187\",\"serialNos\":[\"" +
					serialNo +
					"\"],\"orderNos\":[\"" +
					monCarRenOrder.getId() +
					"\"],\"detailMark\":\"0\"}";
			// 对参数加密获取MD5签名
			String queryInvoiceSign = SecurityUtil.encodeByMD5(salt + queryInvoiceParam);
			String queryInvoiceUrl = "http://api.yuxtech.com/invoice/xw?token=" + access_token + "&method=baiwang.s.outputinvoice.query&appKey=1010062&sign=" + queryInvoiceSign;
			JSONObject queryInvoiceJson = HttpClientUtil.sendPost(queryInvoiceUrl, queryInvoiceParam);
			boolean queryInvoiceSuccess = Boolean.valueOf(queryInvoiceJson.getString("success"));
			if(queryInvoiceSuccess == true && queryInvoiceJson != null){
				String response = queryInvoiceJson.getString("response");
				List<InvoiceInfo> invoiceInfoList = JSON.parseArray(response, InvoiceInfo.class);
				if(!invoiceInfoList.isEmpty()){
					pdfUrl = invoiceInfoList.get(0).getPdfUrl();
				}
			}else{
				String errorResponse = queryInvoiceJson.getString("errorResponse");
				JSONObject errorResponseJson = JSONObject.parseObject(errorResponse);
				resCode = errorResponseJson.getString("code");
				resMsg = errorResponseJson.getString("message");
				jsonObject.put("respCode", resCode);
				jsonObject.put("errDesc", resMsg);
				return jsonObject;
			}
*/
			// 保存开票记录
			MonCarRenInvoice monCarRenInvoice = new MonCarRenInvoice();
			monCarRenInvoice.setId(TimestampGenerator.generateSerialNumber());
			monCarRenInvoice.setOrderId(monCarRenOrder.getId());
			monCarRenInvoice.setRequestId(requestId);
			monCarRenInvoice.setSerialNo(serialNo);
			monCarRenInvoice.setBuyerName(buyerName);
			monCarRenInvoice.setBuyerTaxNo(buyerTaxNo);
			monCarRenInvoice.setPushEmail(pushEmail);
			monCarRenInvoice.setInvoiceType(Integer.valueOf(invoiceType));
			//monCarRenInvoice.setPdfUrl(pdfUrl);
			//monCarRenInvoice.setResCode(resCode);
			//monCarRenInvoice.setResMsg(resMsg);
			monCarRenInvoice.setInvoiceStatus(1);
			monCarRenInvoice.setCreateTime(new Date());
			monCarRenInvoice.setUpdateTime(new Date());
			monCarRenInvoice.setDeleteFlag(Constant.DELETE_FLAG_NOT);
			monCarRenInvoiceDaoMapper.save(monCarRenInvoice);

		}else {
			String errorResponse = invoiceResultJson.getString("errorResponse");
			JSONObject errorResponseJson = JSONObject.parseObject(errorResponse);
			String code = errorResponseJson.getString("code");
			String message = errorResponseJson.getString("message");
			String subCode = errorResponseJson.getString("subCode");
			String subMessage = errorResponseJson.getString("subMessage");
			jsonObject.put("respCode", subCode);
			jsonObject.put("errDesc", subMessage);
			return jsonObject;
		}
		jsonObject.put("respCode", "000");
		jsonObject.put("errDesc", "成功");
		return jsonObject;
	}

	/**
	 * 月租车续费
	 * @param monCarRenRequestVo
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/monCarRen/monCarRen.do")
	public CarRenewResponseVo monCarRen(@RequestBody MonCarRenRequestVo monCarRenRequestVo) throws Exception {
		CarRenewResponseVo carRenewResponseVo = new CarRenewResponseVo();
		String cstCode = monCarRenRequestVo.getCstCode();
		String wxOpenId = monCarRenRequestVo.getWxOpenId();
		String proNum = monCarRenRequestVo.getProNum();
		BigDecimal payAmount = monCarRenRequestVo.getPayAmount();
		Integer monthNum = monCarRenRequestVo.getMonthNum();
		String carCode = monCarRenRequestVo.getCarCode();
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

		try {
			// 调用长期车查询接口
			ConstantConfig lftc_api_url = constantConfDaoMapper.getByKey(Constant.LFTC_API_URL);
			ConstantConfig lftc_api_key = constantConfDaoMapper.getByKey(Constant.LFTC_API_KEY);
			Key k = AESUtils.toKey(org.apache.commons.codec.binary.Base64.decodeBase64(lftc_api_key.getConfigValue()));
			String data = "{\"carCode\":\"" + carCode + "\"}";
			byte[] encryptData = AESUtils.encrypt(data.getBytes("utf-8"), k);
			String encryptStr = AESUtils.parseByte2HexStr(encryptData);
			String resultData = HttpClientUtil.sendPostParking(lftc_api_url.getConfigValue() + "/GetLongUserInfo", encryptStr);
			byte[] decryptData = AESUtils.decrypt(AESUtils.parseHexStr2Byte(resultData), k);
			String strData = new String(decryptData, "utf-8");
			JSONObject jsonResult = JSONObject.parseObject(strData);
			String resCode = jsonResult.getString("resCode");
			String resMsg = jsonResult.getString("resMsg");
			// 订单号
			String orderId = TimestampGenerator.generateSerialNumber();
			Date sysDate = new Date();
			int intTotalAmount = 0;
			// 0-成功 1-失败
			if ("0".equals(resCode)) {
				String carTypeNo = jsonResult.getString("carTypeNo");
				String beginTime = jsonResult.getString("beginTime");
				String endTime = jsonResult.getString("endTime");
				String userName = jsonResult.getString("userName");
				String userTel = jsonResult.getString("userTel");
				String userAddress = jsonResult.getString("userAddress");
				String rule = jsonResult.getString("rule");
				JSONObject ruleJson = JSONObject.parseObject(rule);
				String ruleID = ruleJson.getString("ruleID");
				String ruleName = ruleJson.getString("ruleName");
				String ruleType = ruleJson.getString("ruleType");
				String ruleCount = ruleJson.getString("ruleCount");
				String ruleAmount = ruleJson.getString("ruleAmount");
				// 解析日期-结束日期
				LocalDate dateEndTime = LocalDate.parse(endTime, DateUtils.formatter_ymd_hms);
				// 续费开始时间，结束日期加一天
				LocalDate dateNextDay = dateEndTime.plusDays(1);
				// 续费开始日期拼接时分秒
				String renewBeginTime = dateNextDay.format(DateUtils.formatter_ymd) + " 00:00:00";
				// 续费结束时间，根据dateNextDay加monthNum月
				LocalDate localDateEndTime = dateNextDay.plusMonths(monthNum);
				// 续费结束日期减一天
				LocalDate dateBeforeDay = localDateEndTime.minusDays(1);
				// 续费结束日期拼接时分秒
				String renewEndTime = dateBeforeDay.format(DateUtils.formatter_ymd) + " 23:59:59";
				// 创建微信支付订单
				MonCarRenOrder monCarRenOrder = new MonCarRenOrder();
				monCarRenOrder.setId(orderId);
				monCarRenOrder.setProNum(proNum);
				monCarRenOrder.setCarCode(carCode);
				monCarRenOrder.setCarTypeNo(carTypeNo);
				monCarRenOrder.setWxOpenId(wxOpenId);
				monCarRenOrder.setCstCode(cstCode);
				HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
				monCarRenOrder.setCstName(hgjCst.getCstName());
				monCarRenOrder.setPayAmount(payAmount);
				BigDecimal multiply = payAmount.multiply(new BigDecimal("100"));
				intTotalAmount = multiply.intValue();
				monCarRenOrder.setAmountTotal(intTotalAmount);
				monCarRenOrder.setIpItemName(orderId);
				monCarRenOrder.setBeginTime(beginTime);
				monCarRenOrder.setEndTime(endTime);
				monCarRenOrder.setMonthNum(monthNum);
				monCarRenOrder.setRenewBeginTime(renewBeginTime);
				monCarRenOrder.setRenewEndTime(renewEndTime);
				monCarRenOrder.setUserTel(userTel);
				monCarRenOrder.setUserName(userName);
				monCarRenOrder.setUserAddress(userAddress);
				monCarRenOrder.setRuleId(ruleID);
				monCarRenOrder.setRuleName(ruleName);
				monCarRenOrder.setRuleType(ruleType);
				monCarRenOrder.setRuleCount(ruleCount);
				monCarRenOrder.setRuleAmount(ruleAmount);
				monCarRenOrder.setOrderStatus(0);
				monCarRenOrder.setCreateTime(sysDate);
				monCarRenOrder.setUpdateTime(sysDate);
				monCarRenOrder.setDeleteFlag(Constant.DELETE_FLAG_NOT);
				monCarRenOrderDaoMapper.save(monCarRenOrder);
				logger.info("缴费订单号:" + orderId + "||缴费金额:" + payAmount);
			} else {
				carRenewResponseVo.setRespCode("999");
				carRenewResponseVo.setErrDesc(resMsg);
				return carRenewResponseVo;
			}

			// 服务商小程序appId
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
//				// 服务商户号-宜悦
//				ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
//				signInfo.setSpMchId(spMchId.getConfigValue());
//				// 子服务商户号-东方渔人码头
//				ConstantConfig subMchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.SUB_MCH_ID);
//				signInfo.setSubMchId(subMchId.getConfigValue());
				// 服务商户号-宜悦
				ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
				signInfo.setSpMchId(spMchId.getConfigValue());
				// 子服务商户号-凡享
				ConstantConfig subMchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.SUB_MCH_ID_FX);
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
					Constant.MONCARREN_CALLBACK_URL, intTotalAmount, wxOpenId);
			HttpUrl httpurl = HttpUrl.parse(prePayUrl);
			// 获取证书token
			String authorization = monCarRenService.getToken("POST", httpurl, params, signInfo, proNum);
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
				//sign.initSign(MyPrivatekey.getPrivateKey(privateKey));
				sign.initSign(MyPrivatekey.getPrivateKey(privateKeyYy));
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
	@RequestMapping("/monCarRenOrderStatusUpdate.do")
	public BaseRespVo carRenewOrderStatusUpdate(@RequestBody MonCarRenOrderStatusVo monCarRenOrderStatusVo) throws Exception {
		BaseRespVo baseRespVo = new BaseRespVo();
		String orderNo = monCarRenOrderStatusVo.getId();
		// 根据订单号查询支付记录表预支付成功的订单，修改为支付中
		List<Integer> orderStatusList_0 = new ArrayList<>();
		orderStatusList_0.add(Constant.ORDER_STATUS_PRE);
		MonCarRenOrderStatusVo monCarRenOrderStatusVo_0 = new MonCarRenOrderStatusVo();
		monCarRenOrderStatusVo_0.setId(orderNo);
		monCarRenOrderStatusVo_0.setOrderStatusList(orderStatusList_0);
		MonCarRenOrder monCarRenOrder_0 = monCarRenOrderDaoMapper.getMonCarRenOrder(monCarRenOrderStatusVo_0);
		if(monCarRenOrder_0 != null){
			// 更新支付订单 为支付中
			monCarRenService.updateStatusPro(monCarRenOrder_0);
		}
		baseRespVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		baseRespVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		baseRespVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		logger.info("订单orderNo:" + orderNo + "支付状态修改为支付中成功");
		return baseRespVo;
	}

	@PostMapping(value = "/monCarRen/callBack")
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
			MonCarRenOrderStatusVo monCarRenOrderStatusVo_01 = new MonCarRenOrderStatusVo();
			monCarRenOrderStatusVo_01.setId(outTradeNo);
			monCarRenOrderStatusVo_01.setOrderStatusList(paymentStatusList_01);
			MonCarRenOrder monCarRenOrder = monCarRenOrderDaoMapper.getMonCarRenOrder(monCarRenOrderStatusVo_01);
			if(monCarRenOrder != null){
				monCarRenOrder.setTransactionId(transactionId);
				monCarRenOrder.setTradeType(tradeType);
				monCarRenOrder.setTradeState(tradeState);
				monCarRenOrder.setTradeStateDesc(tradeStateDesc);
				monCarRenOrder.setBankType(bankType);
				monCarRenOrder.setSuccessTime(successTime);
				monCarRenOrder.setUpdateTime(new Date());
				if("SUCCESS".equals(tradeState)){
					monCarRenService.updateStatusSuccess(monCarRenOrder);
				}else {
					monCarRenService.updateStatusFail(monCarRenOrder);
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

	@PostMapping(value = "/monCarRen/invoice/callBack")
	@ResponseBody
	public void invoiceCallBack(@RequestBody JSONObject jsonObject) {
		logger.info("发票开具回调返回数据：" + jsonObject);
		String data = jsonObject.getString("data");
		JSONObject dataJson = JSONObject.parseObject(data);
		String orderNo = dataJson.getString("orderNo");
		String pdfUrl = dataJson.getString("pdfUrl");
		String status = dataJson.getString("status");
		String statusMessage = dataJson.getString("statusMessage");
		MonCarRenInvoice monCarRenInvoice = new MonCarRenInvoice();
		monCarRenInvoice.setOrderId(orderNo);
		monCarRenInvoice.setPdfUrl(pdfUrl);
		monCarRenInvoice.setResCode(status);
		monCarRenInvoice.setResMsg(statusMessage);
		if("01".equals(status)) {
			monCarRenInvoice.setInvoiceStatus(2);
		}
		monCarRenInvoice.setUpdateTime(new Date());
		monCarRenInvoiceDaoMapper.update(monCarRenInvoice);
	}

	@RequestMapping("/monCarRen/queryMonCarRenLog.do")
	@ResponseBody
	public MonCarRenLogVo queryMonCarRenLog(@RequestBody MonCarRenLogVo monCarRenLogVo) {
		PageHelper.offsetPage((monCarRenLogVo.getPageNum()-1) * monCarRenLogVo.getPageSize(),monCarRenLogVo.getPageSize());
		MonCarRenOrder monCarRenOrder = new MonCarRenOrder();
		monCarRenOrder.setWxOpenId(monCarRenLogVo.getWxOpenId());
		List<MonCarRenOrder> list = monCarRenOrderDaoMapper.getList(monCarRenOrder);
		// 计算N个月前的日期
		ConstantConfig byProNumAndKey = constantConfDaoMapper.getByProNumAndKey(monCarRenLogVo.getProNum(), Constant.MON_CAR_REN_INVOICE_BEFORE_MONTH);
		LocalDate creationBeforeMonth = LocalDate.now().minusMonths(Integer.valueOf(byProNumAndKey.getConfigValue()));
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		for(MonCarRenOrder c : list){
			if(StringUtils.isNotBlank(c.getSuccessTime())) {
				ZonedDateTime zonedDateTime = ZonedDateTime.parse(c.getSuccessTime(), formatter);
				String successTime = zonedDateTime.format(DateUtils.formatter_ymd_hms);
				c.setSuccessTime(successTime);
				// 判断支付时间是否是N个月前的
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime localDateSuccessTime = LocalDateTime.parse(successTime, dateTimeFormatter);
				LocalDate paymentTime = localDateSuccessTime.toLocalDate();
				if (paymentTime.isBefore(creationBeforeMonth)) {
					// 支付时间是N个月以前的，无法开票
					c.setTimeStatus(1);
				}else{
					// 支付时间不是N个月以前的，可以开票
					c.setTimeStatus(0);
				}
				// 查询开票状态 1-开票中 2-开票成功 3-未开票
				if(c.getInvoiceStatus() == null){
					c.setInvoiceStatus(3);
				}
			}
		}
		PageInfo<MonCarRenOrder> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)monCarRenLogVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		monCarRenLogVo.setPages(pageNumTotal);
		monCarRenLogVo.setTotalNum((int) pageInfo.getTotal());
		monCarRenLogVo.setPageSize(monCarRenLogVo.getPageSize());
		monCarRenLogVo.setList(list);
		monCarRenLogVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return monCarRenLogVo;
	}

	public static void main(String[] args) {

		String salt = "oITcOlxUOW3cEfp3";
		String data = "{\"invoiceType\":\"1\",\"orderNo\":\"2025030515172593651c\",\"orderDateTime\":\"2025-03-05 15:23:01\",\"invoiceSpecialMark\":\"06\",\"priceTaxMark\":\"1\",\"invoiceDetailList\":[{\"goodsLineNo\":1,\"goodsCode\":\"3040502020200000000\",\"goodsName\":\"停车费\",\"goodsSpecification\":\"\",\"goodsUnit\":\"\",\"goodsQuantity\":1,\"goodsPrice\":0,\"goodsTotalPrice\":1,\"goodsTaxRate\":0.05,\"vatSpecialManagement\":\"按5%简易征收\",\"freeTaxMark\":\"\"}],\"leaseInfo\":{\"leasePropertyNo\":\"沪房地杨字（2015）第009043号\",\"leaseAddress\":\"上海市&杨浦区\",\"leaseDetailAddress\":\"杨树浦路1088号，江浦路39号\",\"leaseCrossSign\":\"否\",\"leaseAreaUnit\":\"平方米\",\"leaseHoldDateStart\":\"2007-03-31\",\"leaseHoldDateEnd\":\"2057-03-30\"},\"buyerName\":\"安世亚太科技股份有限公司\",\"buyerTaxNo\":\"91110105756700197H\",\"invoiceTypeCode\":\"02\",\"taxNo\":\"913101183420070187\",\"naturalMark\":\"0\",\"pushEmail\":\"\",\"remarks\":\"\"}";
		String saltData = salt + data;
		String md5Str = SecurityUtil.encodeByMD5(saltData);
		System.out.println(md5Str);
		System.out.println(System.currentTimeMillis());
	}

}

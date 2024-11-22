package com.ej.hgj.controller.card;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseReqVo;
import com.ej.hgj.constant.AjaxResult;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.active.CouponQrCodeDaoMapper;
import com.ej.hgj.dao.card.CardCstBatchDaoMapper;
import com.ej.hgj.dao.card.CardCstDaoMapper;
import com.ej.hgj.dao.card.CardQrCodeDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProNeighConfDaoMapper;
import com.ej.hgj.dao.coupon.CouponGrantDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoCardMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorCodeDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorLogDaoMapper;
import com.ej.hgj.entity.active.CouponQrCode;
import com.ej.hgj.entity.card.CardCst;
import com.ej.hgj.entity.card.CardCstBatch;
import com.ej.hgj.entity.card.CardQrCode;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProNeighConfig;
import com.ej.hgj.entity.coupon.CouponGrant;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoCard;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.QrCodeUtil;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.active.ActiveRequestVo;
import com.ej.hgj.vo.active.ActiveResponseVo;
import com.ej.hgj.vo.card.CardRequestVo;
import com.ej.hgj.vo.card.CardResponseVo;
import com.ej.hgj.vo.hu.CardPermVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CardController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CardCstDaoMapper cardCstDaoMapper;

	@Autowired
	private ProNeighConfDaoMapper proNeighConfDaoMapper;

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private CardQrCodeDaoMapper cardQrCodeDaoMapper;

	@Autowired
	private OpenDoorLogDaoMapper openDoorLogDaoMapper;

	@Autowired
	private CstIntoMapper cstIntoMapper;

	@Autowired
	private CardCstBatchDaoMapper cardCstBatchDaoMapper;

	@Autowired
	private CstIntoCardMapper cstIntoCardMapper;

	/**
	 * 查询游泳卡信息
	 * @param baseReqVo
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/card/queryCardSwim")
	public CardResponseVo couponQuery(@RequestBody BaseReqVo baseReqVo) {
		CardResponseVo cardResponseVo = new CardResponseVo();
		String proNum = baseReqVo.getProNum();
		String cstCode = baseReqVo.getCstCode();
		String wxOpenId = baseReqVo.getWxOpenId();
		String expDate = DateUtils.strY(new Date());
		String cardType = "1";
		CardCst cardInfo = cardCstDaoMapper.getCardInfo(proNum, cstCode, cardType, expDate);
		// 查询登录人身份
		CstInto byWxOpenIdAndStatus_1 = cstIntoMapper.getByWxOpenIdAndStatus_1(baseReqVo.getWxOpenId());
		// 查询登录人游泳卡权限
		CstIntoCard cstIntoCard = new CstIntoCard();
		cstIntoCard.setWxOpenId(wxOpenId);
		cstIntoCard.setCardId(1);
		List<CstIntoCard> cstIntoCardList = cstIntoCardMapper.getList(cstIntoCard);
		// 身份是产权人和有卡权限的才可以使用游泳卡
		if(cardInfo != null && byWxOpenIdAndStatus_1 != null && (byWxOpenIdAndStatus_1.getIntoRole() == 2 || !cstIntoCardList.isEmpty())){
			cardResponseVo.setCardCstBatchId(cardInfo.getCardCstBatchId());
			cardResponseVo.setCardCode(cardInfo.getCardCode());
			cardResponseVo.setCardName(cardInfo.getCardName());
			cardResponseVo.setCardExpNum(cardInfo.getTotalNum() - cardInfo.getApplyNum());
			cardResponseVo.setExpDate(cardInfo.getExpDate());
		}
		cardResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		cardResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		cardResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return cardResponseVo;
	}

	/**
	 * 创建游泳卡二维码
	 * @param response
	 * @param cardRequestVo
	 * @return
	 */
	@SneakyThrows
	@RequestMapping("/card/createCardQrCode")
	@ResponseBody
	public JSONObject addCouponQrCode(HttpServletResponse response, @RequestBody CardRequestVo cardRequestVo) {
		JSONObject jsonObject = new JSONObject();
		String expDate = DateUtils.strYmd(new Date());
		String cstCode = cardRequestVo.getCstCode();
		String wxOpenId = cardRequestVo.getWxOpenId();
		String proNum = cardRequestVo.getProNum();
		String cardCstBatchId = cardRequestVo.getCardCstBatchId();
		if(StringUtils.isBlank(cstCode) || StringUtils.isBlank(wxOpenId) ||
				StringUtils.isBlank(proNum) || StringUtils.isBlank(cardCstBatchId)){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		// 查询卡批次
		CardCstBatch cardCstBatch = cardCstBatchDaoMapper.getById(cardCstBatchId);
		// 卡信息查询
		CardCst cardCst = cardCstDaoMapper.getByCardCode(cardCstBatch.getCardCode());
		Integer totalNum = cardCstBatch.getTotalNum();
		Integer applyNum = cardCstBatch.getApplyNum();
		// 卡禁用校验
		if(cardCst.getIsExp() == 0){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "卡已禁用");
			return jsonObject;
		}
		// 卡过期校验
//		Integer sysTimeInt = Integer.valueOf(DateUtils.strYmd());
//		Integer startTimeInt = Integer.valueOf(cardCst.getStartTime().replace("-",""));
//		Integer endTimeInt = Integer.valueOf(cardCst.getEndTime().replace("-",""));
//		if(sysTimeInt < startTimeInt || sysTimeInt > endTimeInt){
//			jsonObject.put("RESPCODE", "999");
//			jsonObject.put("ERRDESC", "卡已过期");
//			return jsonObject;
//		}
		String sysYear = DateUtils.strY(new Date());
		if(!sysYear.equals(cardCstBatch.getExpDate())){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "卡已过期");
			return jsonObject;
		}

		// 查询游泳池营业时间
		ConstantConfig swim_bus_time = constantConfDaoMapper.getByKey(Constant.SWIM_BUS_TIME);
		String[] busTimes = swim_bus_time.getConfigValue().split(",");
		String startHourMin = busTimes[0];
		String [] startHourMins = startHourMin.split(":");
		Integer startHour = Integer.valueOf(startHourMins[0]);
		Integer startMin = Integer.valueOf(startHourMins[1]);
		String endHourMin = busTimes[1];
		String [] endHourMins = endHourMin.split(":");
		Integer endHour = Integer.valueOf(endHourMins[0]);
		Integer endMin = Integer.valueOf(endHourMins[1]);

		// 根据日期，客户编号, 客户卡关联ID，有效状态，查询已生成的二维码
		CardQrCode cardQrCodePram = new CardQrCode();
		cardQrCodePram.setExpDate(expDate);
		cardQrCodePram.setCstCode(cstCode);
		//cardQrCodePram.setWxOpenId(wxOpenId);
		cardQrCodePram.setIsExp(1);
		cardQrCodePram.setCardCstBatchId(cardCstBatchId);
		List<CardQrCode> qrCodeByExpDate = cardQrCodeDaoMapper.getQrCodeByExpDate(cardQrCodePram);
		// 如果有直接查询历史记录，反之再调用接口
		if(!qrCodeByExpDate.isEmpty()){
			// 过滤出当前微信号当天已生成的二维码
			List<CardQrCode> qrCodeByExpDateFilter = qrCodeByExpDate.stream().filter(cardQrCode -> cardQrCode.getWxOpenId().equals(wxOpenId)).collect(Collectors.toList());
			if(!qrCodeByExpDateFilter.isEmpty()) {
				CardQrCode qrCode = qrCodeByExpDateFilter.get(0);
				// 卡二维码失效校验
	//			if(qrCode.getIsExp() == 0){
	//				jsonObject.put("RESPCODE", "999");
	//				jsonObject.put("ERRDESC", "当天开门次数已用完");
	//				return jsonObject;
	//			}
				String qrCodeContent = qrCode.getQrCodeContent();
				// 生成二维码
				String png_base64 = createQrCode(qrCodeContent, response);
				jsonObject.put("RESPCODE", "000");
				jsonObject.put("cardQrCode", png_base64);
				jsonObject.put("startExpDate",expDate+" "+startHourMin);
				jsonObject.put("endExpDate",expDate+" "+endHourMin);
				// 总开门次数
				ConstantConfig configOpenDoorSize = constantConfDaoMapper.getByKey(Constant.CARD_QR_CODE_OPEN_DOOR_SIZE);
				jsonObject.put("openDoorTotalNum", configOpenDoorSize.getConfigValue());
				// 需要扣次数的设备号
				//ConstantConfig configDeviceNo = constantConfDaoMapper.getByKey(Constant.SWIM_DEVICE_NO);
				// 已开门次数
//				List<OpenDoorLog> byCardNoAndIsUnlock = openDoorLogDaoMapper.getByCardNoAndIsUnlock(qrCode.getCardNo(), configDeviceNo.getConfigValue());
//				if (!byCardNoAndIsUnlock.isEmpty()) {
//					jsonObject.put("openDoorApplyNum", byCardNoAndIsUnlock.size());
//				} else {
//					jsonObject.put("openDoorApplyNum", "0");
//				}
				return jsonObject;
			}
		}
		// 游泳卡二维码创建次数大于游泳卡剩余次数，无法继续创建
		if(!qrCodeByExpDate.isEmpty() && qrCodeByExpDate.size() >= (totalNum - applyNum)){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "二维码生成次数不能大于卡剩余次数");
			return jsonObject;
		}
		// 游泳卡次数用完不能再创建二维码
		if((totalNum - applyNum) <= 0){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "无可用次数");
			return jsonObject;
		}
		// 拆分时间
		String[] expDateSpilt = expDate.split("-");
		Integer expYear = Integer.valueOf(expDateSpilt[0]);
		Integer expMonth = Integer.valueOf(expDateSpilt[1]);
		Integer expDay = Integer.valueOf(expDateSpilt[2]);

		// 设置特定的年、月、日、时、分、秒
		LocalDateTime startDate = LocalDateTime.of(expYear, expMonth, expDay, startHour, startMin, 00);
		// 设置特定的年、月、日、时、分、秒
		LocalDateTime endDate = LocalDateTime.of(expYear, expMonth, expDay, endHour, endMin, 59);
		// 获取时区
		ZoneId zoneId = ZoneId.systemDefault();
		// 转换为ZonedDateTime并获取毫秒时间戳
		long startTime = startDate.atZone(zoneId).toInstant().toEpochMilli();
		long endTime = endDate.atZone(zoneId).toInstant().toEpochMilli();
		// 根据项目号获取小区号
		ProNeighConfig byProjectNum = proNeighConfDaoMapper.getByProjectNum(proNum);
		String neighNo = byProjectNum.getNeighNo();

		/**
		// 从配置文件获取游泳池闸机的单元号，楼层号，房间号
		ConstantConfig constantConfig = constantConfDaoMapper.getByKey(Constant.SWIM_POOL_GATE);
		// 数据库配置示例：1,1,4-1-0101
		String swim_pool_gate = constantConfig.getConfigValue();
		String[] gateInfo = swim_pool_gate.split(",");
		String unitNo = gateInfo[0];
		String floor = gateInfo[1];
		String resCode = gateInfo[2];
		// 截取房间号
		String[] resCodeSplit = resCode.split("-");
		String addressNumber = unitNo+resCodeSplit[2];
		// 调用获取二维码内容的接口-post请求
		ConstantConfig constantConfigUrl = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_QR_CODE_URL);
		String jsonData = "{  \"neighNo\": \"" + neighNo + "\",  \"addressNumber\": " + addressNumber + ",  \"startTime\": " +
				startTime + ",  \"endTime\": " + endTime + ",  \"unitNumber\": " + unitNo + ",  \"floors\": " + floor + "}";
		 **/

		ConstantConfig constantConfig = constantConfDaoMapper.getByKey(Constant.SWIM_POOL_GATE);
		String swimPoolGate = constantConfig.getConfigValue();
		JSONArray jsonArray = JSON.parseArray(swimPoolGate);
		String swimPoolGateJStr = JSONObject.toJSONString(jsonArray.get(0));
		JSONObject swimPoolGateJson = JSONObject.parseObject(swimPoolGateJStr);
		String unitNumber = swimPoolGateJson.getString("unitNumber");
		//String floors = swimPoolGateJson.getString("floors");
		ConstantConfig constantConfigUrl = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_QR_CODE_URL);
		String jsonData = "{  \"neighNo\": \"" + neighNo + "\",  \"addressNumber\": " + unitNumber + ",  \"startTime\": " +
				startTime + ",  \"endTime\": " + endTime + ",  \"unitInfos\": " + swimPoolGate + "}";
		JSONObject resultJson = HttpClientUtil.sendPost(constantConfigUrl.getConfigValue(), jsonData);
		String result = resultJson.get("result").toString();
		String message = resultJson.getString("message");
		// 成功
		if("1".equals(result)){
			// 获取data中的二维码内容
			JSONObject data = resultJson.getJSONObject("data");
			String cardNo = data.get("cardNo").toString();
			String qrCodeContent = data.get("qrCode").toString();
			// 生成通行二维码
			String png_base64 = createQrCode(qrCodeContent,response);
			// 保存二维码生成记录
			CardQrCode cardQrCode = new CardQrCode();
			Date date = new Date();
			cardQrCode.setId(TimestampGenerator.generateSerialNumber());
			cardQrCode.setProNum(proNum);
			cardQrCode.setExpDate(expDate);
			cardQrCode.setStartTime(startTime);
			cardQrCode.setEndTime(endTime);
			cardQrCode.setCardNo(cardNo);
			cardQrCode.setQrCodeContent(qrCodeContent);
			cardQrCode.setNeighNo(neighNo);
			//cardQrCode.setAddressNum(addressNumber);
			cardQrCode.setUnitNum(unitNumber);
			//cardQrCode.setFloors(floors);
			cardQrCode.setWxOpenId(wxOpenId);
			cardQrCode.setCstCode(cstCode);
			HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
			cardQrCode.setCstName(hgjCst.getCstName());
			cardQrCode.setCardCstBatchId(cardCstBatchId);
			//cardQrCode.setResCode(resCode);
			// 1-有效 0-无效
			cardQrCode.setIsExp(1);
			cardQrCode.setCreateTime(date);
			cardQrCode.setUpdateTime(date);
			cardQrCode.setDeleteFlag(0);
			cardQrCodeDaoMapper.save(cardQrCode);
			jsonObject.put("RESPCODE", "000");
			jsonObject.put("cardQrCode", png_base64);
			jsonObject.put("startExpDate",expDate+" "+startHourMin);
			jsonObject.put("endExpDate",expDate+" "+endHourMin);

			// 总开门次数
			ConstantConfig configOpenDoorSize = constantConfDaoMapper.getByKey(Constant.CARD_QR_CODE_OPEN_DOOR_SIZE);
			jsonObject.put("openDoorTotalNum", configOpenDoorSize.getConfigValue());
			// 需要扣次数的设备号
			// ConstantConfig configDeviceNo = constantConfDaoMapper.getByKey(Constant.SWIM_DEVICE_NO);
			// 已开门次数
//			List<OpenDoorLog> byCardNoAndIsUnlock = openDoorLogDaoMapper.getByCardNoAndIsUnlock(cardNo,configDeviceNo.getConfigValue());
//			if(!byCardNoAndIsUnlock.isEmpty()) {
//				jsonObject.put("openDoorApplyNum", byCardNoAndIsUnlock.size());
//			}else {
//				jsonObject.put("openDoorApplyNum","0");
//			}
		}else {
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", message);
			return jsonObject;
		}
		return jsonObject;
	}

	/**
	 * 卡权限设置
	 */
	@ResponseBody
	@RequestMapping("card/cardPerm")
	public AjaxResult cardPerm(@RequestBody CardPermVo cardPermVo) {
		AjaxResult ajaxResult = new AjaxResult();
		String proNum = cardPermVo.getProNum();
		String wxOpenId = cardPermVo.getWxOpenId();
		String tenantWxOpenId = cardPermVo.getTenantWxOpenId();
		String cstCode = cardPermVo.getCstCode();
		// 删除卡权限
		cstIntoCardMapper.deleteCardPerm(proNum,cstCode,tenantWxOpenId);
		// 新增卡权限
		Integer[] cardIds = cardPermVo.getCardIds();
		if(cardIds != null && cardIds.length > 0){
			for(int i = 0; i<cardIds.length; i++){
				CstIntoCard cstIntoCard = new CstIntoCard();
				cstIntoCard.setId(TimestampGenerator.generateSerialNumber());
				cstIntoCard.setProNum(proNum);
				cstIntoCard.setCardId(cardIds[i]);
				cstIntoCard.setWxOpenId(tenantWxOpenId);
				cstIntoCard.setCstCode(cstCode);
				cstIntoCard.setCreateBy(wxOpenId);
				cstIntoCard.setUpdateBy(wxOpenId);
				cstIntoCard.setCreateTime(new Date());
				cstIntoCard.setUpdateTime(new Date());
				cstIntoCard.setDeleteFlag(Constant.DELETE_FLAG_NOT);
				cstIntoCardMapper.save(cstIntoCard);
			}
		}
		ajaxResult.setRespCode(Constant.SUCCESS);
		ajaxResult.setMessage(Constant.SUCCESS_RESULT_MESSAGE);
		return ajaxResult;
	}

	@SneakyThrows
	public String createQrCode(String qrCodeContent, HttpServletResponse response){
		BufferedImage bufferedImage = QrCodeUtil.createCodeToOutputStream(qrCodeContent, response.getOutputStream());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "png", outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bytes = outputStream.toByteArray();
		String png_base64 = Base64.encodeBase64String(bytes);
		png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
		return  png_base64;
	}


}

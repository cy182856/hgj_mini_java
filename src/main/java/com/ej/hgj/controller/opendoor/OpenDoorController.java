package com.ej.hgj.controller.opendoor;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProNeighConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorCodeDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorLogDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorQuickCodeDaoMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProNeighConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.opendoor.OpenDoorCode;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.entity.opendoor.OpenDoorQuickCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.QrCodeUtil;
import com.ej.hgj.utils.RandomNumberGenerator;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.opendoor.OpenDoorCodeVo;
import com.ej.hgj.vo.visit.VisitLogVo;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class OpenDoorController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private OpenDoorLogDaoMapper openDoorLogDaoMapper;

	@Autowired
	private OpenDoorCodeDaoMapper openDoorCodeDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private HgjHouseDaoMapper hgjHouseDaoMapper;

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private ProNeighConfDaoMapper proNeighConfDaoMapper;

	@Autowired
	private OpenDoorQuickCodeDaoMapper openDoorQuickCodeDaoMapper;

	@SneakyThrows
	@RequestMapping("/opendoor/addOpenDoorQrCode.do")
	@ResponseBody
	public JSONObject addVisitQrCode(HttpServletResponse response, @RequestBody OpenDoorCodeVo openDoorCodeVo) {
		JSONObject jsonObject = new JSONObject();
		String expDate = openDoorCodeVo.getExpDate();
		String houseId = openDoorCodeVo.getHouseId();
		String cstCode = openDoorCodeVo.getCstCode();
		String wxOpenId = openDoorCodeVo.getWxOpenId();
		String proNum = openDoorCodeVo.getProNum();
		String visitName = openDoorCodeVo.getVisitName();
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(openDoorCodeVo.getCstCode());
		if(StringUtils.isNotBlank(visitName)){
			openDoorCodeVo.setVisitName(visitName);
		}else {
			openDoorCodeVo.setVisitName(hgjCst.getCstName());
		}
		if(StringUtils.isBlank(expDate) || StringUtils.isBlank(houseId) || StringUtils.isBlank(cstCode) || StringUtils.isBlank(wxOpenId) || StringUtils.isBlank(proNum)){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		// 有效二维码生成次数限制
		ConstantConfig constantConfigSize = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_QR_CODE_CREATE_SIZE);
		Integer qrCreateSize = Integer.valueOf(constantConfigSize.getConfigValue());
		// 查询用户已生成的有效二维码， 大于等于当前日期的二维码生成记录
		OpenDoorCode openDoorCodeParam = new OpenDoorCode();
		openDoorCodeParam.setWxOpenId(wxOpenId);
		openDoorCodeParam.setType(1);
		openDoorCodeParam.setExpDate(expDate);
		List<OpenDoorCode> qrCodeByExpDate = openDoorCodeDaoMapper.getQrCodeByExpDate(openDoorCodeParam);
		if(!qrCodeByExpDate.isEmpty() && qrCodeByExpDate.size() >= qrCreateSize){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "访客通行码当天生成次数已用完");
			return jsonObject;
		}
		// 根据有效日期、访客称呼查询生成记录，如果有直接查询历史记录，反之再调用接口
		OpenDoorCode openDoorCodeParam2 = new OpenDoorCode();
		openDoorCodeParam2.setWxOpenId(wxOpenId);
		openDoorCodeParam2.setType(1);
		openDoorCodeParam2.setVisitName(openDoorCodeVo.getVisitName());
		openDoorCodeParam2.setExpDate(expDate);
		List<OpenDoorCode> qrCodeByExpDate2 = openDoorCodeDaoMapper.getQrCodeByExpDate2(openDoorCodeParam2);
		if(!qrCodeByExpDate2.isEmpty()){
			OpenDoorCode openDoorCode = qrCodeByExpDate2.get(0);
			String qrCodeContent = openDoorCode.getQrCodeContent();
			// 生成通行二维码
			String png_base64 = createQrCode(qrCodeContent,response);
			jsonObject.put("RESPCODE", "000");
			jsonObject.put("visitQrCode", png_base64);
			jsonObject.put("openDoorCodeVo",openDoorCodeVo);
			return jsonObject;
		}
		// 拆分时间
		String[] expDateSpilt = expDate.split("-");
		Integer expYear = Integer.valueOf(expDateSpilt[0]);
		Integer expMonth = Integer.valueOf(expDateSpilt[1]);
		Integer expDay = Integer.valueOf(expDateSpilt[2]);
		// 设置特定的年、月、日、时、分、秒
		LocalDateTime startDate = LocalDateTime.of(expYear, expMonth, expDay, 00, 00, 00);
		// 设置特定的年、月、日、时、分、秒
		LocalDateTime endDate = LocalDateTime.of(expYear, expMonth, expDay, 23, 59, 59);
		// 获取时区
		ZoneId zoneId = ZoneId.systemDefault();
		// 转换为ZonedDateTime并获取毫秒时间戳
		long startTime = startDate.atZone(zoneId).toInstant().toEpochMilli();
		long endTime = endDate.atZone(zoneId).toInstant().toEpochMilli();
		// 根据项目号获取小区号
		ProNeighConfig byProjectNum = proNeighConfDaoMapper.getByProjectNum(proNum);
		String neighNo = byProjectNum.getNeighNo();
		// 根据已选择的房屋ID获取单元号
		HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
		String unitNo = hgjHouse.getUnitNo();
		// 房间号
		String resCode = hgjHouse.getResCode();
		// 截取房间号
		String[] resCodeSplit = resCode.split("-");
		String addressNumber = unitNo+resCodeSplit[2];
		// 楼层
		String floor = hgjHouse.getFloorNum().toString();
		// 调用获取二维码内容的接口-post请求
		ConstantConfig constantConfigUrl = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_QR_CODE_URL);
//		String jsonData = "{  \"neighNo\": \"" + neighNo + "\",  \"addressNumber\": " + addressNumber + ",  \"startTime\": " +
//				startTime + ",  \"endTime\": " + endTime + ",  \"unitNumber\": " + unitNo + ",  \"floors\": " + floor + "}";
		String unitInfos = "[{\"unitNumber\":" + unitNo + ",\"floorType\":" + "2" + ",\"floors\":null}]";
		String jsonData = "{  \"neighNo\": \"" + neighNo + "\",  \"addressNumber\": " + unitNo + ",  \"startTime\": " +
				startTime + ",  \"endTime\": " + endTime + ",  \"unitInfos\": " + unitInfos + "}";
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
			OpenDoorCode openDoorCode = new OpenDoorCode();
			Date date = new Date();
			openDoorCode.setId(TimestampGenerator.generateSerialNumber());
			openDoorCode.setProNum(openDoorCodeVo.getProNum());
			openDoorCode.setProName(openDoorCodeVo.getProName());
			// 1-智慧管家二维码 2-客服直接创建的二维码
			openDoorCode.setType(1);
			openDoorCode.setVisitName(openDoorCodeVo.getVisitName());
			openDoorCode.setExpDate(openDoorCodeVo.getExpDate());
			openDoorCode.setStartTime(startTime);
			openDoorCode.setEndTime(endTime);
			openDoorCode.setCardNo(cardNo);
			openDoorCode.setQrCodeContent(qrCodeContent);
			openDoorCode.setNeighNo(neighNo);
			openDoorCode.setAddressNum(addressNumber);
			openDoorCode.setUnitNum(unitNo);
			openDoorCode.setFloors(floor);
			openDoorCode.setWxOpenId(openDoorCodeVo.getWxOpenId());
			openDoorCode.setCstCode(openDoorCodeVo.getCstCode());
			openDoorCode.setCstName(hgjCst.getCstName());
			openDoorCode.setHouseId(houseId);
			openDoorCode.setResCode(hgjHouse.getResCode());
			openDoorCode.setCreateTime(date);
			openDoorCode.setUpdateTime(date);
			openDoorCode.setDeleteFlag(0);
			openDoorCodeDaoMapper.save(openDoorCode);
			jsonObject.put("RESPCODE", "000");
			jsonObject.put("visitQrCode", png_base64);
			jsonObject.put("openDoorCodeVo",openDoorCodeVo);
		}else {
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", message);
			return jsonObject;
		}
		return jsonObject;
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

	@RequestMapping("/opendoor/createQuickCode.do")
	@ResponseBody
	public JSONObject addVisitRandomNum(@RequestBody OpenDoorCodeVo openDoorLogVo) {
		JSONObject jsonObject = new JSONObject();
		String houseId = openDoorLogVo.getHouseId();
		String cstCode = openDoorLogVo.getCstCode();
		String wxOpenId = openDoorLogVo.getWxOpenId();
		String proNum = openDoorLogVo.getProNum();
		if(StringUtils.isBlank(houseId) || StringUtils.isBlank(cstCode) || StringUtils.isBlank(wxOpenId) || StringUtils.isBlank(proNum)){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		Set<Integer> randomNum = RandomNumberGenerator.generateRandomNumbers(100000, 999999, 1);
		// 根据已选择的房屋ID获取单元号
		HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
		String unitNo = hgjHouse.getUnitNo();
		// 房间号
		String resCode = hgjHouse.getResCode();
		// 截取房间号
		String[] resCodeSplit = resCode.split("-");
		String addressNumber = unitNo+resCodeSplit[2];
		// 楼层
		String floor = hgjHouse.getFloorNum().toString();
		OpenDoorQuickCode openDoorQuickCode = new OpenDoorQuickCode();
		Date date = new Date();
		openDoorQuickCode.setId(TimestampGenerator.generateSerialNumber());
		openDoorQuickCode.setProNum(openDoorLogVo.getProNum());
		openDoorQuickCode.setProName(openDoorLogVo.getProName());
		// 当前时间年月日
		openDoorQuickCode.setExpDate(DateUtils.strYmd(date));
		ProNeighConfig byProjectNum = proNeighConfDaoMapper.getByProjectNum(proNum);
		openDoorQuickCode.setNeighNo(byProjectNum.getNeighNo());
		openDoorQuickCode.setAddressNum(addressNumber);
		openDoorQuickCode.setUnitNum(unitNo);
		openDoorQuickCode.setFloors(floor);
		openDoorQuickCode.setWxOpenId(openDoorLogVo.getWxOpenId());
		openDoorQuickCode.setCstCode(openDoorLogVo.getCstCode());
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(openDoorLogVo.getCstCode());
		openDoorQuickCode.setCstName(hgjCst.getCstName());
		openDoorQuickCode.setHouseId(houseId);
		openDoorQuickCode.setResCode(hgjHouse.getResCode());
		String randNum = null;
		for(Integer integer : randomNum){
			randNum = integer.toString();
		}
		// 1-有效 0-无效
		openDoorQuickCode.setIsExpire(1);
		openDoorQuickCode.setCreateTime(date);
		openDoorQuickCode.setUpdateTime(date);
		openDoorQuickCode.setQuickCode(randNum);
		openDoorQuickCode.setDeleteFlag(Constant.DELETE_FLAG_NOT);
		openDoorQuickCodeDaoMapper.save(openDoorQuickCode);
		jsonObject.put("RESPCODE", "000");
		jsonObject.put("randomNum", randomNum);
		return jsonObject;
	}

	/**
	 * 获取一周时间
	 * @param openDoorLogVo
	 * @return
	 */
	@RequestMapping("/opendoor/queryWeekDate.do")
	@ResponseBody
	public JSONObject queryWeekDate(@RequestBody VisitLogVo openDoorLogVo) {
		JSONObject jsonObject = new JSONObject();
		Date date = new Date();
		String startDate = DateUtils.strYmd(date);
		// 获取当前时间后的第七天
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH,7);
		Date date7Day = calendar.getTime();
		String endDate = DateUtils.strYmd(date7Day);
		jsonObject.put("respCode", "000");
		jsonObject.put("startDate", startDate);
		jsonObject.put("endDate", endDate);
		return jsonObject;
	}

	@RequestMapping("/opendoor/queryOpenDoorLog.do")
	@ResponseBody
	public OpenDoorCodeVo queryVisitInfos(HttpServletRequest request, @RequestBody OpenDoorCodeVo openDoorLogVo) {
		PageHelper.offsetPage((openDoorLogVo.getPageNum()-1) * openDoorLogVo.getPageSize(),openDoorLogVo.getPageSize());
		OpenDoorLog doorLog = new OpenDoorLog();
//		doorLog.setProNum(openDoorLogVo.getProNum());
		doorLog.setWxOpenId(openDoorLogVo.getWxOpenId());
//		doorLog.setCstCode(openDoorLogVo.getCstCode());
		List<OpenDoorLog> list = openDoorLogDaoMapper.getList(doorLog);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		for(OpenDoorLog openDoorLog : list){
			Long eventTime = openDoorLog.getEventTime();
			Date date = new Date(eventTime);
			String formattedDate = sdf.format(date);
			openDoorLog.setOpenDoorTime(formattedDate);
		}
		PageInfo<OpenDoorLog> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)openDoorLogVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		openDoorLogVo.setPages(pageNumTotal);
		openDoorLogVo.setTotalNum((int) pageInfo.getTotal());
		openDoorLogVo.setPageSize(openDoorLogVo.getPageSize());
		openDoorLogVo.setList(list);
		openDoorLogVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return openDoorLogVo;
	}

	/**
	 * 获取通行码说明文字
	 * @return
	 */
	@RequestMapping("/opendoor/queryVisitExplain.do")
	@ResponseBody
	public JSONObject queryVisitExplain() {
		JSONObject jsonObject = new JSONObject();
		ConstantConfig visitorCode = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_VISITOR_CODE);
		ConstantConfig quickAccessCode = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_QUICK_ACCESS_CODE);
		jsonObject.put("respCode", "000");
		jsonObject.put("visitorCode", visitorCode.getConfigValue());
		jsonObject.put("quickAccessCode", quickAccessCode.getConfigValue());
		return jsonObject;
	}


	public static void main(String[] args) {
		//String url = "http://10.251.2.4:18520/QrCode/ZHGJ";
		//String json = "{\"name\":\"张三\"}"; // 示例中文数据
		//String json = "{\"neighNo\": \"110111509112\",  \"addressNumber\": 1101,  \"startTime\": 1726124779000,  \"endTime\": 1726127779000,  \"unitNumber\": 1,  \"floors\": 11}";
		//JSONObject jsonObject = HttpClientUtil.sendPost(url, json);

		//String startDate = "2024-09-13 00:00:00";
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//		LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
//		long startTime = startDateTime.toEpochSecond(ZoneOffset.UTC);
//
//		String endDate = "2024-09-13 23:59:59";
//		LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);
//		long endTime = endDateTime.toEpochSecond(ZoneOffset.UTC);


		// 设置特定的年、月、日、时、分、秒
		LocalDateTime startDate = LocalDateTime.of(2024, 9, 13, 00, 00, 00);

		// 设置特定的年、月、日、时、分、秒
		LocalDateTime endDate = LocalDateTime.of(2024, 9, 13, 23, 59, 59);

		// 获取时区
		ZoneId zoneId = ZoneId.systemDefault();

		// 转换为ZonedDateTime并获取毫秒时间戳
		long startTime = startDate.atZone(zoneId).toInstant().toEpochMilli();

		long endTime = endDate.atZone(zoneId).toInstant().toEpochMilli();

		System.out.println(startTime+"---"+endTime);


		Instant instant = Instant.ofEpochMilli(endTime);
		LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = dateTime.format(formatter);
		System.out.println(formattedDateTime);



	}

}


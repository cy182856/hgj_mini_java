package com.ej.hgj.controller.active;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.active.CouponQrCodeDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProNeighConfDaoMapper;
import com.ej.hgj.dao.coupon.CouponGrantDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorCodeDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorLogDaoMapper;
import com.ej.hgj.entity.active.CouponQrCode;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProNeighConfig;
import com.ej.hgj.entity.coupon.CouponGrant;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.opendoor.OpenDoorCode;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.QrCodeUtil;
import com.ej.hgj.utils.bill.*;
import com.ej.hgj.vo.active.ActiveRequestVo;
import com.ej.hgj.vo.active.ActiveResponseVo;
import com.ej.hgj.vo.opendoor.OpenDoorCodeVo;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ActiveController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private ProNeighConfDaoMapper proNeighConfDaoMapper;

	@Autowired
	private HgjHouseDaoMapper hgjHouseDaoMapper;

	@Autowired
	private OpenDoorLogDaoMapper openDoorLogDaoMapper;

	@Autowired
	private CouponGrantDaoMapper couponGrantDaoMapper;

	@Autowired
	private OpenDoorCodeDaoMapper openDoorCodeDaoMapper;

	@Autowired
	private CouponQrCodeDaoMapper couponQrCodeDaoMapper;

//	@ResponseBody
//	@RequestMapping("/active/openLog/query.do")
//	public ActiveResponseVo openLogQuery(@RequestBody ActiveRequestVo activeRequestVo) {
//		Integer pageNum = activeRequestVo.getPageNum();
//		Integer pageSize = activeRequestVo.getPageSize();
//		activeRequestVo.setPageNum(null);
//		activeRequestVo.setPageSize(null);
//		ActiveResponseVo activeResponseVo = new ActiveResponseVo();
//		String cstCode = activeRequestVo.getCstCode();
//		String wxOpenId = activeRequestVo.getWxOpenId();
//		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
//
//		// 头部显示数据-客户名称
//		String cstName = hgjCst.getCstName();
//		// 头部显示数据-总次数
//
//		PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
//		OpenDoorLog doorLog = new OpenDoorLog();
////		doorLog.setProNum(activeRequestVo.getProNum());
////		doorLog.setWxOpenId(activeRequestVo.getWxOpenId());
////		doorLog.setCstCode(activeRequestVo.getCstCode());
//		List<OpenDoorLog> list = openDoorLogDaoMapper.getList(doorLog);
//		PageInfo<OpenDoorLog> pageInfo = new PageInfo<>(list);
//		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) pageSize);
//		list = pageInfo.getList();
//		logger.info("list返回记录数");
//		logger.info(list != null ? list.size() + "" : 0 + "");
//		activeResponseVo.setPages(pageNumTotal);
//		activeResponseVo.setTotalNum((int) pageInfo.getTotal());
//		activeResponseVo.setPageSize(pageSize);
//		activeResponseVo.setOpenDoorLogList(list);
//		activeResponseVo.setCstName(cstName);
//		activeResponseVo.setCouponTotalNum(50);
//		activeResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
//		activeResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
//		activeResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
//		return activeResponseVo;
//	}
//

	@ResponseBody
	@RequestMapping("/active/coupon/query.do")
	public ActiveResponseVo couponQuery(@RequestBody ActiveRequestVo activeRequestVo) {
		Integer pageNum = activeRequestVo.getPageNum();
		Integer pageSize = activeRequestVo.getPageSize();
		ActiveResponseVo activeResponseVo = new ActiveResponseVo();
		String cstCode = activeRequestVo.getCstCode();
		//String wxOpenId = activeRequestVo.getWxOpenId();
		//HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
		CouponGrant couponGrant = new CouponGrant();
		// couponGrant.setWxOpenId(activeRequestVo.getWxOpenId());
		couponGrant.setCstCode(cstCode);
		couponGrant.setCouponType(activeRequestVo.getCouponType());
		couponGrant.setRange(1);
		List<CouponGrant> list = couponGrantDaoMapper.getList(couponGrant);
		Integer sysTime = Integer.valueOf(DateUtils.strYmd());
		for(CouponGrant c : list){
			Integer startTime = Integer.valueOf(c.getStartTime().replace("-",""));
			Integer endTime = Integer.valueOf(c.getEndTime().replace("-",""));
			if(c.getExpNum() > 0 && sysTime >= startTime && startTime <= endTime){
				c.setStatus(1);
			}
			if((c.getExpNum() == 0 && sysTime >= startTime && sysTime <= endTime)||
					(c.getExpNum() == 0 && sysTime >= endTime)){
				c.setStatus(2);
			}
			if(c.getExpNum() > 0 && startTime > sysTime){
				c.setStatus(3);
			}
		}
		PageInfo<CouponGrant> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) pageSize);
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "" : 0 + "");
		activeResponseVo.setPages(pageNumTotal);
		activeResponseVo.setTotalNum((int) pageInfo.getTotal());
		activeResponseVo.setPageSize(pageSize);
		activeResponseVo.setCouponGrantList(list);
		activeResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		activeResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		activeResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return activeResponseVo;
	}

	@SneakyThrows
	@RequestMapping("/active/addCouponQrCode.do")
	@ResponseBody
	public JSONObject addCouponQrCode(HttpServletResponse response, @RequestBody ActiveRequestVo activeRequestVo) {
		JSONObject jsonObject = new JSONObject();
		String expDate = DateUtils.strYmd(new Date());
		String cstCode = activeRequestVo.getCstCode();
		String wxOpenId = activeRequestVo.getWxOpenId();
		String proNum = activeRequestVo.getProNum();
		String couponId = activeRequestVo.getCouponId();
		Integer status = activeRequestVo.getStatus();
		if(StringUtils.isBlank(cstCode) || StringUtils.isBlank(wxOpenId) ||
				StringUtils.isBlank(proNum) || StringUtils.isBlank(couponId) || status == null){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		// 可用券当有效次数用完不能再创建二维码
		CouponGrant couponGrant = couponGrantDaoMapper.getById(couponId);{
			if(couponGrant.getExpNum() == 0 && status == 1){
				jsonObject.put("RESPCODE", "999");
				jsonObject.put("ERRDESC", "无可用次数");
				return jsonObject;
			}
		}
		// 有效二维码生成次数限制
		ConstantConfig constantConfigSize = constantConfDaoMapper.getByKey(Constant.COUPON_QR_CODE_CREATE_SIZE);
		Integer qrCreateSize = Integer.valueOf(constantConfigSize.getConfigValue());
		// 查询用户已生成的有效二维码， 大于等于当前日期的二维码生成记录
		CouponQrCode couponQrCodeParam = new CouponQrCode();
		couponQrCodeParam.setWxOpenId(wxOpenId);
		couponQrCodeParam.setExpDate(expDate);
		List<CouponQrCode> qrCodeByExpDate = couponQrCodeDaoMapper.getQrCodeByExpDate(couponQrCodeParam);
		if(!qrCodeByExpDate.isEmpty() && qrCodeByExpDate.size() >= qrCreateSize){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "二维码生成次数已达到上限值");
			return jsonObject;
		}

		// 根据有效日期券ID查询生成记录，如果有直接查询历史记录，反之再调用接口
		if(!qrCodeByExpDate.isEmpty()){
			List<CouponQrCode> qrCodeByExpDateFilter = qrCodeByExpDate.stream().filter(couponQrCode -> couponQrCode.getCouponId().equals(couponId)).collect(Collectors.toList());
			if(!qrCodeByExpDateFilter.isEmpty()){
				CouponQrCode couponQrCode = qrCodeByExpDateFilter.get(0);
				String qrCodeContent = couponQrCode.getQrCodeContent();
				// 生成通行二维码
				String png_base64 = createQrCode(qrCodeContent,response);
				jsonObject.put("RESPCODE", "000");
				jsonObject.put("couponQrCode", png_base64);
				jsonObject.put("expDate",expDate);
				return jsonObject;
			}else {
				jsonObject.put("RESPCODE", "999");
				jsonObject.put("ERRDESC", "二维码已失效");
				return jsonObject;
			}
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
		//HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
		//String unitNo = hgjHouse.getUnitNo();
		String unitNo = "1";
		// 房间号
		//String resCode = hgjHouse.getResCode();
		String resCode = "4-1-0101";
		// 截取房间号
		String[] resCodeSplit = resCode.split("-");
		String addressNumber = unitNo+resCodeSplit[2];
		// 楼层
		//String floor = hgjHouse.getFloorNum().toString();
		String floor = "1";
		// 调用获取二维码内容的接口-post请求
		ConstantConfig constantConfigUrl = constantConfDaoMapper.getByKey(Constant.OPEN_DOOR_QR_CODE_URL);
		String jsonData = "{  \"neighNo\": \"" + neighNo + "\",  \"addressNumber\": " + addressNumber + ",  \"startTime\": " +
				startTime + ",  \"endTime\": " + endTime + ",  \"unitNumber\": " + unitNo + ",  \"floors\": " + floor + "}";
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
			CouponQrCode couponQrCode = new CouponQrCode();
			Date date = new Date();
			couponQrCode.setId(TimestampGenerator.generateSerialNumber());
			couponQrCode.setProNum(proNum);
			couponQrCode.setProName(activeRequestVo.getProName());
			couponQrCode.setExpDate(expDate);
			couponQrCode.setStartTime(startTime);
			couponQrCode.setEndTime(endTime);
			couponQrCode.setCardNo(cardNo);
			couponQrCode.setQrCodeContent(qrCodeContent);
			couponQrCode.setNeighNo(neighNo);
			couponQrCode.setAddressNum(addressNumber);
			couponQrCode.setUnitNum(unitNo);
			couponQrCode.setFloors(floor);
			couponQrCode.setWxOpenId(wxOpenId);
			couponQrCode.setCstCode(cstCode);
			HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
			couponQrCode.setCstName(hgjCst.getCstName());
			couponQrCode.setCouponId(couponId);
			couponQrCode.setResCode(resCode);
			// 1-有效 0-无效
			couponQrCode.setIsExpire(1);
			couponQrCode.setCreateTime(date);
			couponQrCode.setUpdateTime(date);
			couponQrCode.setDeleteFlag(0);
			couponQrCodeDaoMapper.save(couponQrCode);
			jsonObject.put("RESPCODE", "000");
			jsonObject.put("couponQrCode", png_base64);
			jsonObject.put("expDate",expDate);

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

}

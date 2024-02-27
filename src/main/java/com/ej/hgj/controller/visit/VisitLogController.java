package com.ej.hgj.controller.visit;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.visit.VisitLogDaoMapper;
import com.ej.hgj.dao.visit.VisitPassDaoMapper;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.repair.RepairLog;
import com.ej.hgj.entity.visit.VisitLog;
import com.ej.hgj.entity.visit.VisitPass;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.GenerateUniqueIdUtil;
import com.ej.hgj.utils.QrCodeUtil;
import com.ej.hgj.utils.RandomNumberGenerator;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.visit.VisitLogVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.GenericFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Controller
public class VisitLogController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private VisitPassDaoMapper visitPassDaoMapper;

	@Autowired
	private VisitLogDaoMapper visitLogDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private HgjHouseDaoMapper hgjHouseDaoMapper;

	@RequestMapping("/visitinfo/addVisitQrCode.do")
	@ResponseBody
	public JSONObject addVisitQrCode(HttpServletResponse response, @RequestBody VisitLogVo visitLogVo) {
		JSONObject jsonObject = new JSONObject();
		if(StringUtils.isBlank(visitLogVo.getVisitName()) || StringUtils.isBlank(visitLogVo.getCstCode())
				|| StringUtils.isBlank(visitLogVo.getWxOpenId()) || StringUtils.isBlank(visitLogVo.getProNum())){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		try {
			VisitPass visitPass = new VisitPass();
			Date date = new Date();
			String guid = TimestampGenerator.generateSerialNumber();
			visitPass.setId(guid);
			visitPass.setProNum(visitLogVo.getProNum());
			visitPass.setProName(visitLogVo.getProName());
			visitPass.setPassUrl(guid);
			visitPass.setWxOpenId(visitLogVo.getWxOpenId());
			visitPass.setCstCode(visitLogVo.getCstCode());
			HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(visitLogVo.getCstCode());
			visitPass.setCstName(hgjCst.getCstName());
			visitPass.setCstMobile(hgjCst.getMobile());
			visitPass.setHouseId(visitLogVo.getHouseId());
			visitPass.setVisitName(visitLogVo.getVisitName());
			visitPass.setCarNum(visitLogVo.getCarNum());
			visitPass.setExpNum(visitLogVo.getExpCnt());
			visitPass.setResNum(visitLogVo.getExpCnt());
			// visitPass.setEffectiveTime(24);
			visitPass.setIsExpire(0);
			visitPass.setCreateTime(date);
			visitPass.setUpdateTime(date);
			visitPass.setDeleteFlag(0);
			visitPassDaoMapper.save(visitPass);
			// 生成通行二维码
			BufferedImage bufferedImage = QrCodeUtil.createCodeToOutputStream(guid, response.getOutputStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", baos);
			byte[] bytes = baos.toByteArray();
			//BASE64Encoder encoder = new BASE64Encoder();
			//String png_base64 = encoder.encodeBuffer(bytes).trim();//转换成base64串
			String png_base64 = Base64.encodeBase64String(bytes);
			png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
			jsonObject.put("RESPCODE", "000");
			jsonObject.put("visitQrCode", png_base64);
			// 前端显示数据
			HgjHouse hgjHouse = hgjHouseDaoMapper.getById(visitLogVo.getHouseId());
			visitLogVo.setHouseName(hgjHouse.getBudName()+"-"+hgjHouse.getResName());
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(date);
//			cal.add(Calendar.HOUR_OF_DAY, 24);
//			Date newDate = cal.getTime();
//			visitLogVo.setExpDate(newDate);
			visitLogVo.setEffectuateDate(date);
			jsonObject.put("visitLogVo",visitLogVo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	@RequestMapping("/visitinfo/addVisitRandomNum.do")
	@ResponseBody
	public JSONObject addVisitRandomNum(@RequestBody VisitLogVo visitLogVo) {
		JSONObject jsonObject = new JSONObject();
		if(StringUtils.isBlank(visitLogVo.getCstCode())
				|| StringUtils.isBlank(visitLogVo.getWxOpenId()) || StringUtils.isBlank(visitLogVo.getProNum())){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		// 检验客户当天快速通行码生成数量，不能超过50个
		VisitPass vp = new VisitPass();
		vp.setCstCode(visitLogVo.getCstCode());
		vp.setWxOpenId(visitLogVo.getWxOpenId());
		List<VisitPass> cstRanNumList = visitPassDaoMapper.getCstRanNumList(vp);
		if(!cstRanNumList.isEmpty() && cstRanNumList.size() >= 50){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "快速通行码数量已达到上限");
			return jsonObject;
		}
		Set<Integer> randomNum = RandomNumberGenerator.generateRandomNumbers(100000, 999999, 1);
		VisitPass visitPass = new VisitPass();
		Date date = new Date();
		String guid = TimestampGenerator.generateSerialNumber();
		visitPass.setId(guid);
		visitPass.setProNum(visitLogVo.getProNum());
		visitPass.setProName(visitLogVo.getProName());
		String randNum = null;
		for(Integer integer : randomNum){
			randNum = integer.toString();
		}
		visitPass.setRandNum(randNum);
		visitPass.setWxOpenId(visitLogVo.getWxOpenId());
		visitPass.setCstCode(visitLogVo.getCstCode());
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(visitLogVo.getCstCode());
		visitPass.setCstName(hgjCst.getCstName());
		visitPass.setCstMobile(hgjCst.getMobile());
		visitPass.setExpNum(visitLogVo.getExpCnt());
		visitPass.setResNum(visitLogVo.getExpCnt());
		//visitPass.setEffectiveTime(24);
		visitPass.setIsExpire(0);
		visitPass.setCreateTime(date);
		visitPass.setUpdateTime(date);
		visitPass.setDeleteFlag(0);
		visitPassDaoMapper.save(visitPass);
		jsonObject.put("RESPCODE", "000");
		jsonObject.put("randomNum", randomNum);
		return jsonObject;
	}
	
//	@RequestMapping("/visitinfo/showVisitInfoDetail.do")
//	@ResponseBody
//	public JSONObject showVisitInfoDetail(HttpServletRequest request, @RequestBody VisitLogVo visitLogVo) {
//		JSONObject jsonObject = new JSONObject();
//		logInfo(logger, "查看访客通行码新消息请求参数为",showDetails(visitLogVo));
//		try {
//			visitLogVo.setCustId(getCustId(request));
//			VisitLogResponse visitLogResponse = visitLogManager.checkVisitLog(visitLogVo);
//			logInfo(logger, "查看访客通行码信息请求返回",showDetails(visitLogResponse));
//			jsonObject.put(JiasvConstants.RESP_CODE, visitLogResponse.getRespCode());
//			jsonObject.put(JiasvConstants.ERR_CODE, visitLogResponse.getErrCode());
//			jsonObject.put(JiasvConstants.ERR_DESC, visitLogResponse.getErrDesc());
//			jsonObject.put("visitLogInfo", visitLogResponse.getVisitLogRespVo());
//		} catch (Exception e) {
//			logError(logger, e, "查看访客通行码信息系统异常");
//			jsonObject.put(JiasvConstants.RESP_CODE, MonsterBasicRespCode.SYSTEM_ERROR.getReturnCode());
//			jsonObject.put(JiasvConstants.ERR_CODE, JiasvBasicRespCode.SYSTEM_EXCEPTION.getRespCode());
//			jsonObject.put(JiasvConstants.ERR_DESC, JiasvBasicRespCode.SYSTEM_EXCEPTION.getRespDesc());
//		}
//		return jsonObject;
//	}
//
	@RequestMapping("/visitinfo/queryVisitInfos.do")
	@ResponseBody
	public VisitLogVo queryVisitInfos(HttpServletRequest request, @RequestBody VisitLogVo visitLogVo) {
		PageHelper.offsetPage((visitLogVo.getPageNum()-1) * visitLogVo.getPageSize(),visitLogVo.getPageSize());
		VisitLog visitLog = new VisitLog();
		visitLog.setProNum(visitLogVo.getProNum());
		visitLog.setWxOpenId(visitLogVo.getWxOpenId());
		visitLog.setCstCode(visitLogVo.getCstCode());
		List<VisitLog> list = visitLogDaoMapper.getList(visitLog);
		PageInfo<VisitLog> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)visitLogVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		visitLogVo.setPages(pageNumTotal);
		visitLogVo.setTotalNum((int) pageInfo.getTotal());
		visitLogVo.setPageSize(visitLogVo.getPageSize());

		for(VisitLog v : list){
			// 前端显示数据
			HgjHouse hgjHouse = hgjHouseDaoMapper.getById(v.getHouseId());
			if(hgjHouse != null){
				v.setHouseName(hgjHouse.getBudName()+"-"+hgjHouse.getResName());
			}
			v.setEffectuateDate(v.getCreateTime());
		}
		visitLogVo.setList(list);
		visitLogVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return visitLogVo;
	}
//
//	@RequestMapping("/visitinfo/updVisitLog.do")
//	@ResponseBody
//	public JSONObject updVisitLog(HttpServletRequest request, @RequestBody VisitLogVo visitLogVo) {
//		JSONObject jsonObject = new JSONObject();
//		logInfo(logger, "更新访客日志通行码信息请求参数为",showDetails(visitLogVo));
//		try {
//			visitLogVo.setCustId(getCustId(request));
//			VisitLogResponse visitLogResponse = visitLogManager.updVisitLogCheckCode(visitLogVo);
//			logInfo(logger, "更新访客日志通行码信息请求返回",showDetails(visitLogResponse));
//			jsonObject.put(JiasvConstants.RESP_CODE, visitLogResponse.getRespCode());
//			jsonObject.put(JiasvConstants.ERR_CODE, visitLogResponse.getErrCode());
//			jsonObject.put(JiasvConstants.ERR_DESC, visitLogResponse.getErrDesc());
//		} catch (Exception e) {
//			logError(logger, e, "更新访客日志通行码信息系统异常");
//			jsonObject.put(JiasvConstants.RESP_CODE, MonsterBasicRespCode.SYSTEM_ERROR.getReturnCode());
//			jsonObject.put(JiasvConstants.ERR_CODE, JiasvBasicRespCode.SYSTEM_EXCEPTION.getRespCode());
//			jsonObject.put(JiasvConstants.ERR_DESC, JiasvBasicRespCode.SYSTEM_EXCEPTION.getRespDesc());
//		}
//		return jsonObject;
//	}
}

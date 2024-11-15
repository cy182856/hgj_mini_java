package com.ej.hgj.controller.resident;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseReqVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.identity.IdentityDaoMapper;
import com.ej.hgj.dao.visit.VisitLogDaoMapper;
import com.ej.hgj.dao.visit.VisitPassDaoMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.identity.Identity;
import com.ej.hgj.entity.visit.VisitLog;
import com.ej.hgj.entity.visit.VisitPass;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.sy.dao.house.SyHouseDaoMapper;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Controller
public class ResQrCodeController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CstIntoMapper cstIntoMapper;

	@Autowired
	private IdentityDaoMapper identityDaoMapper;

	@Autowired
	private SyHouseDaoMapper syHouseDaoMapper;

	@RequestMapping("/resQrCode/addResQrCode.do")
	@ResponseBody
	public JSONObject addVisitQrCode(HttpServletResponse response, @RequestBody BaseReqVo baseReqVo) {
		JSONObject jsonObject = new JSONObject();
		String wxOpenId = baseReqVo.getWxOpenId();
		String cstCode = baseReqVo.getCstCode();
		if(StringUtils.isBlank(baseReqVo.getCstCode()) || StringUtils.isBlank(wxOpenId) || StringUtils.isBlank(baseReqVo.getProNum())){
			jsonObject.put("RESPCODE", "999");
			jsonObject.put("ERRDESC", "请求参数错误");
			return jsonObject;
		}
		try {
			// 二维码参数 wxOpenId + 当前时间
			long sysTem = System.currentTimeMillis();
			String qrCodeContent = wxOpenId + "," + sysTem;
			// 生成通行二维码
			BufferedImage bufferedImage = QrCodeUtil.createCodeToOutputStream(qrCodeContent, response.getOutputStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", baos);
			byte[] bytes = baos.toByteArray();
			String png_base64 = Base64.encodeBase64String(bytes);
			png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
			jsonObject.put("RESPCODE", "000");
			jsonObject.put("resQrCode", png_base64);
			// 当前时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date createDate = new Date(sysTem);
			String createTime = sdf.format(createDate);
			jsonObject.put("createTime", createTime);
			// 有效截止时间-5分钟
			LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(sysTem), ZoneId.systemDefault());
			LocalDateTime newDateTime = dateTime.plusMinutes(5);
			long newTimestamp = newDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			Date endDate = new Date(newTimestamp);
			String endTime = sdf.format(endDate);
			jsonObject.put("endTime", endTime);
			// 入住信息
			CstInto byWxOpenIdAndStatus_1 = cstIntoMapper.getByWxOpenIdAndStatus_1(wxOpenId);
			// 姓名
			jsonObject.put("userName", byWxOpenIdAndStatus_1.getUserName());
			// 身份
			Identity byCode = identityDaoMapper.getByCode(byWxOpenIdAndStatus_1.getIntoRole().toString());
			jsonObject.put("identity", byCode.getMiniDesc());
			// 房间号
			HgjHouse hgjHouse = new HgjHouse();
			hgjHouse.setCstCode(cstCode);
			List<HgjHouse> list = syHouseDaoMapper.getListByCstCode(hgjHouse);
			List<String> houseList = new ArrayList<>();
			if(!list.isEmpty()){
				for(HgjHouse house : list){
					houseList.add(house.getResName());
				}
			}
			jsonObject.put("houseList", houseList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}

}

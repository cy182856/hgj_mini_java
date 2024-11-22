package com.ej.hgj.controller.identity;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseReqVo;
import com.ej.hgj.constant.AjaxResult;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.identity.IdentityDaoMapper;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoCard;
import com.ej.hgj.entity.hu.CstIntoHouse;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.identity.Identity;
import com.ej.hgj.sy.dao.house.SyHouseDaoMapper;
import com.ej.hgj.utils.QrCodeUtil;
import com.ej.hgj.vo.repair.RepairRequestVo;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class IdentityController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IdentityDaoMapper identityDaoMapper;

	@Autowired
	private HgjHouseDaoMapper hgjHouseDaoMapper;

	@RequestMapping("/identity/list")
	@ResponseBody
	public JSONObject identityList(@RequestBody BaseReqVo baseReqVo) {
		JSONObject jsonObject = new JSONObject();
		String proNum = baseReqVo.getProNum();
		Identity identity = new Identity();
		identity.setProNum(proNum);
		List<Identity> identityList = identityDaoMapper.getList(identity);
		jsonObject.put("respCode", "000");
		jsonObject.put("identityList", identityList);
		return jsonObject;
	}
}

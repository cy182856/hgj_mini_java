package com.ej.hgj.controller.active;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.bill.BillMergeDaoMapper;
import com.ej.hgj.dao.bill.BillMergeDetailDaoMapper;
import com.ej.hgj.dao.bill.PaymentRecordDaoMapper;
import com.ej.hgj.dao.bill.item.HgjBillItemDaoMapper;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.coupon.CouponGrantDaoMapper;
import com.ej.hgj.dao.cst.CstPayPerDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoHouseDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorLogDaoMapper;
import com.ej.hgj.dao.tag.TagCstDaoMapper;
import com.ej.hgj.entity.bill.*;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.coupon.CouponGrant;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstIntoHouse;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.entity.tag.TagCst;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.bill.BillService;
import com.ej.hgj.sy.dao.bill.BillDaoMapper;
import com.ej.hgj.sy.dao.bill.item.BillItemDaoMapper;
import com.ej.hgj.sy.dao.bill.year.BillYearDaoMapper;
import com.ej.hgj.utils.RandomNumberGenerator;
import com.ej.hgj.utils.bill.*;
import com.ej.hgj.vo.active.ActiveRequestVo;
import com.ej.hgj.vo.active.ActiveResponseVo;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.BillResponseVo;
import com.ej.hgj.vo.bill.RequestPaymentStatusVo;
import com.ej.hgj.vo.bill.SignInfoVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import okhttp3.HttpUrl;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ActiveController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${private.key}")
	private String privateKey;

	@Value("${private.key.yy}")
	private String privateKeyYy;

	@Value("${private.key.fx}")
	private String privateKeyFx;

	@Value("${private.key.bus}")
	private String privateKeyBus;

	@Autowired
	private BillDaoMapper billDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private PaymentRecordDaoMapper paymentRecordDaoMapper;

	@Autowired
	private BillService billService;

	@Autowired
	private CstPayPerDaoMapper cstPayPerDaoMapper;

	@Autowired
	private CstIntoMapper cstIntoMapper;

	@Autowired
	private BillMergeDaoMapper billMergeDaoMapper;

	@Autowired
	private BillMergeDetailDaoMapper billMergeDetailDaoMapper;

	@Autowired
	private ProConfDaoMapper proConfDaoMapper;

	@Autowired
	private BillYearDaoMapper billYearDaoMapper;

	@Autowired
	private BillItemDaoMapper billItemDaoMapper;

	@Autowired
	private HgjBillItemDaoMapper hgjBillItemDaoMapper;

	@Autowired
	private CstIntoHouseDaoMapper cstIntoHouseDaoMapper;

	@Autowired
	private TagCstDaoMapper tagCstDaoMapper;

	@Autowired
	private OpenDoorLogDaoMapper openDoorLogDaoMapper;

	@Autowired
	private CouponGrantDaoMapper couponGrantDaoMapper;

	@ResponseBody
	@RequestMapping("/active/openLog/query.do")
	public ActiveResponseVo openLogQuery(@RequestBody ActiveRequestVo activeRequestVo) {
		Integer pageNum = activeRequestVo.getPageNum();
		Integer pageSize = activeRequestVo.getPageSize();
		activeRequestVo.setPageNum(null);
		activeRequestVo.setPageSize(null);
		ActiveResponseVo activeResponseVo = new ActiveResponseVo();
		String cstCode = activeRequestVo.getCstCode();
		String wxOpenId = activeRequestVo.getWxOpenId();
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);

		// 头部显示数据-客户名称
		String cstName = hgjCst.getCstName();
		// 头部显示数据-总次数

		PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
		OpenDoorLog doorLog = new OpenDoorLog();
		doorLog.setProNum(activeRequestVo.getProNum());
		doorLog.setWxOpenId(activeRequestVo.getWxOpenId());
		doorLog.setCstCode(activeRequestVo.getCstCode());
		List<OpenDoorLog> list = openDoorLogDaoMapper.getList(doorLog);
		PageInfo<OpenDoorLog> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) pageSize);
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "" : 0 + "");
		activeResponseVo.setPages(pageNumTotal);
		activeResponseVo.setTotalNum((int) pageInfo.getTotal());
		activeResponseVo.setPageSize(pageSize);
		activeResponseVo.setOpenDoorLogList(list);
		activeResponseVo.setCstName(cstName);
		activeResponseVo.setCouponTotalNum(50);
		activeResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		activeResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		activeResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return activeResponseVo;
	}


	@ResponseBody
	@RequestMapping("/active/coupon/query.do")
	public ActiveResponseVo couponQuery(@RequestBody ActiveRequestVo activeRequestVo) {
		Integer pageNum = activeRequestVo.getPageNum();
		Integer pageSize = activeRequestVo.getPageSize();
		activeRequestVo.setPageNum(null);
		activeRequestVo.setPageSize(null);
		ActiveResponseVo activeResponseVo = new ActiveResponseVo();
		String cstCode = activeRequestVo.getCstCode();
		String wxOpenId = activeRequestVo.getWxOpenId();
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);

		PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
		CouponGrant couponGrant = new CouponGrant();
		couponGrant.setWxOpenId(activeRequestVo.getWxOpenId());
		couponGrant.setCstCode(activeRequestVo.getCstCode());
		List<CouponGrant> list = couponGrantDaoMapper.getList(couponGrant);
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

}

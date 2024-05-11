package com.ej.hgj.controller.bill;

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
import com.ej.hgj.dao.cst.CstPayPerDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoHouseDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.tag.TagCstDaoMapper;
import com.ej.hgj.entity.bill.*;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.cst.CstPayPer;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoHouse;
import com.ej.hgj.entity.tag.TagCst;
import com.ej.hgj.entity.visit.VisitPass;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.service.bill.BillService;
import com.ej.hgj.sy.dao.bill.BillDaoMapper;
import com.ej.hgj.sy.dao.bill.item.BillItemDaoMapper;
import com.ej.hgj.sy.dao.bill.year.BillYearDaoMapper;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.RandomNumberGenerator;
import com.ej.hgj.utils.bill.*;
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
public class BillController extends BaseController {

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

	/**
	 * 查询客户欠费总金额
	 */
	@ResponseBody
	@RequestMapping("/queryPriRev.do")
	public BillResponseVo queryPriRev(@RequestBody BillRequestVo billRequestVo) {
		BillResponseVo billResponseVo = new BillResponseVo();
		billRequestVo.setPageNum(null);
		billRequestVo.setPageSize(null);
		String cstCode = billRequestVo.getCstCode();
		String proNum = billRequestVo.getProNum();
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		billRequestVo.setCstId(hgjCst.getId());
		List<String> houseIdList = new ArrayList<>();
		// 查询委托人、住户已入住房间
		List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(billRequestVo.getCstCode(), billRequestVo.getWxOpenId());
		for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
			houseIdList.add(cstIntoHouse.getHouseId());
		}
		billRequestVo.setHouseIdList(houseIdList);

		// 获取需要查询的费用类型列表,数据库配置
		List<String> billItemNameList = new ArrayList<>();
		List<BillItem> billItemNames = hgjBillItemDaoMapper.getList(proNum);
		for(BillItem billItem : billItemNames){
			billItemNameList.add(billItem.getItemName());
		}
		billRequestVo.setBillItemNameList(billItemNameList);

		// 获取项目名
		ProConfig proConfig = proConfDaoMapper.getByProjectNum(billRequestVo.getProNum());
		String proName = proConfig.getProjectName();
		billResponseVo.setProName(proName);
		Bill bill = billDaoMapper.priRevAmount(billRequestVo);
		BigDecimal priRev = new BigDecimal(0);
		if(bill != null){
			priRev = bill.getPriRev();
		}
		billResponseVo.setPriRevAmount(priRev);
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	/**
	 * 账单查询
	 */
	@ResponseBody
	@RequestMapping("/queryBill.do")
	public BillResponseVo queryBill(@RequestBody BillRequestVo billRequestVo) {
		Integer pageNum = billRequestVo.getPageNum();
		Integer pageSize = billRequestVo.getPageSize();
		billRequestVo.setPageNum(null);
		billRequestVo.setPageSize(null);
		BillResponseVo billResponseVo = new BillResponseVo();
		String cstCode = billRequestVo.getCstCode();
		String proNum = billRequestVo.getProNum();
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		billRequestVo.setCstId(hgjCst.getId());
		List<String> houseIdList = new ArrayList<>();
		// 查询委托人、住户已入住房间
		List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(billRequestVo.getCstCode(), billRequestVo.getWxOpenId());
		for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
			houseIdList.add(cstIntoHouse.getHouseId());
		}
		billRequestVo.setHouseIdList(houseIdList);

		// 获取需要查询的费用类型列表,数据库配置
		List<String> billItemNameList = new ArrayList<>();
		List<BillItem> billItemNames = hgjBillItemDaoMapper.getList(proNum);
		for(BillItem billItem : billItemNames){
			billItemNameList.add(billItem.getItemName());
		}
		billRequestVo.setBillItemNameList(billItemNameList);

		// 获取费用类型
		List<BillItem> billItemList_09 = billItemDaoMapper.getBillItem_09(billRequestVo);
		List<BillItem> billItemList_0 = billItemDaoMapper.getBillItem_0(billRequestVo);
//		billItemList_09 = billItemList_09.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(BillItem::getYear).thenComparing(BillItem::getIpItemName))), ArrayList::new));
		for(BillItem billItem : billItemList_09){
			Set<Integer> randomNum = RandomNumberGenerator.generateRandomNumbers(100000, 999999, 1);
			String randNum = null;
			for(Integer integer : randomNum){
				randNum = integer.toString();
			}
			billItem.setId(randNum);
			billItem.setChecked(false);
			billItem.setIsHidden(true);
			billItem.setBindAll(false);
			billItem.setTitle(billItem.getIpItemName());
			List<BillItem> billItem_0 = billItemList_0.stream().filter(billItem0 -> billItem0.getYear().equals(billItem.getYear()) && billItem0.getIpItemName().equals(billItem.getIpItemName())).collect(Collectors.toList());
			if(!billItem_0.isEmpty()){
				billItem.setPriRev(billItem_0.get(0).getPriRev());
			}else {
				billItem.setPriRev(new BigDecimal("0"));
			}
		}

		// 获取月账单
		List<Bill> billMonthList_09 = billDaoMapper.getMonthBill_09(billRequestVo);
		List<Bill> billMonthList_0 = billDaoMapper.getMonthBill_0(billRequestVo);
		for (Bill b : billMonthList_09){
			Set<Integer> randomNum = RandomNumberGenerator.generateRandomNumbers(100000, 999999, 1);
			String randNum = null;
			for(Integer integer : randomNum){
				randNum = integer.toString();
			}
			b.setId(randNum);
			b.setChecked(false);
			b.setYear(b.getRepYears().substring(0,4));
			b.setTitle(b.getRepYears().substring(0,4) + "年" + b.getRepYears().substring(5,7) + "月");
			List<Bill> bill_0 = billMonthList_0.stream().filter(bill0 -> bill0.getRepYears().equals(b.getRepYears()) && bill0.getIpItemName().equals(b.getIpItemName())).collect(Collectors.toList());
			if(!bill_0.isEmpty()){
				b.setPriRev(bill_0.get(0).getPriRev());
			}else {
				b.setPriRev(new BigDecimal("0"));
			}
		}
		for(BillItem billItem : billItemList_09){
			List<Bill> billMonths = billMonthList_09.stream().filter(bill -> bill.getYear().equals(billItem.getYear()) && bill.getIpItemName().equals(billItem.getIpItemName())).collect(Collectors.toList());
			billItem.setChildren(billMonths);
		}

		// 获取年账单
		List<BillYear> billYearList_0 = billYearDaoMapper.getBillYear_0(billRequestVo);
		PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
		billRequestVo.setPageNum(pageNum);
		billRequestVo.setPageSize(pageSize);
		List<BillYear> billYearList_09 = billYearDaoMapper.getBillYear_09(billRequestVo);
		for(BillYear billYear : billYearList_09){
			Set<Integer> randomNum = RandomNumberGenerator.generateRandomNumbers(100000, 999999, 1);
			String randNum = null;
			for(Integer integer : randomNum){
				randNum = integer.toString();
			}
			billYear.setId(randNum);
			billYear.setChecked(false);
			billYear.setIsHidden(true);
			billYear.setBindAll(false);
			billYear.setTitle(billYear.getYear() + "年");
			List<BillYear> billYear_0 = billYearList_0.stream().filter(by_0 -> by_0.getYear().equals(billYear.getYear())).collect(Collectors.toList());
			if(!billYear_0.isEmpty()){
				billYear.setPriRev(billYear_0.get(0).getPriRev());
			}else {
				billYear.setPriRev(new BigDecimal("0"));
			}
			List<BillItem> billItems = billItemList_09.stream().filter(billItem -> billYear.getYear().equals(billItem.getYear())).collect(Collectors.toList());
			billYear.setChildren(billItems);
		}
		PageInfo<BillYear> pageInfo = new PageInfo<>(billYearList_09);
		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) pageSize);
		billYearList_09 = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(billYearList_09 != null ? billYearList_09.size() + "" : 0 + "");
		billResponseVo.setPages(pageNumTotal);
		billResponseVo.setTotalNum((int) pageInfo.getTotal());
		billResponseVo.setPageSize(pageSize);
		billResponseVo.setMenuTree(billYearList_09);
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	/**
	 * 大订单查询
	 */
	@ResponseBody
	@RequestMapping("/queryBillMerge.do")
	public BillResponseVo queryBillMerge(@RequestBody BillRequestVo billRequestVo) {
		BillResponseVo billResponseVo = new BillResponseVo();
		String cstCode = billRequestVo.getCstCode();
		// 查询客户是否有缴费开票权限
		Integer isPayment = 0;
		Integer isInvoice = 0;
//		List<CstPayPer> cstPayPerList = cstPayPerDaoMapper.findByCstCode(cstCode);
//		if(!cstPayPerList.isEmpty()){
//			List<CstPayPer> cstPayPerListPayFilter = cstPayPerList.stream().filter(cstPayPer -> 1 == cstPayPer.getFunctionId()).collect(Collectors.toList());
//			if(!cstPayPerListPayFilter.isEmpty()){
//				isPayment = 1;
//			}
//			List<CstPayPer> cstPayPerListInvFilter = cstPayPerList.stream().filter(cstPayPer -> 2 == cstPayPer.getFunctionId()).collect(Collectors.toList());
//			if(!cstPayPerListInvFilter.isEmpty()){
//				isInvoice = 1;
//			}
//		}
		// 查询缴费付款标签编号
		ConstantConfig payByKey = constantConfDaoMapper.getByKey(Constant.PAY_TAG_ID);
		TagCst tagCst = new TagCst();
		tagCst.setTagId(payByKey.getConfigValue());
		tagCst.setCstCode(cstCode);
		// 通过缴费编号查询客户是否有缴费标签
		List<TagCst> tagCstListPay = tagCstDaoMapper.getList(tagCst);
		if(!tagCstListPay.isEmpty()){
			isPayment = 1;
		}
		// 查询缴费开票标签编号
		ConstantConfig invByKey = constantConfDaoMapper.getByKey(Constant.INV_TAG_ID);
		// 通过缴费编号查询客户是否有开票标签
		tagCst.setTagId(invByKey.getConfigValue());
		List<TagCst> tagCstListInv= tagCstDaoMapper.getList(tagCst);
		if(!tagCstListInv.isEmpty()){
			isInvoice = 1;
		}
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		billRequestVo.setCstId(hgjCst.getId());
		PageHelper.offsetPage((billRequestVo.getPageNum() - 1) * billRequestVo.getPageSize(), billRequestVo.getPageSize());
		List<BillMerge> list = billMergeDaoMapper.getList(billRequestVo);
		if(!list.isEmpty()) {
			for (BillMerge billMerge : list) {
				// 支付权限
				billMerge.setIsPayment(isPayment);
				// 开票权限
				billMerge.setIsInvoice(isInvoice);
				// 支付时限
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(billMerge.getCreateTime());
				// 将时间向后调整1小时
				// calendar.add(Calendar.HOUR_OF_DAY, + 1);
				// 将时间向后调整10分钟
				calendar.add(Calendar.MINUTE,10);
				billMerge.setTimeLimit(calendar.getTime());
			}
		}
		PageInfo<BillMerge> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) billRequestVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "" : 0 + "");
		billResponseVo.setPages(pageNumTotal);
		billResponseVo.setTotalNum((int) pageInfo.getTotal());
		billResponseVo.setPageSize(billRequestVo.getPageSize());
		billResponseVo.setBillMergeList(list);
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	/**
	 * 查询大订单详细
	 */
	@ResponseBody
	@RequestMapping("/queryBillDetail.do")
	public BillResponseVo queryBillDetail(@RequestBody BillRequestVo billRequestVo) {
		BillResponseVo billResponseVo = new BillResponseVo();
		BillMergeDetail billMergeDetail = new BillMergeDetail();
		billMergeDetail.setCstCode(billRequestVo.getCstCode());
		billMergeDetail.setBillId(billRequestVo.getId());
		List<BillMergeDetail> mergeDetailList = billMergeDetailDaoMapper.getList(billMergeDetail);
		List<String> billOrders = new ArrayList<>();
		if(!mergeDetailList.isEmpty()){
			for(BillMergeDetail b : mergeDetailList){
				billOrders.add(b.getOrderNo());
			}
		}
		PageHelper.offsetPage((billRequestVo.getPageNum() - 1) * billRequestVo.getPageSize(), billRequestVo.getPageSize());
		billRequestVo.setBillIds(billOrders);
		List<Bill> list = billDaoMapper.getOrderByIds(billRequestVo);
		PageInfo<Bill> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) billRequestVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "" : 0 + "");
		billResponseVo.setPages(pageNumTotal);
		billResponseVo.setTotalNum((int) pageInfo.getTotal());
		billResponseVo.setPageSize(billRequestVo.getPageSize());
		billResponseVo.setBillList(list);
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	/**
	 * 查询月账单详细
	 */
	@ResponseBody
	@RequestMapping("/queryBillMonthDetail.do")
	public BillResponseVo queryBillMonthDetail(@RequestBody BillRequestVo billRequestVo) {
		Integer pageNum = billRequestVo.getPageNum();
		Integer pageSize = billRequestVo.getPageSize();
		billRequestVo.setPageNum(null);
		billRequestVo.setPageSize(null);
		BillResponseVo billResponseVo = new BillResponseVo();
		String cstCode = billRequestVo.getCstCode();
		String wxOpenId = billRequestVo.getWxOpenId();
		String repYears = billRequestVo.getRepYears();
		String ipItemName = billRequestVo.getIpItemName();
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		billRequestVo.setCstId(hgjCst.getId());
		List<String> houseIdList = new ArrayList<>();
		// 查询委托人、住户已入住房间
		List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(billRequestVo.getCstCode(), billRequestVo.getWxOpenId());
		for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
			houseIdList.add(cstIntoHouse.getHouseId());
		}
		billRequestVo.setHouseIdList(houseIdList);

		// 头部显示数据-月
		Integer month = Integer.valueOf(repYears.substring(5, 7));
		// 头部显示数据-已缴金额
		BigDecimal billMonthAmountPaidIn = new BigDecimal(0);
		billRequestVo.setLockLogo("1");
		Bill paidIn = billDaoMapper.getBillMonthAmount(billRequestVo);
		if(paidIn != null){
			billMonthAmountPaidIn= paidIn.getPriRev();
		}
		// 头部显示数据-账单总金额
		BigDecimal billMonthAmountTotal = new BigDecimal(0);
		billRequestVo.setLockLogo(null);
		Bill total = billDaoMapper.getBillMonthAmount(billRequestVo);
		if(total != null){
			billMonthAmountTotal = total.getPriRev();
		}
		billRequestVo.setRepYears(repYears);
		billRequestVo.setIpItemName(ipItemName);
		PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
		List<Bill> list = billDaoMapper.getList(billRequestVo);
		PageInfo<Bill> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) pageSize);
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "" : 0 + "");
		billResponseVo.setPages(pageNumTotal);
		billResponseVo.setTotalNum((int) pageInfo.getTotal());
		billResponseVo.setPageSize(pageSize);
		billResponseVo.setBillList(list);
		billResponseVo.setMonth(month.toString());
		billResponseVo.setYearMonth(repYears.substring(0,4) + "年" + repYears.substring(5,7) + "月");
		billResponseVo.setBillMonthAmountPaidIn(billMonthAmountPaidIn);
		billResponseVo.setBillMonthAmountTotal(billMonthAmountTotal);
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	/**
	 * 缴费下单
	 */
	@ResponseBody
	@RequestMapping("/placeOrder.do")
	public BillResponseVo placeOrder(@RequestBody BillRequestVo billRequestVo) throws Exception {
		BillResponseVo billResponseVo = new BillResponseVo();
		String cstCode = billRequestVo.getCstCode();
		String wxOpenId = billRequestVo.getWxOpenId();
		String proNum = billRequestVo.getProNum();
		String orderNo = billRequestVo.getId();
		BigDecimal priRev = billRequestVo.getPriRev();
		if(StringUtils.isBlank(cstCode)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(wxOpenId)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(proNum)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(orderNo)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
			return billResponseVo;
		}
		if(priRev == null || priRev.compareTo(BigDecimal.ZERO) <= 0 ){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespDesc());
			return billResponseVo;
		}
		// 检查客户是否有缴费权限
//		List<CstPayPer> cstPayPerList = cstPayPerDaoMapper.findByCstCode(cstCode);
//		if(cstPayPerList.isEmpty()){
//			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
//			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespCode());
//			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespDesc());
//			return billResponseVo;
//		}

		// 查询缴费付款标签编号
		ConstantConfig payByKey = constantConfDaoMapper.getByKey(Constant.PAY_TAG_ID);
		TagCst tagCst = new TagCst();
		tagCst.setTagId(payByKey.getConfigValue());
		tagCst.setCstCode(cstCode);
		// 通过缴费编号查询客户是否有缴费标签
		List<TagCst> tagCstListPay = tagCstDaoMapper.getList(tagCst);
		if(tagCstListPay.isEmpty()){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespDesc());
			return billResponseVo;
		}

		// 1-表示有缴费权限  2-表示开票权限
//		List<CstPayPer> cstPayPerListPayFilter = cstPayPerList.stream().filter(cstPayPer -> 1 == cstPayPer.getFunctionId()).collect(Collectors.toList());
//		if(cstPayPerListPayFilter.isEmpty()){
//			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
//			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespCode());
//			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespDesc());
//			return billResponseVo;
//		}
		// 预支付交易会话标识,该值有效期为2小时
		String prepayId = null;
		// 预下单成功返回，签名用
		String sinPrepayId = null;
		logger.info("缴费订单号:" + orderNo + "||缴费金额:" + billRequestVo.getPriRev());
		if(StringUtils.isBlank(orderNo)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
			return billResponseVo;
		}
		// 支付成功订单，不允许重复下单
		List<Integer> paymentStatusList_2 = new ArrayList<>();
		paymentStatusList_2.add(Constant.PAYMENT_STATUS_SUCCESS);
		RequestPaymentStatusVo requestPaymentStatusVo_2 = new RequestPaymentStatusVo();
		requestPaymentStatusVo_2.setCstCode(cstCode);
		requestPaymentStatusVo_2.setProNum(proNum);
		requestPaymentStatusVo_2.setWxOpenId(wxOpenId);
		requestPaymentStatusVo_2.setOrderNo(orderNo);
		requestPaymentStatusVo_2.setPaymentStatusList(paymentStatusList_2);
		PaymentRecord paymentRecord_2 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_2);
		if (paymentRecord_2 != null){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_REPEAT_ORDER.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_REPEAT_ORDER.getRespDesc());
			return billResponseVo;
		}
		// 查询支付中订单-不允许下单
		List<Integer> paymentStatusList_1 = new ArrayList<>();
		paymentStatusList_1.add(Constant.PAYMENT_STATUS_PRO);
		RequestPaymentStatusVo requestPaymentStatusVo_1 = new RequestPaymentStatusVo();
		requestPaymentStatusVo_1.setCstCode(cstCode);
		requestPaymentStatusVo_1.setProNum(proNum);
		requestPaymentStatusVo_1.setWxOpenId(wxOpenId);
		requestPaymentStatusVo_1.setOrderNo(orderNo);
		requestPaymentStatusVo_1.setPaymentStatusList(paymentStatusList_1);
		PaymentRecord paymentRecord_1 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_1);
		if (paymentRecord_1 != null){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_ALREADY.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_ALREADY.getRespDesc());
			return billResponseVo;
		}
		// 非待支付状态不允许下单
		BillMerge billMerge = billMergeDaoMapper.getById(orderNo);
		 if(billMerge.getBillStatus() != Constant.BILL_STATUS_WAIT){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespDesc());
			return billResponseVo;
		}
		// 校验订单是否超时
		if(billService.updateStatusTimeOut(billMerge) == false){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_TIMEOUT.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_TIMEOUT.getRespDesc());
			return billResponseVo;
		}

		// 校验思源订单与智慧管家订单状态是否是支付成功，支付中，部分缴费，已缴费状态，这几种状态不能下单
		// 通过大订单ID查出小订单
		BillMergeDetail billMergeDetail = new BillMergeDetail();
		billMergeDetail.setBillId(orderNo);
		List<BillMergeDetail> billMergeDetailList = billMergeDetailDaoMapper.getList(billMergeDetail);
		List<String> orderIds = new ArrayList<>();
		if(!billMergeDetailList.isEmpty()){
			for(BillMergeDetail bmd : billMergeDetailList){
				orderIds.add(bmd.getOrderNo());
			}
		}
		BillRequestVo billReq = new BillRequestVo();
		billReq.setBillIds(orderIds);
		billReq.setPageNum(null);
		billReq.setPageSize(null);
		billReq.setLockLogo("0");
		// 思源系统订单校验
		List<Bill> billList = billDaoMapper.getOrderByIds(billReq);
		if(!billList.isEmpty()){
			for(Bill bill : billList){
//				if(bill.getLockLogo() == 9 || bill.getLockLogo() == 1){
//					billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
//					billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespCode());
//					billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespDesc());
//					return billResponseVo;
//				}
				// 智慧管家系统订单校验
				BillMergeDetail bm = new BillMergeDetail();
				List<Integer> billStatusList = new ArrayList<>();
				billStatusList.add(Constant.BILL_STATUS_SUCCESS);
				billStatusList.add(Constant.BILL_STATUS_PRO);
				bm.setOrderNo(bill.getId());
				bm.setBillStatusList(billStatusList);
				List<BillMergeDetail> list = billMergeDetailDaoMapper.getList(bm);
				if(!list.isEmpty()){
					billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
					billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespCode());
					billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespDesc());
					return billResponseVo;
				}
			}
		} else {
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_REPEAT.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_REPEAT.getRespDesc());
			return billResponseVo;
		}

		// 合并订单总金额与思源应收金额校验
		BigDecimal syPriRev = new BigDecimal("0");
		for(Bill bill : billList){
			if(bill.getLockLogo() == 0){
				syPriRev = syPriRev.add(bill.getPriRev());
			}
		}
		if(syPriRev.subtract(priRev).compareTo(BigDecimal.ZERO) != 0 ){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespDesc());
			return billResponseVo;
		}

		// 查询预支付成功订单
		List<Integer> paymentStatusList_0 = new ArrayList<>();
		paymentStatusList_0.add(Constant.PAYMENT_STATUS_PRE);
		RequestPaymentStatusVo requestPaymentStatusVo_0 = new RequestPaymentStatusVo();
		requestPaymentStatusVo_0.setCstCode(cstCode);
		requestPaymentStatusVo_0.setProNum(proNum);
		requestPaymentStatusVo_0.setWxOpenId(wxOpenId);
		requestPaymentStatusVo_0.setOrderNo(orderNo);
		requestPaymentStatusVo_0.setPaymentStatusList(paymentStatusList_0);
		PaymentRecord paymentRecord_0 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_0);
		if(paymentRecord_0 != null){
			prepayId = paymentRecord_0.getPrepayId();
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
			BigDecimal multiply = priRev.multiply(new BigDecimal("100"));
			int intTotalAmount = multiply.intValue();

			// --------------------根据项目号选择商户号--------------
			// 服务商模式-东方渔人码头
			if("10000".equals(proNum)){
				// 服务商户号-宜悦
				ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID_YY);
				signInfo.setSpMchId(spMchId.getConfigValue());
				// 子服务商户号-东方渔人码头
				ConstantConfig subMchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.SUB_MCH_ID);
				signInfo.setSubMchId(subMchId.getConfigValue());
			// 直连商户模式-大连路项目
//			}else if("10001".equals(proNum)){
//				// 直连商户号
//				ConstantConfig mchId = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.MCH_ID);
//				signInfo.setMchId(mchId.getConfigValue());
//			}
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
//			if("10000".equals(proNum)){
//				prePayUrl = Constant.PREPAY_URL;
//				params = buildPlaceOrder(signInfo.getAppId(), signInfo.getSpMchId(),
//						signInfo.getSubMchId(), billRequestVo.getIpItemName(), orderNo,
//						Constant.CALLBACK_URL, intTotalAmount, billRequestVo.getWxOpenId());
//			}else if("10001".equals(proNum)){
//				prePayUrl = Constant.PREPAY_URL_BUS;
//				params = buildPlaceOrderBus(signInfo.getMchId(), orderNo,
//						signInfo.getAppId(), billRequestVo.getIpItemName(),
//						Constant.CALLBACK_URL_BUS, intTotalAmount, billRequestVo.getWxOpenId());
//			}
			prePayUrl = Constant.PREPAY_URL;

			params = buildPlaceOrder(signInfo.getSpAppId(), signInfo.getSubAppId(), signInfo.getSpMchId(),
					signInfo.getSubMchId(), billRequestVo.getIpItemName(), orderNo,
					Constant.CALLBACK_URL, intTotalAmount, billRequestVo.getWxOpenId());
			HttpUrl httpurl = HttpUrl.parse(prePayUrl);
			// 获取证书token
			String authorization = billService.getToken("POST", httpurl, params, signInfo, proNum);
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
				logger.info("下单成功返回prepay_id:" + prepay_id + "||单号:" + billRequestVo.getId() + "||支付金额:" + intTotalAmount + "分");
			} else {
				// 下单失败
				billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
				billResponseVo.setErrCode(jsonObject.getString("code"));
				billResponseVo.setErrDesc(jsonObject.getString("message"));
				logger.info("下单失败单号:" + billRequestVo.getId() + "||message:" + jsonObject.getString("message"));
				return billResponseVo;
			}
		}
		// 生成签名
		String paySign = getPaySign(signInfo,sinPrepayId, proNum);
		if(StringUtils.isNotBlank(paySign)){
			logger.info("生成签名成功返回paySign:" + paySign + "||单号:" + billRequestVo.getId());
			signInfo.setPaySign(paySign);
		}else {
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.SIGNATURE_FAILED.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.SIGNATURE_FAILED.getRespDesc());
			logger.info("生成签名失败单号:" + billRequestVo.getId());
			return billResponseVo;
		}
		billResponseVo.setSignInfoVo(signInfo);
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		// 插入缴费记录 - 没有预下单或者预下单超出两个小时
		if(StringUtils.isBlank(prepayId)) {
			savePayment(billRequestVo, sinPrepayId);
		}
		return billResponseVo;
	}

	/**
	 * 合并账单
	 */
	@ResponseBody
	@RequestMapping("/mergeBill.do")
	public BillResponseVo mergeBill(@RequestBody BillRequestVo billRequestVo) {
		BillResponseVo billResponseVo = new BillResponseVo();
		billRequestVo.setPageSize(null);
		billRequestVo.setPageNum(null);
		String cstCode = billRequestVo.getCstCode();
		String wxOpenId = billRequestVo.getWxOpenId();
		String proNum = billRequestVo.getProNum();
		if(StringUtils.isBlank(cstCode)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(wxOpenId)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(proNum)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespDesc());
			return billResponseVo;
		}
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		List<String> houseIdList = new ArrayList<>();
		// 查询委托人、住户已入住房间
		List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(billRequestVo.getCstCode(), billRequestVo.getWxOpenId());
		for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
			houseIdList.add(cstIntoHouse.getHouseId());
		}
		billRequestVo.setHouseIdList(houseIdList);

		// 缴费订单号
		String[] orderIds = null;
		List<String> billIds = new ArrayList<>();
		// 根据账期，类型获取账单
		List<Bill> monthBillList = billRequestVo.getCheckData();
		if(!monthBillList.isEmpty()){
			for(Bill monthBill : monthBillList){
				billRequestVo.setCstId(hgjCst.getId());
				billRequestVo.setRepYears(monthBill.getRepYears());
				billRequestVo.setIpItemName(monthBill.getIpItemName());
				List<Bill> billList = billDaoMapper.getList(billRequestVo);
				for(Bill bill : billList){
					if(bill.getLockLogo() == 0){
						billIds.add(bill.getId());
					}
				}
			}
		}
		orderIds = billIds.toArray(new String[0]);
		//orderIds = billRequestVo.getOrderIds();
		BigDecimal totalAmount = billRequestVo.getTotalAmount();

		if(orderIds == null || orderIds.length == 0){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
			return billResponseVo;
		}

		if(totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0 ){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespDesc());
			return billResponseVo;
		}

		// 被合并订单在智慧管家中不能是(支付成功、支付中、待支付)
		BillRequestVo billReq = new BillRequestVo();
		billReq.setBillIds(Arrays.stream(orderIds).collect(Collectors.toList()));
		billReq.setPageNum(null);
		billReq.setPageSize(null);
		billReq.setLockLogo("0");
		// 思源系统订单校验
		List<Bill> billList = billDaoMapper.getOrderByIds(billReq);
		if(!billList.isEmpty()){
			for(Bill bill : billList){
//				if(bill.getLockLogo() == 9 || bill.getLockLogo() == 1){
//					billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
//					billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespCode());
//					billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespDesc());
//					return billResponseVo;
//				}
				// 智慧管家系统订单校验
				BillMergeDetail billMergeDetail = new BillMergeDetail();
				List<Integer> billStatusList = new ArrayList<>();
				billStatusList.add(Constant.BILL_STATUS_SUCCESS);
				billStatusList.add(Constant.BILL_STATUS_PRO);
				billStatusList.add(Constant.BILL_STATUS_WAIT);
				billMergeDetail.setOrderNo(bill.getId());
				billMergeDetail.setBillStatusList(billStatusList);
				List<BillMergeDetail> list = billMergeDetailDaoMapper.getList(billMergeDetail);
				if(!list.isEmpty()){
					// 他人未支付订单提示有账单正在支付中
					List<BillMergeDetail> listFilter = list.stream().filter(bmd -> bmd.getWxOpenId().equals(wxOpenId)).collect(Collectors.toList());
					if(listFilter.isEmpty()){
						billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
						billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_IN.getRespCode());
						billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_IN.getRespDesc());
						return billResponseVo;
					}else {
						// 本人未支付订单跳转支付页面
						billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
						billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespCode());
						billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_STATUS_ERROR.getRespDesc());
						return billResponseVo;
					}
				}
			}
		}else {
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_REPEAT.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_REPEAT.getRespDesc());
			return billResponseVo;
		}

		// 合并订单总金额与思源应收金额校验
		BigDecimal syPriRev = new BigDecimal("0");
		for(Bill bill : billList){
			syPriRev = syPriRev.add(bill.getPriRev());
		}
		if(syPriRev.subtract(totalAmount).compareTo(BigDecimal.ZERO) != 0 ){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_AMOUNT_ERROR.getRespDesc());
			return billResponseVo;
		}

		// 合并已勾选的账单
		billService.billMerge(proNum, wxOpenId, cstCode, totalAmount, orderIds);

		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	/**
	 * 取消账单
	 */
	@ResponseBody
	@RequestMapping("/cancelBill.do")
	public BillResponseVo cancelBill(@RequestBody BillRequestVo billRequestVo) {
		BillResponseVo billResponseVo = new BillResponseVo();
		String cstCode = billRequestVo.getCstCode();
		String wxOpenId = billRequestVo.getWxOpenId();
		String proNum = billRequestVo.getProNum();
		String id = billRequestVo.getId();
		if(StringUtils.isBlank(cstCode)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(wxOpenId)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(proNum)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespDesc());
			return billResponseVo;
		}
		if(StringUtils.isBlank(id)){
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
			return billResponseVo;
		}
		// 取消订单,待支付，预下单，的可以取消
		BillMerge billMerge = billMergeDaoMapper.getById(id);
		if(billMerge.getBillStatus() == Constant.BILL_STATUS_WAIT){
			billService.updateStatusCancel(id);
		}else {
			billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_CANCEL_FAIL.getRespCode());
			billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_CANCEL_FAIL.getRespDesc());
			return billResponseVo;
		}
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	// 组装参数下单参数-服务商  小程序主体是服务商
//	public String buildPlaceOrder(String sp_appid, String sp_mchid, String sub_mchid,
//								  String description,String out_trade_no,
//								  String notify_url,int total, String sp_openid
//								   ){
//		String spAppid = "{ \"sp_appid\":\"" + sp_appid + "\",";
//		String spMchid = "\"sp_mchid\":\"" + sp_mchid + "\",";
//		String subMchid = "\"sub_mchid\":\""+sub_mchid+"\",";
//		String desc = "\"description\":\"" + description + "\",";
//		String outTradeNo = "\"out_trade_no\":\"" + out_trade_no + "\",";
//		String notifyUrl = "\"notify_url\":\"" + notify_url + "\",";
//		String amount = "\"amount\":{ \"total\":"+ total +",\"currency\":\"CNY\"},";
//		String spOpenid = "\"payer\":{ \"sp_openid\":\"" +sp_openid + "\"}}";
//		return spAppid + spMchid + subMchid + desc  + outTradeNo + notifyUrl + amount + spOpenid;
//	}

	// 组装参数下单参数-服务商  小程序主体是特约商户
	public String buildPlaceOrder(String sp_appid, String sub_appid, String sp_mchid, String sub_mchid,
								  String description,String out_trade_no,
								  String notify_url,int total, String sub_openid
	){
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

	// 组装参数下单参数-直联商户
	public String buildPlaceOrderBus(String mchid, String out_trade_no,
									 String appid, String description,
									 String notify_url, int total, String openid
	){
		String mchId = "{ \"mchid\":\"" + mchid + "\",";
		String outTradeNo = "\"out_trade_no\":\"" + out_trade_no + "\",";
		String appId = "\"appid\":\"" + appid + "\",";
		String desc = "\"description\":\"" + description + "\",";
		String notifyUrl = "\"notify_url\":\"" + notify_url + "\",";
		String amount = "\"amount\":{ \"total\":"+ total +",\"currency\":\"CNY\"},";
		String payer = "\"payer\":{ \"openid\":\"" +openid + "\"}}";
		return mchId + outTradeNo + appId + desc  + notifyUrl + amount + payer;
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

	public void savePayment(BillRequestVo billRequestVo, String sinPrepayId){
		PaymentRecord paymentRecord = new PaymentRecord();
		paymentRecord.setId(TimestampGenerator.generateSerialNumber());
		paymentRecord.setOrderNo(billRequestVo.getId());
		paymentRecord.setPrepayId(sinPrepayId);
		paymentRecord.setProNum(billRequestVo.getProNum());
		paymentRecord.setWxOpenId(billRequestVo.getWxOpenId());
		paymentRecord.setCstCode(billRequestVo.getCstCode());
		BigDecimal multiply = billRequestVo.getPriRev().multiply(new BigDecimal("100"));
		paymentRecord.setAmountTotal(multiply.intValue());
		paymentRecord.setPriRev(billRequestVo.getPriRev());
		paymentRecord.setPriPaid(billRequestVo.getPriRev());
		paymentRecord.setIpItemName(billRequestVo.getIpItemName());
		paymentRecord.setPaymentStatus(Constant.PAYMENT_STATUS_PRE);
		paymentRecord.setCreateTime(new Date());
		paymentRecord.setUpdateTime(new Date());
		paymentRecord.setDeleteFlag(0);
		paymentRecordDaoMapper.save(paymentRecord);
	}

	/**
	 * 前端支付完成-修改状态为支付中
	 */
	@ResponseBody
	@RequestMapping("/paymentCompleted.do")
	public BillResponseVo paymentCompleted(@RequestBody BillRequestVo billRequestVo) throws Exception {
		BillResponseVo billResponseVo = new BillResponseVo();
		String orderNo = billRequestVo.getId();
		// 根据订单号查询支付记录表预支付成功的订单，修改为支付中
		List<Integer> paymentStatusList_0 = new ArrayList<>();
		paymentStatusList_0.add(Constant.PAYMENT_STATUS_PRE);
		RequestPaymentStatusVo requestPaymentStatusVo_0 = new RequestPaymentStatusVo();
		requestPaymentStatusVo_0.setOrderNo(orderNo);
		requestPaymentStatusVo_0.setPaymentStatusList(paymentStatusList_0);
		PaymentRecord paymentRecord_0 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_0);
		if(paymentRecord_0 != null){
			// 更新支付记录,大订单，小订单 为支付中
			billService.updateStatusPro(paymentRecord_0);
		}
		billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return billResponseVo;
	}

	@PostMapping(value = "/callBack")
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
			//decryptDataObj 为解码后的obj，其内容如下。之后便是验签成功后的业务处理
			//{
			//	"sp_appid": "wx8888888888888888",
			//	"sp_mchid": "1230000109",
			//	"sub_appid": "wxd678efh567hg6999",
			//	"sub_mchid": "1900000109",
			//	"out_trade_no": "1217752501201407033233368018",
			//	"trade_state_desc": "支付成功",
			//	"trade_type": "MICROPAY",
			//	"attach": "自定义数据",
			//	"transaction_id": "1217752501201407033233368018",
			//	"trade_state": "SUCCESS",
			//	"bank_type": "CMC",
			//	"success_time": "2018-06-08T10:34:56+08:00",
			//    ...
			//	"payer": {
			//		"openid": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"
			//	},
			//	"scene_info": {
			//		"device_id": "013467007045764"
			//	}
			//}
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
			// 商户订单号 - 大订单号
			String outTradeNo = decryptDataObj.getString("out_trade_no");
			List<Integer> paymentStatusList_01 = new ArrayList<>();
			paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRE);
			paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRO);
			RequestPaymentStatusVo requestPaymentStatusVo_01 = new RequestPaymentStatusVo();
			requestPaymentStatusVo_01.setOrderNo(outTradeNo);
			requestPaymentStatusVo_01.setPaymentStatusList(paymentStatusList_01);
			PaymentRecord paymentRecord = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_01);
			if(paymentRecord != null){
				paymentRecord.setTransactionId(transactionId);
				paymentRecord.setTradeType(tradeType);
				paymentRecord.setTradeState(tradeState);
				paymentRecord.setTradeStateDesc(tradeStateDesc);
				paymentRecord.setBankType(bankType);
				paymentRecord.setSuccessTime(successTime);
				paymentRecord.setUpdateTime(new Date());
				if("SUCCESS".equals(tradeState)){
					billService.updateStatusSuccess(paymentRecord);
				}else {
					billService.updateStatusFail(paymentRecord);
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

	@PostMapping(value = "/callBackBus")
	@ResponseBody
	public Map<String, String> callBackBus(@RequestBody JSONObject jsonObject) {
		logger.info("微信支付回调开始 jsonObject:" + jsonObject.toString());
		String method = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			//登录微信商户平台，进入【账户中心 > API安全 】目录，设置APIV3密钥
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
			// 商户订单号 - 大订单号
			String outTradeNo = decryptDataObj.getString("out_trade_no");
			List<Integer> paymentStatusList_01 = new ArrayList<>();
			paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRE);
			paymentStatusList_01.add(Constant.PAYMENT_STATUS_PRO);
			RequestPaymentStatusVo requestPaymentStatusVo_01 = new RequestPaymentStatusVo();
			requestPaymentStatusVo_01.setOrderNo(outTradeNo);
			requestPaymentStatusVo_01.setPaymentStatusList(paymentStatusList_01);
			PaymentRecord paymentRecord = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_01);
			if(paymentRecord != null){
				paymentRecord.setTransactionId(transactionId);
				paymentRecord.setTradeType(tradeType);
				paymentRecord.setTradeState(tradeState);
				paymentRecord.setTradeStateDesc(tradeStateDesc);
				paymentRecord.setBankType(bankType);
				paymentRecord.setSuccessTime(successTime);
				paymentRecord.setUpdateTime(new Date());
				if("SUCCESS".equals(tradeState)){
					billService.updateStatusSuccess(paymentRecord);
				}else {
					billService.updateStatusFail(paymentRecord);
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

	/**
	 * 账单查询
	 */
	/**@ResponseBody
	 @RequestMapping("/queryBill.do")
	 public BillResponseVo queryBill(@RequestBody BillRequestVo billRequestVo) {
	 BillResponseVo billResponseVo = new BillResponseVo();
	 String cstCode = billRequestVo.getCstCode();
	 HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
	 billRequestVo.setCstId(hgjCst.getId());
	 PageHelper.offsetPage((billRequestVo.getPageNum() - 1) * billRequestVo.getPageSize(), billRequestVo.getPageSize());
	 List<Bill> list = billDaoMapper.getList(billRequestVo);
	 //List<Bill> list = billDaoMapper.getListByRepYears(billRequestVo);
	 if(!list.isEmpty()) {
	 // 根据思源订单id查询惠管家订单状态
	 List<String> orderNoList = new ArrayList<>();
	 for (Bill bill : list) {
	 orderNoList.add(bill.getId());
	 }
	 // 思源中订单未缴费，惠管家是缴费中的订单修改显示状态位缴费中
	 //			List<PaymentRecord> listByOrderNo = paymentRecordDaoMapper.getListByOrderNo(orderNoList);
	 //			if (!listByOrderNo.isEmpty()) {
	 //				for (Bill bill : list) {
	 //					List<PaymentRecord> listByOrderNoFilter = listByOrderNo.stream().filter(order -> order.getOrderNo().equals(bill.getId())).sorted(Comparator.comparing(PaymentRecord::getUpdateTime).reversed()).collect(Collectors.toList());
	 //					if (!listByOrderNoFilter.isEmpty()) {
	 //						PaymentRecord paymentRecord = listByOrderNoFilter.get(0);
	 //						if (paymentRecord.getPaymentStatus() == Constant.PAYMENT_STATUS_PRO) {
	 //							bill.setLockLogo(paymentRecord.getPaymentStatus());
	 //						}
	 //					}
	 //				}
	 //			}
	 }

	 // 根据项目号、客户编号、账期查询缴费记录
	 //		for (Bill bill : list) {
	 //			PaymentRecord paymentRecord = new PaymentRecord();
	 //			paymentRecord.setProNum(billRequestVo.getProNum());
	 //			paymentRecord.setCstCode(cstCode);
	 //			paymentRecord.setRepYears(bill.getRepYears());
	 //			List<PaymentRecord> paymentRecordList = paymentRecordDaoMapper.getList(paymentRecord);
	 //			if(!paymentRecordList.isEmpty()){
	 //				if(Constant.PAYMENT_STATUS_ALREADY == paymentRecordList.get(0).getPaymentStatus()){
	 //					bill.setLockLogo(Constant.PAYMENT_STATUS_ALREADY);
	 //				}else {
	 //					// 设置为未缴费
	 //					bill.setLockLogo(0);
	 //				}
	 //			}else {
	 //				// 设置为未缴费
	 //				bill.setLockLogo(0);
	 //			}
	 //
	 //			// 根据项目号账期生成ID
	 //			if(!paymentRecordList.isEmpty()){
	 //				PaymentRecord payment = paymentRecordList.get(0);
	 //				// 插入已付金额
	 //				bill.setPriPaid(payment.getPriPaid());
	 //				String orderNo = payment.getOrderNo();
	 //				if(StringUtils.isBlank(orderNo)){
	 //					bill.setId(TimestampGenerator.generateSerialNumber());
	 //				}else {
	 //					bill.setId(orderNo);
	 //				}
	 //			}else {
	 //				bill.setId(TimestampGenerator.generateSerialNumber());
	 //			}
	 //		}
	 // 未缴费
	 //		if("0".equals(billRequestVo.getLockLogo())){
	 //			list = list.stream().filter(bill -> bill.getLockLogo() == 0).collect(Collectors.toList());
	 //		}
	 // 已缴费
	 //		if("1".equals(billRequestVo.getLockLogo())){
	 //			list = list.stream().filter(bill -> bill.getLockLogo() == 9).collect(Collectors.toList());
	 //		}
	 PageInfo<Bill> pageInfo = new PageInfo<>(list);
	 int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) billRequestVo.getPageSize());
	 list = pageInfo.getList();
	 logger.info("list返回记录数");
	 logger.info(list != null ? list.size() + "" : 0 + "");
	 billResponseVo.setPages(pageNumTotal);
	 billResponseVo.setTotalNum((int) pageInfo.getTotal());
	 billResponseVo.setPageSize(billRequestVo.getPageSize());
	 billResponseVo.setBillList(list);
	 billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
	 return billResponseVo;
	 }*/


	/**
	 * 缴费下单
	 */
	/**@ResponseBody
	 @RequestMapping("/placeOrder.do")
	 public BillResponseVo placeOrder(@RequestBody BillRequestVo billRequestVo) throws Exception {
	 BillResponseVo billResponseVo = new BillResponseVo();
	 String cstCode = billRequestVo.getCstCode();
	 String wxOpenId = billRequestVo.getWxOpenId();
	 String proNum = billRequestVo.getProNum();
	 String[] orderIds = billRequestVo.getOrderIds();
	 BigDecimal totalAmount = billRequestVo.getTotalAmount();
	 if(StringUtils.isBlank(cstCode)){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_CODE_NULL.getRespDesc());
	 return billResponseVo;
	 }
	 if(StringUtils.isBlank(wxOpenId)){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_WX_OPENID_NULL.getRespDesc());
	 return billResponseVo;
	 }
	 if(StringUtils.isBlank(proNum)){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_PRO_NUM_NULL.getRespDesc());
	 return billResponseVo;
	 }
	 if(orderIds == null || orderIds.length == 0){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
	 return billResponseVo;
	 }
	 if(totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0 ){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_TOTAL_AMOUNT_NULL.getRespDesc());
	 return billResponseVo;
	 }
	 // 校验前端金额合计与思源的金额合计是否一致


	 // 检查客户是否有缴费权限
	 List<CstPayPer> cstPayPerList = cstPayPerDaoMapper.findByCstCode(cstCode);
	 if(cstPayPerList.isEmpty()){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespDesc());
	 return billResponseVo;
	 }
	 // 1-标示有缴费权限  2-表示开票权限
	 List<CstPayPer> cstPayPerListPayFilter = cstPayPerList.stream().filter(cstPayPer -> 1 == cstPayPer.getFunctionId()).collect(Collectors.toList());
	 if(cstPayPerListPayFilter.isEmpty()){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_CST_NOT_PAYMENT.getRespDesc());
	 return billResponseVo;
	 }

	 // 预支付交易会话标识,该值有效期为2小时
	 String prepayId = null;
	 // 预下单成功返回，签名用
	 String sinPrepayId = null;
	 // 付款单编号
	 String payNo = null;
	 // 付款记录
	 PaymentRecord payRecord = null;

	 // 将订单号数组转为字符串作为查询参数
	 String orderIdsStr = String.join(", ", orderIds);
	 PaymentRecord paymentRecord = new PaymentRecord();
	 paymentRecord.setCstCode(cstCode);
	 paymentRecord.setProNum(proNum);
	 paymentRecord.setOrderNo(orderIdsStr);
	 List<PaymentRecord> paymentRecordList = paymentRecordDaoMapper.getList(paymentRecord);
	 if(paymentRecordList.isEmpty()){
	 payNo = TimestampGenerator.generateSerialNumber();
	 }else{
	 payRecord = paymentRecordList.get(0);
	 payNo = payRecord.getPayNo();
	 }

	 // String orderNo = billRequestVo.getId();
	 String orderNo = orderIdsStr;
	 // logger.info("缴费订单号:" + orderNo + "||缴费金额:" + billRequestVo.getPriRev());
	 logger.info("缴费订单号:" + orderNo + "||缴费金额:" + totalAmount);
	 if(StringUtils.isBlank(orderNo)){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_NO_NULL.getRespDesc());
	 return billResponseVo;
	 }
	 // 支付成功订单，不允许重复下单
	 List<Integer> paymentStatusList_9 = new ArrayList<>();
	 paymentStatusList_9.add(Constant.PAYMENT_STATUS_SUCCESS);
	 RequestPaymentStatusVo requestPaymentStatusVo_9 = new RequestPaymentStatusVo();
	 requestPaymentStatusVo_9.setCstCode(cstCode);
	 requestPaymentStatusVo_9.setProNum(proNum);
	 requestPaymentStatusVo_9.setWxOpenId(wxOpenId);
	 requestPaymentStatusVo_9.setOrderNo(orderNo);
	 requestPaymentStatusVo_9.setPaymentStatusList(paymentStatusList_9);
	 PaymentRecord paymentRecord_9 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_9);
	 if (paymentRecord_9 != null){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_REPEAT_ORDER.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_REPEAT_ORDER.getRespDesc());
	 return billResponseVo;
	 }
	 // 查询支付中订单-不允许下单
	 List<Integer> paymentStatusList_3 = new ArrayList<>();
	 paymentStatusList_3.add(Constant.PAYMENT_STATUS_PRO);
	 RequestPaymentStatusVo requestPaymentStatusVo_3 = new RequestPaymentStatusVo();
	 requestPaymentStatusVo_3.setCstCode(cstCode);
	 requestPaymentStatusVo_3.setProNum(proNum);
	 requestPaymentStatusVo_3.setWxOpenId(wxOpenId);
	 requestPaymentStatusVo_3.setOrderNo(orderNo);
	 requestPaymentStatusVo_3.setPaymentStatusList(paymentStatusList_3);
	 PaymentRecord paymentRecord_3 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_3);
	 if (paymentRecord_3 != null){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_ORDER_ALREADY.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_ORDER_ALREADY.getRespDesc());
	 return billResponseVo;
	 }
	 // 查询预支付成功订单-两小时内可继续支付，超出时间重新下单
	 List<Integer> paymentStatusList_2 = new ArrayList<>();
	 paymentStatusList_2.add(Constant.PAYMENT_STATUS_PRE);
	 RequestPaymentStatusVo requestPaymentStatusVo_2 = new RequestPaymentStatusVo();
	 requestPaymentStatusVo_2.setCstCode(cstCode);
	 requestPaymentStatusVo_2.setProNum(proNum);
	 requestPaymentStatusVo_2.setWxOpenId(wxOpenId);
	 requestPaymentStatusVo_2.setOrderNo(orderNo);
	 requestPaymentStatusVo_2.setPaymentStatusList(paymentStatusList_2);
	 PaymentRecord paymentRecord_2 = paymentRecordDaoMapper.getPaymentRecord(requestPaymentStatusVo_2);
	 if(paymentRecord_2 != null){
	 // 两小时内未支付-直接用现有prepayId
	 boolean b = DateUtils.compareDate(paymentRecord_2.getCreateTime(), 2);
	 if(b){
	 prepayId = paymentRecord_2.getPrepayId();
	 }else {
	 //  超出两个小时，重新下单并且修改原有预支付订单状态为 7-超时未支付
	 PaymentRecord paymentRecord_7 = new PaymentRecord();
	 paymentRecord_7.setId(paymentRecord_2.getId());
	 paymentRecord_7.setPaymentStatus(Constant.PAYMENT_STATUS_TIMEOUT);
	 paymentRecord_7.setUpdateTime(new Date());
	 paymentRecordDaoMapper.update(paymentRecord_7);
	 }
	 }

	 // 订单编号ids
	 String[] ids = null;
	 if(payRecord != null){
	 ids = payRecord.getOrderNo().split(",");
	 }else {
	 ids = orderIds;
	 }

	 // 查询思源该订单是否已缴费
	 HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
	 BillRequestVo billRequest = new BillRequestVo();
	 billRequest.setCstId(hgjCst.getId());
	 billRequest.setProNum(proNum);
	 for(int i=0; i<ids.length; i++){
	 billRequest.setId(ids[i]);
	 List<Bill> list = billDaoMapper.getList(billRequest);
	 if(!list.isEmpty()){
	 Bill bill = list.get(0);
	 //list = list.stream().filter(b -> b.getLockLogo() == 1 || b.getLockLogo() == 9).collect(Collectors.toList());
	 // 已缴费或者部分缴费不允许下单
	 if("9".equals(bill.getLockLogo()) || "1".equals(bill.getLockLogo())){
	 //if(!list.isEmpty()){
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.PAYMENT_OTHER_PLATFORMS_ALREADY.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.PAYMENT_OTHER_PLATFORMS_ALREADY.getRespDesc()+":" + bill.getId());
	 return billResponseVo;
	 }
	 }
	 }

	 // 查询每笔订单的缴费状态,订单号模糊匹配付款单


	 ConstantConfig miniProgramApp = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP);
	 // 设置签名对象
	 SignInfoVo signInfo = new SignInfoVo();
	 // 微信小程序智慧管家appId
	 signInfo.setAppId(miniProgramApp.getAppId());
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
	 // BigDecimal priRev = billRequestVo.getPriRev();
	 // BigDecimal priRev = new BigDecimal("0.01");
	 // BigDecimal multiply = priRev.multiply(new BigDecimal("100"));
	 BigDecimal multiply = totalAmount.multiply(new BigDecimal("100"));
	 int intTotalAmount = multiply.intValue();
	 // 服务商户号
	 ConstantConfig spMchId = constantConfDaoMapper.getByKey(Constant.SP_MCH_ID);
	 signInfo.setSpMchId(spMchId.getConfigValue());
	 // 子服务商户号
	 ConstantConfig subMchId = constantConfDaoMapper.getByKey(Constant.SUB_MCH_ID);
	 signInfo.setSubMchId(subMchId.getConfigValue());
	 // 证书序列号
	 ConstantConfig serialNo = constantConfDaoMapper.getByKey(Constant.SERIAL_NO);
	 signInfo.setSerialNo(serialNo.getConfigValue());
	 billRequestVo.setIpItemName("账单缴费");
	 String params = buildPlaceOrder(signInfo.getAppId(), signInfo.getSpMchId(),
	 signInfo.getSubMchId(), billRequestVo.getIpItemName(), payNo,
	 Constant.CALLBACK_URL, intTotalAmount, billRequestVo.getWxOpenId());
	 HttpUrl httpurl = HttpUrl.parse(Constant.PREPAY_URL);
	 // 获取证书token
	 String authorization = billService.getToken("POST", httpurl, params, signInfo);
	 // JSAPI下单-服务商
	 authorization =  "WECHATPAY2-SHA256-RSA2048" + " " + authorization;
	 String result = HttpRequestUtils.post(Constant.PREPAY_URL, params, authorization);
	 // 下单返回值转json
	 JSONObject jsonObject = JSONObject.parseObject(result);
	 String prepay_id = jsonObject.getString("prepay_id");
	 sinPrepayId = prepay_id;
	 // 下单成功
	 if (StringUtils.isNotBlank(prepay_id)) {
	 signInfo.setRepayId("prepay_id=" + prepay_id);
	 logger.info("下单成功返回prepay_id:" + prepay_id + "||单号:" + billRequestVo.getId() + "||支付金额:" + totalAmount + "分");
	 } else {
	 // 下单失败
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(jsonObject.getString("code"));
	 billResponseVo.setErrDesc(jsonObject.getString("message"));
	 logger.info("下单失败单号:" + billRequestVo.getId() + "||message:" + jsonObject.getString("message"));
	 return billResponseVo;
	 }
	 }
	 // 生成签名
	 String paySign = getPaySign(signInfo,sinPrepayId);
	 if(StringUtils.isNotBlank(paySign)){
	 logger.info("生成签名成功返回paySign:" + paySign + "||单号:" + billRequestVo.getId());
	 signInfo.setPaySign(paySign);
	 }else {
	 billResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.SIGNATURE_FAILED.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.SIGNATURE_FAILED.getRespDesc());
	 logger.info("生成签名失败单号:" + billRequestVo.getId());
	 return billResponseVo;
	 }
	 billResponseVo.setSignInfoVo(signInfo);
	 billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
	 // 插入缴费记录 - 没有预下单或者预下单超出两个小时
	 if(StringUtils.isBlank(prepayId)) {
	 savePayment(billRequestVo, sinPrepayId, orderIdsStr, totalAmount, payNo);
	 }
	 return billResponseVo;
	 }*/

	/**
	 * 账单查询

	 @ResponseBody
	 @RequestMapping("/queryBill_0122.do")
	 public BillResponseVo queryBill_0122(@RequestBody BillRequestVo billRequestVo) {
	 Integer pageNum = billRequestVo.getPageNum();
	 Integer pageSize = billRequestVo.getPageSize();
	 BillResponseVo billResponseVo = new BillResponseVo();
	 String cstCode = billRequestVo.getCstCode();
	 String lockLogo = billRequestVo.getLockLogo();
	 billRequestVo.setPageNum(null);
	 billRequestVo.setPageSize(null);
	 HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
	 billRequestVo.setCstId(hgjCst.getId());
	 List<Bill> filterlist = new ArrayList<>();
	 List<String> houseIdList = new ArrayList<>();
	 // 查询租户入住的房屋
	 CstInto cstInto = new CstInto();
	 cstInto.setCstCode(cstCode);
	 cstInto.setWxOpenId(billRequestVo.getWxOpenId());
	 List<CstInto> cstIntos = cstIntoMapper.getList(cstInto);
	 if(!cstIntos.isEmpty()){
	 for (CstInto cst : cstIntos){
	 if(StringUtils.isNotBlank(cst.getHouseId()) && cst.getIntoRole() == Constant.INTO_ROLE_TENANT && cst.getIntoStatus() == Constant.INTO_STATUS_Y){
	 houseIdList.add(cst.getHouseId());
	 }
	 }
	 }
	 // 未交清
	 if("0".equals(lockLogo)){
	 // 思源订单列表
	 List<Bill> list = billDaoMapper.getList(billRequestVo);
	 if(!list.isEmpty()) {
	 List<String> orderNoList = new ArrayList<>();
	 for (Bill bill : list) {
	 orderNoList.add(bill.getId());
	 }
	 // 智慧管家订单列表
	 BillMergeDetail billMergeDetail = new BillMergeDetail();
	 List<Integer> billStatusList = new ArrayList<>();
	 billStatusList.add(Constant.BILL_STATUS_WAIT);
	 billStatusList.add(Constant.BILL_STATUS_PRO);
	 billStatusList.add(Constant.BILL_STATUS_SUCCESS);
	 billMergeDetail.setBillOrderNoList(orderNoList);
	 billMergeDetail.setBillStatusList(billStatusList);
	 List<BillMergeDetail> billMergeDetailList = billMergeDetailDaoMapper.getList(billMergeDetail);
	 // 取出待支付，支付中，支付成功的账单的订单号
	 List<String> billOrderNoList = new ArrayList<>();
	 if (!billMergeDetailList.isEmpty()) {
	 for (BillMergeDetail bm : billMergeDetailList) {
	 billOrderNoList.add(bm.getOrderNo());
	 }
	 }

	 // 思源订单列表
	 PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
	 billRequestVo.setBillOrderNoList(billOrderNoList);
	 billRequestVo.setHouseIdList(houseIdList);
	 billRequestVo.setPageNum(pageNum);
	 billRequestVo.setPageSize(pageSize);
	 filterlist = billDaoMapper.getList(billRequestVo);
	 }

	 // 排除待支付，支付中，支付成功的账单
	 //			for (int i = list.size() - 1; i >= 0; i--) {
	 //				Bill bill = list.get(i);
	 //				List<BillMergeDetail> billMergeDetailListFilter = billMergeDetailList.stream().filter(mergeDetail -> bill.getId().equals(mergeDetail.getOrderNo())).collect(Collectors.toList());
	 //				if (!billMergeDetailListFilter.isEmpty()) {
	 //					list.remove(i);
	 //				}
	 //			}
	 // 已交清
	 }else if("1".equals(lockLogo)){
	 PageHelper.offsetPage((pageNum - 1) * pageSize, pageSize);
	 billRequestVo.setPageNum(pageNum);
	 billRequestVo.setPageSize(pageSize);
	 billRequestVo.setHouseIdList(houseIdList);
	 filterlist = billDaoMapper.getList(billRequestVo);
	 }else {
	 // 没有lockLogo状态的时候，比如查看账单详情
	 filterlist = billDaoMapper.getList(billRequestVo);
	 }

	 PageInfo<Bill> pageInfo = new PageInfo<>(filterlist);
	 int pageNumTotal = (int) Math.ceil((double) pageInfo.getTotal() / (double) pageSize);
	 filterlist = pageInfo.getList();
	 logger.info("list返回记录数");
	 logger.info(filterlist != null ? filterlist.size() + "" : 0 + "");
	 billResponseVo.setPages(pageNumTotal);
	 billResponseVo.setTotalNum((int) pageInfo.getTotal());
	 billResponseVo.setPageSize(billRequestVo.getPageSize());
	 billResponseVo.setBillList(filterlist);
	 billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
	 billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
	 billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
	 return billResponseVo;
	 }*/


	public static void main(String[] args) {
		//System.out.println(RandomStringUtils.getRandomStringByLength(32));
		String[] array = {"apple", "banana", "orange"};

		// 使用String.join()方法将数组转换成带有逗号分隔的字符串
		String result = String.join(", ", array);


		String s = "2024-09";
		System.out.println(s.substring(5,7));

	}
}

package com.ej.hgj.controller.electricity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.cst.CstMeterDaoMapper;
import com.ej.hgj.entity.carrenew.CarRenewOrder;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.cst.CstMeter;
import com.ej.hgj.entity.electricity.Electricity;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.vo.electricity.ElectricityVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ElectricityController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private CstMeterDaoMapper cstUserDaoMapper;


	/**
	 * 电费余额查询
	 * @param electricityVo
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping("/electricity/queryElectricitySurplus.do")
	@ResponseBody
	public ElectricityVo queryElectricitySurplus(@RequestBody ElectricityVo electricityVo) throws ParseException {
		CstMeter cstMeterPram = new CstMeter();
		cstMeterPram.setCstCode(electricityVo.getCstCode());
		cstMeterPram.setUserId(electricityVo.getUserId());
		List<CstMeter> cstMeterList = cstUserDaoMapper.getList(cstMeterPram);
		// 电表绑定验证
		if (cstMeterList.isEmpty()) {
			electricityVo.setRespCode("111");
			electricityVo.setErrDesc("客户未绑定电表,请咨询客服");
			return electricityVo;
		}
		// 获取客户电表编号拼接字符串
		String userId = "";
		for(CstMeter c : cstMeterList){
			userId += c.getUserId()+",";
		}
		StringBuilder userIds = new StringBuilder(userId);
		userIds.deleteCharAt(userIds.length() - 1);
		// 调用电费余额查询接口
		ConstantConfig constantConfig = constantConfDaoMapper.getByProNumAndKey(electricityVo.getProNum(), Constant.ELECTRICITY_API_URL);
		// 请求接口
		String url = constantConfig.getConfigValue() + "/GetRemainingElectricityBill?userIds=" + userIds + "";
		JSONObject jsonObject = JSONObject.parseObject(HttpClientUtil.doGet(url));
		String code = jsonObject.getString("code");
		String message = jsonObject.getString("message");
		String data = jsonObject.getString("data");
		logger.info("queryElectricitySurplus code：" + code + "||message：" + message);
		if (code.equals("0") && StringUtils.isNotBlank(data)) {
			List<Electricity> list = JSON.parseArray(data, Electricity.class);
			if(!list.isEmpty()) {
				for (Electricity e : list) {
					if (StringUtils.isNotBlank(e.getDateTime())) {
						LocalDateTime localDateTime = LocalDateTime.parse(e.getDateTime());
						Date dateTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
						String formatted = DateUtils.strYmdHms(dateTime);
						e.setDateTime(formatted);
					}
				}
			}
			logger.info("查询记录返回：" + list != null ? list.size() + "" : 0 + "");
			electricityVo.setList(list);
			electricityVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		} else {
			electricityVo.setRespCode("999");
			electricityVo.setErrDesc(message);
			return electricityVo;
		}
		return electricityVo;
	}

	/**
	 * 用电量查询
	 * @param electricityVo
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping("/electricity/queryElectricity.do")
	@ResponseBody
	public ElectricityVo queryElectricity(@RequestBody ElectricityVo electricityVo) throws ParseException {
		CstMeter cstMeterPram = new CstMeter();
		cstMeterPram.setCstCode(electricityVo.getCstCode());
		cstMeterPram.setUserId(electricityVo.getUserId());
		List<CstMeter> cstMeterList = cstUserDaoMapper.getList(cstMeterPram);
		// 电表绑定验证
		if (cstMeterList.isEmpty()) {
			electricityVo.setRespCode("111");
			electricityVo.setErrDesc("客户未绑定电表,请咨询客服");
			return electricityVo;
		}
		// 将符合要求的userId过滤出来，开始时间小于开户时间的用户编号
		List<CstMeter> cstMeterUserIdList = new ArrayList<>();
		for (CstMeter cstMeter : cstMeterList) {
			// 查询开始时间小于开户时间验证
			String startDate = electricityVo.getStartDate();
			String accountDate = cstMeter.getAccountDate();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date startTime = simpleDateFormat.parse(startDate);
			Date accountTime = simpleDateFormat.parse(accountDate);
			// 开始日期 >= 开户日期
			if (accountTime.before(startTime) || accountTime.equals(startTime)) {
				cstMeterUserIdList.add(cstMeter);
			}
		}
		// 将符合要求的userId循环调用接口拼接数据,封装到listAll
		List<Electricity> listAll = new ArrayList<>();
		if (!cstMeterUserIdList.isEmpty()) {
			for (CstMeter cstMeter : cstMeterUserIdList) {
				ConstantConfig constantConfig = constantConfDaoMapper.getByProNumAndKey(electricityVo.getProNum(), Constant.ELECTRICITY_API_URL);
				// 请求接口
				String url = constantConfig.getConfigValue() + "/GetElectricityBillsAsyncForApp?userId=" +
						cstMeter.getUserId() +
						"&startDate=" +
						electricityVo.getStartDate() +
						"&endDate=" +
						electricityVo.getEndDate() +
						"";
				JSONObject jsonObject = JSONObject.parseObject(HttpClientUtil.doGet(url));
				String code = jsonObject.getString("code");
				String message = jsonObject.getString("message");
				String data = jsonObject.getString("data");
				logger.info("queryElectricity code：" + code + "||message：" + message);
				if (code.equals("0") && StringUtils.isNotBlank(data)) {
					List<Electricity> list = JSON.parseArray(data, Electricity.class);
					listAll.addAll(list);
				} else {
					electricityVo.setRespCode("999");
					electricityVo.setErrDesc(message);
					return electricityVo;
				}
			}
		}

//		if(startTime.before(accountTime)){
//			electricityVo.setRespCode("999");
//			electricityVo.setErrDesc("开始日期不能小于开户日期");
//			return electricityVo;
//		}

		if(!listAll.isEmpty()) {
			// 根据房间去重复
			List<Electricity> list = listAll.stream()
					.collect(Collectors.collectingAndThen(
							Collectors.toMap(
									Electricity::getRoomId, // 根据RoomId去重
									Function.identity(), // 使用Electricity对象本身作为值，因为我们只需要去重，但保留完整对象
									(existing, replacement) -> existing // 选择现有的对象，因为我们只想保留一个对象
							),
							map -> new ArrayList<>(map.values()) // 将Map的values转换为List
					));
			// 每个房间用电量
			for (Electricity e : list) {
				Double sumUsedPower = listAll.stream() // 创建Stream
						.filter(a -> a.getRoomId().equals(e.getRoomId())) // 过滤条件
						.mapToDouble(Electricity::getUsedPower) // 将每个元素的用电量映射为double类型，用于求和
						.sum(); // 求和
				String sumUsedPowerStr = String.format("%.2f", sumUsedPower);
				e.setSumUsedPower(sumUsedPower);
				e.setSumUsedPowerStr(sumUsedPowerStr);
			}
			// 用电量总和
			Double totalUsedPower = list.stream() // 创建Stream
					.mapToDouble(Electricity::getSumUsedPower) // 将每个元素的price映射为double类型，用于求和
					.sum(); // 求和
			String totalUsedPowerStr = String.format("%.2f", totalUsedPower);
			logger.info("查询记录返回：" + list != null ? list.size() + "" : 0 + "");
			electricityVo.setList(list);
			electricityVo.setTotalUsedPower(totalUsedPowerStr);
		}
		electricityVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return electricityVo;
	}

	/**
	 * 用电明细
	 * @param electricityVo
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping("/electricity/queryElectricityDetail.do")
	@ResponseBody
	public ElectricityVo queryElectricityDetail(@RequestBody ElectricityVo electricityVo) throws ParseException {
		CstMeter cstMeterPram = new CstMeter();
		cstMeterPram.setCstCode(electricityVo.getCstCode());
		cstMeterPram.setUserId(electricityVo.getUserId());
		List<CstMeter> cstMeterList = cstUserDaoMapper.getList(cstMeterPram);		// 电表绑定验证
		if (cstMeterList.isEmpty()) {
			electricityVo.setRespCode("111");
			electricityVo.setErrDesc("客户未绑定电表,请咨询客服");
			return electricityVo;
		}
		// 将符合要求的userId过滤出来，开始时间小于开户时间的用户编号
		List<CstMeter> cstMeterUserIdList = new ArrayList<>();
		for (CstMeter cstMeter : cstMeterList) {
			// 查询开始时间小于开户时间验证
			String startDate = electricityVo.getStartDate();
			String accountDate = cstMeter.getAccountDate();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date startTime = simpleDateFormat.parse(startDate);
			Date accountTime = simpleDateFormat.parse(accountDate);
			// 开始日期 >= 开户日期
			if (accountTime.before(startTime) || accountTime.equals(startTime)) {
				cstMeterUserIdList.add(cstMeter);
			}
		}
		// 将符合要求的userId循环调用接口拼接数据,封装到listAll
		List<Electricity> listAll = new ArrayList<>();
		if (!cstMeterUserIdList.isEmpty()) {
			for (CstMeter cstMeter : cstMeterUserIdList) {
				ConstantConfig constantConfig = constantConfDaoMapper.getByProNumAndKey(electricityVo.getProNum(), Constant.ELECTRICITY_API_URL);
				// 请求接口
				String url = constantConfig.getConfigValue() + "/GetElectricityBillsAsyncForApp?userId=" +
						cstMeter.getUserId() +
						"&startDate=" +
						electricityVo.getStartDate() +
						"&endDate=" +
						electricityVo.getEndDate() +
						"";
				JSONObject jsonObject = JSONObject.parseObject(HttpClientUtil.doGet(url));
				String code = jsonObject.getString("code");
				String message = jsonObject.getString("message");
				String data = jsonObject.getString("data");
				logger.info("queryElectricity code：" + code + "||message：" + message);
				if (code.equals("0") && StringUtils.isNotBlank(data)) {
					List<Electricity> list = JSON.parseArray(data, Electricity.class);
					listAll.addAll(list);
				} else {
					electricityVo.setRespCode("999");
					electricityVo.setErrDesc(message);
					return electricityVo;
				}
			}
		}
		if(!listAll.isEmpty()) {
			List<Electricity> list = listAll.stream().filter(electricity -> electricity.getRoomId().equals(electricityVo.getRoomId())).collect(Collectors.toList());
			// 创建DateTimeFormatter来解析字符串
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			for (Electricity e : list) {
				// 原始字符串
				String dateTimeStr = e.getDate();
				// 解析字符串为LocalDateTime
				LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
				// 格式化LocalDateTime为其他格式
				String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				e.setDate(formattedDateTime);
			}
			logger.info("查询记录返回：" + list != null ? list.size() + "" : 0 + "");
			electricityVo.setList(list);
		}
		electricityVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		return electricityVo;
	}

	@ResponseBody
	@RequestMapping("/electricity/queryCurrentDate.do")
	public JSONObject queryCurrentDate(@RequestBody ElectricityVo electricityVo){
		JSONObject jsonObject = new JSONObject();
		ConstantConfig constantConfig = constantConfDaoMapper.getByProNumAndKey(electricityVo.getProNum(), Constant.ELECTRICITY_PICKER_BEFORE_MONTH);
		// 获取当前日期
		LocalDate today = LocalDate.now();
		// 往前推N个月
		LocalDate threeMonthsAgo = today.minus(Integer.valueOf(constantConfig.getConfigValue()), ChronoUnit.MONTHS);
		// 创建一个DateTimeFormatter对象，定义日期格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		// 格式化日期
		String sysTime = today.format(formatter);
		String beforeMonth = threeMonthsAgo.format(formatter);
		jsonObject.put("respCode", "000");
		jsonObject.put("sysTime",sysTime);
		jsonObject.put("beforeMonth",beforeMonth);
		return jsonObject;
	}

}


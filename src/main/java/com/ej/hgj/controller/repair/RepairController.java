package com.ej.hgj.controller.repair;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.AjaxResult;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.config.RepairConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoHouseDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.repair.RepairLogDaoMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.config.RepairConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.file.FileMessage;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoHouse;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.repair.RepairLog;
import com.ej.hgj.entity.workord.Material;
import com.ej.hgj.entity.workord.ReturnVisit;
import com.ej.hgj.entity.workord.WorkOrd;
import com.ej.hgj.entity.workord.WorkPos;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.sy.dao.house.SyHouseDaoMapper;
import com.ej.hgj.sy.dao.workord.MaterialDaoMapper;
import com.ej.hgj.sy.dao.workord.ReturnVisitDaoMapper;
import com.ej.hgj.sy.dao.workord.WorkOrdDaoMapper;
import com.ej.hgj.sy.dao.workord.WorkPosDaoMapper;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.GenerateUniqueIdUtil;
import com.ej.hgj.utils.SyPostClient;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.utils.file.FileSendClient;
import com.ej.hgj.vo.repair.RepairRequestVo;
import com.ej.hgj.vo.repair.RepairResponseVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新增报修接口
 * @author lx
 * @version $Id: RepairController.java, v 0.1 2020-8-14 下午2:10:48 lx Exp $
 */
@Controller
public class RepairController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${upload.path}")
	private String uploadPath;

	@Value("${upload.path.remote}")
	private String uploadPathRemote;

	@Autowired
	private WorkOrdDaoMapper workOrdDaoMapper;

	@Autowired
	private HgjHouseDaoMapper hgjHouseDaoMapper;

	@Autowired
	private SyHouseDaoMapper syHouseDaoMapper;

	@Autowired
	private ProConfDaoMapper proConfDaoMapper;

	@Autowired
	private WorkPosDaoMapper workPosDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private RepairLogDaoMapper repairLogDaoMapper;

	@Autowired
	private MaterialDaoMapper materialDaoMapper;

	@Autowired
	private ReturnVisitDaoMapper returnVisitDaoMapper;

	@Autowired
	private RepairConfDaoMapper repairConfDaoMapper;

	@Autowired
	private ConstantConfDaoMapper constantConfDaoMapper;

	@Autowired
	private CstIntoMapper cstIntoMapper;

	@Autowired
	private CstIntoHouseDaoMapper cstIntoHouseDaoMapper;

	@ResponseBody
	@RequestMapping("/repair.do")
	public RepairResponseVo repair(@RequestBody RepairRequestVo repairRequestVo) {
		RepairResponseVo repairResponseVo = new RepairResponseVo();
		// 报修类型
		String repairType = repairRequestVo.getRepairType();
		// 客户编号
		String cstCode = repairRequestVo.getCstCode();
		// 客户微信openId
		String wxOpenId = repairRequestVo.getWxOpenId();
		// 项目号
		String orgId = repairRequestVo.getProNum();

		if(StringUtils.isBlank(repairType) || StringUtils.isBlank(cstCode)
				|| StringUtils.isBlank(wxOpenId) || StringUtils.isBlank(orgId)){
			repairResponseVo.setRespCode(MonsterBasicRespCode.REQ_DATA_NULL.getReturnCode());
			repairResponseVo.setErrCode(JiasvBasicRespCode.DATA_NULL.getRespCode());
			repairResponseVo.setErrDesc(JiasvBasicRespCode.DATA_NULL.getRespDesc());
			return repairResponseVo;
		}
		// 客户其他信息
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		// 客户手机号
		//String phone = hgjCst.getMobile();
		String phone = repairRequestVo.getCstPhone();;
		// 客户id
		String cstId = hgjCst.getId();
		// 客户名称
		String cstName = hgjCst.getCstName();
		// 获取思源接口地址
		ConstantConfig constantConfig = constantConfDaoMapper.getByKey("sy_url");
		// 获取token
		String token = SyPostClient.getToken(constantConfig.getConfigValue());
		//String token = null;
		// 项目名
		ProConfig proConfig = proConfDaoMapper.getByProjectNum(orgId);
		String orgName = proConfig.getProjectName();
		// 房屋id
		String houseId = "";
		// 报修描述
		String desc = "";
		// 工作位置
		String workPos = "";
		// 报修规则对应表-SRules
		String woNoBasicId = "";
		String woNoBasicName = "";
		String quesTypeId = "";
		String quesTypeName = "";
		String budId = "";
		// 单号 GGBX20230830112-公共  KHBX20230801003-客户
		List<RepairConfig> repairConfigList = repairConfDaoMapper.getByProjectNum(orgId);
		String no = "";
		if("S".equals(repairType)){
			RepairConfig s_repairConfig = repairConfigList.stream().filter(repairConfig -> "S".equals(repairConfig.getRepairType())).collect(Collectors.toList()).get(0);
			woNoBasicId = s_repairConfig.getWoNoBasicId();
			woNoBasicName = s_repairConfig.getWoNoBasicName();
			quesTypeId = s_repairConfig.getQuesTypeId();
			quesTypeName = s_repairConfig.getQuesTypeName();
			// 查询思源单号,第六位以后加1
//			WorkOrd woNo = workOrdDaoMapper.getKhBxWoNo(orgId);
//			if(woNo == null){
//				no = "KHBX" + DateUtils.strYmd()+ "001";
//			}else {
//				String sub_5 = woNo.getWoNo().substring(0,6);
//				String subWoNo = woNo.getWoNo().substring(6);
//				String strWoNo = (Integer.parseInt(subWoNo) + 1) + "";
//				no = sub_5 + strWoNo;
//			}
			String repairNum = constantConfDaoMapper.getByKey(Constant.S_REPAIR_NUM).getConfigValue();
			String sysDate = DateUtils.strYmd();
			String sRepairNumDate = repairNum.substring(0,8);
			if(sysDate.equals(sRepairNumDate)){
				Long repairNumInt = Long.valueOf(repairNum);
				no = "KHBX" + (repairNumInt + 1);
				ConstantConfig config = new ConstantConfig();
				config.setConfigKey(Constant.S_REPAIR_NUM);
				config.setConfigValue((repairNumInt + 1) + "");
				constantConfDaoMapper.update(config);
			}else {
				ConstantConfig config = new ConstantConfig();
				config.setConfigKey(Constant.S_REPAIR_NUM);
				config.setConfigValue(sysDate + "500");
				constantConfDaoMapper.update(config);
				no = "KHBX" + config.getConfigValue();
			}
			houseId = repairRequestVo.getHouseId();
			HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
			budId = hgjHouse.getBudId();
			// 客户报修工作位置
			WorkPos s_workPos = workPosDaoMapper.getWorkPos(orgId, hgjHouse.getResCode());
			workPos = s_workPos.getWorkPos();
			desc = repairRequestVo.getRepairDesc();
		} else if("P".equals(repairType)){
			RepairConfig p_repairConfig = repairConfigList.stream().filter(repairConfig -> "P".equals(repairConfig.getRepairType())).collect(Collectors.toList()).get(0);
			woNoBasicId = p_repairConfig.getWoNoBasicId();
			woNoBasicName = p_repairConfig.getWoNoBasicName();
			quesTypeId = p_repairConfig.getQuesTypeId();
			quesTypeName = p_repairConfig.getQuesTypeName();
			HgjHouse hgjHouseParm = new HgjHouse();
			hgjHouseParm.setCstCode(cstCode);
			List<HgjHouse> list = hgjHouseDaoMapper.getListByCstCode(hgjHouseParm);
			//List<HgjHouse> list = syHouseDaoMapper.getListByCstCode(hgjHouseParm);
			if(!list.isEmpty()){
				HgjHouse house = list.get(0);
				houseId = house.getId();
			}
			HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
			budId = hgjHouse.getBudId();
			// 客户报修工作位置
			WorkPos s_workPos = workPosDaoMapper.getWorkPos(orgId, hgjHouse.getResCode());
			// 查询思源单号,第六位以后加1
//			WorkOrd woNo = workOrdDaoMapper.getGgBxWoNo(orgId);
//			if(woNo == null){
//				no = "GGBX" + DateUtils.strYmd()+ "001";
//			}else {
//				String sub_5 = woNo.getWoNo().substring(0,6);
//				String subWoNo = woNo.getWoNo().substring(6);
//				String strWoNo = (Integer.parseInt(subWoNo) + 1) + "";
//				no = sub_5 + strWoNo;
//			}
			String repairNum = constantConfDaoMapper.getByKey(Constant.P_REPAIR_NUM).getConfigValue();
			String sysDate = DateUtils.strYmd();
			String sRepairNumDate = repairNum.substring(0,8);
			if(sysDate.equals(sRepairNumDate)){
				Long repairNumInt = Long.valueOf(repairNum);
				no = "GGBX" + (repairNumInt + 1);
				ConstantConfig config = new ConstantConfig();
				config.setConfigKey(Constant.P_REPAIR_NUM);
				config.setConfigValue((repairNumInt + 1) + "");
				constantConfDaoMapper.update(config);
			}else {
				ConstantConfig config = new ConstantConfig();
				config.setConfigKey(Constant.P_REPAIR_NUM);
				config.setConfigValue(sysDate + "500");
				constantConfDaoMapper.update(config);
				no = "GGBX" + config.getConfigValue();
			}
			// 公共区域报修工作位置 001-办公楼  002-商场
			if("001".equals(hgjHouse.getGrpCode())){
				ConstantConfig config = constantConfDaoMapper.getByKey("001");
				//workPos = "东方渔人码头_办公楼_办公楼公共区域_办公楼公共区_办公楼公共区";
				workPos = config.getConfigValue();
			}else if("002".equals(hgjHouse.getGrpCode())){
				ConstantConfig config = constantConfDaoMapper.getByKey("002");
				//workPos = "东方渔人码头_商场_商场公共区域_商场公共区_商场公共区";
				workPos = config.getConfigValue();
			}
			desc = s_workPos.getWorkPos() + ";" +repairRequestVo.getRepairDesc();
		}

		// 图片
		String[] fileList = repairRequestVo.getFileList();
		// 发送请求
		//String p7 = initTicket2(orgId, orgName, cstCode, cstName, woNoBasicId, houseId, workPos, desc, phone, wxOpenId, budId, fileList);
		String p7 = initTicket(orgId, orgName, no, cstId, cstName, woNoBasicId, woNoBasicName, quesTypeId, quesTypeName, houseId, workPos, desc, desc, phone, fileList);
		logger.info("报修信息|项目号：" + orgId + "|报修单号：" + no + "|客户名称:" + cstName);
		// 获取请求结果-54号接口
		JSONObject jsonObject = SyPostClient.callSy1("User_Service_SaveWorkOrdInfoAndroid", p7, token, constantConfig.getConfigValue());
		// 4、报事上传接口
		//JSONObject jsonObject = SyPostClient.callSy("AppWeChat_GetMatterSubmit", p7, token, constantConfig.getConfigValue());

		String status = jsonObject.getString("status");
		String msg = jsonObject.getString("msg");
		//String status = "1";
		//String msg = "提交成功";
		if("1".equals(status)){
			repairResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
			repairResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
			repairResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
			// 保存报修记录
			RepairLog repairLog = new RepairLog();
			// 远程文件夹地址
			String folderPathRemote = uploadPathRemote+"/repair/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
			// 远程文件地址
			String filePathRemote = folderPathRemote + "/" + no+".txt";
			// 将图片数组转换为逗号分割的字符串
			repairLog.setImage(filePathRemote);
			repairLog.setId(TimestampGenerator.generateSerialNumber());
			repairLog.setProjectNum(orgId);
			repairLog.setProjectName(orgName);
			repairLog.setRepairNum(no);
			repairLog.setWxOpenId(wxOpenId);
			repairLog.setCstCode(cstCode);
			repairLog.setCstName(cstName);
			repairLog.setCstMobile(phone);
			repairLog.setHouseId(houseId);
			repairLog.setServiceType(woNoBasicName);
			repairLog.setQuesType(quesTypeName);
			repairLog.setRepairDesc(repairRequestVo.getRepairDesc());
			repairLog.setWorkPos(workPos);
			repairLog.setRepairType(repairRequestVo.getRepairType());
			repairLog.setRepairStatus("WOSta_Sub");
			repairLog.setCreateTime(new Date());
			repairLog.setUpdateTime(new Date());
			repairLog.setDeleteFlag(0);
			repairLogDaoMapper.save(repairLog);
			// 发送文件
			try {
				// 本地文件地址
				String filePath = saveImg(fileList,no);
				// 读取文件
				byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
				// 创建文件消息对象
				FileMessage fileMessage = new FileMessage(folderPathRemote, no+".txt", fileBytes);
				FileSendClient.sendFile(fileMessage);
			} catch (Exception e) {
				logger.info("Error in Send: " + e.getMessage());
			}
		}else {
			repairResponseVo.setRespCode(Constant.FAIL_CODE);
			repairResponseVo.setErrDesc(msg);
		}

		return repairResponseVo;
	}

	public String saveImg(String[] fileList, String no) {
		String path = "";
		// 将图片数组转换为逗号分割的字符串
		if (fileList.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (String str : fileList) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(str);
			}
			//目录不存在则直接创建
			File filePath = new File(uploadPath + "/repair");
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			//创建年月日文件夹
			File ymdFile = new File(uploadPath + "/repair/" + new SimpleDateFormat("yyyyMMdd").format(new Date()));
			//目录不存在则直接创建
			if (!ymdFile.exists()) {
				ymdFile.mkdirs();
			}
			//在年月日文件夹下面创建txt文本存储图片base64码
			File txtFile = new File(ymdFile.getPath() + "/" + no + ".txt");
			if (!txtFile.exists()) {
				try {
					txtFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			path = txtFile.getPath();
			FileWriter writer = null;
			try {
				writer = new FileWriter(txtFile);
				writer.write(sb.toString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	// 组装报修参数
	public String initTicket(String orgId, String orgName, String no, String cstId, String cstName,
							 String woNoBasicId, String woNoBasicName, String quesTypeId, String quesTypeName, String houseId,
							 String workPos, String desc, String descDi, String phone, String[] pics){
		String OrgID = "{ \"OrgID\":\"" + orgId + "\",";
		String OrgName = "\"OrgName\":\"" + orgName + "\",";
		String WONo = "\"WONo\":\"" + no + "\",";
		String RSDate = "\"RSDate\":\"" + DateUtils.strYmdHms() + "\",";
		// WONoBasicID,WONoBasicName  报事主规则（一般报事、投诉等）
		String WONoBasicID = "\"WONoBasicID\": \"" + woNoBasicId + "\",";
		String WONoBasicName = "\"WONoBasicName\":\"" + woNoBasicName + "\",";
		//String CstID = "\"CstID\":\"\",";
		String CstID = "\"CstID\":\"" + cstId + "\",";
		String CstName = "\"CstName\":\"" + cstName + "\",";
		String WorkPosFrom = "\"WorkPosFrom\":\"Resource\",";
		String WOID = "\"WOID\":\"" + houseId + "\",";
		String WorkPos = "\"WorkPos\":\"" + workPos + "\",";
		String RSWay = "\"RSWay\": \"wx\",";
		String CallPhone = "\"CallPhone\":\"" + phone + "\",";
		String RStartTime = "\"RStartTime\":\"" + DateUtils.strYmdHms() + "\",";
		String Importance = "\"Importance\":\"1\",";
		String Urgency = "\"Urgency\":\"1\",";
		String Intricacy = "\"Intricacy\":\"1\",";
		String PaidServices = "\"PaidServices\":\"1\",";
		String Orders = "\"Orders\":\"\",";
		String OrdersID = "\"OrdersID\":\"\",";
		String OrdersDepart = "\"OrdersDepart\":\"\","; ;
		String OrdersPositionID = "\"OrdersPositionID\":\"\","; ;
		String WorkQuestion = "\"WorkQuestion\":[{";
		String QuesTypeID = "\"QuesTypeID\":\"" + quesTypeId + "\",";
		String QuesTypeName = "\"QuesTypeName\":\"" + quesTypeName + "\",";
		String QuesDesc = "\"QuesDesc\":\"" + desc + "\",";
		String QuesDeti = "\"QuesDeti\":\"" + descDi + "\",";
		String CreateTime = "\"CreateTime\":\"\",";
		String CreateUser = "\"CreateUser\":\"\",";
		String OpTime = "\"OpTime\":\"\",";
		String OpUser = "\"OpUser\":\"\",";

		for(int i = 0; i<pics.length; i++ ){
			if (i == 0) {
				CreateTime = "\"CreateTime\":\"" + pics[i] + "\",";
			}
			if (i == 1) {
				CreateUser = "\"CreateUser\":\"" + pics[i] + "\",";
			}
			if (i == 2) {
				OpTime = "\"OpTime\":\"" + pics[i] + "\",";
			}
			if (i == 3) {
				OpUser = "\"OpUser\":\"" + pics[i] + "\",";
			}
		}

		OpUser += "}]}";

		return OrgID + OrgName + WONo + RSDate + WONoBasicID + WONoBasicName +
				CstID + CstName + WorkPosFrom + WOID + WorkPos + RSWay + CallPhone + RStartTime +
				Importance + Urgency + Intricacy + PaidServices + Orders + OrdersID + OrdersDepart +
				OrdersPositionID + WorkQuestion + QuesTypeID + QuesTypeName + QuesDesc + QuesDeti +
				CreateTime + CreateUser + OpTime + OpUser;
	}

	public String initTicket2(String orgId, String orgName,  String cstCode, String cstName,
							 String woNoBasicId, String houseId, String workPos, String desc,
							  String phone, String wxOpenId, String budId, String[] pics){
		String img = "";
		for(int i = 0; i<pics.length; i++ ){
			img += "{\"Image\":\"" + pics[i] + "\"},";
		}
		String UserID = "{ \"UserID\":\"\",";
		String FeedID = "\"FeedID\":\"\",";
		String OrgID = "\"OrgID\":\"" + orgId + "\",";
		String OrgName = "\"OrgName\":\"" + orgName + "\",";
		// WONoBasicID,WONoBasicName  报事主规则（一般报事、投诉等）
		String WONoBasicID = "\"WONoBasicID\": \"" + woNoBasicId + "\",";
		// String WONoBasicName = "\"WONoBasicName\":\"" + woNoBasicName + "\",";
		// String CstID = "\"CstID\":\"\",";
		String CstID = "\"CstID\":\"" + cstCode + "\",";
		String CstName = "\"CstName\":\"" + cstName + "\",";
		// String WorkPosFrom = "\"WorkPosFrom\":\"Resource\",";
		String ResID = "\"ResID\":\"" + houseId + "\",";
		String RSWay = "\"RSWay\": \"wx\",";
		String WorkPos = "\"WorkPos\":\"" + workPos + "\",";
		String RSPeo = "\"RSPeo\":\"" + cstName + "\",";
		String CallPhone = "\"CallPhone\":\"" + phone + "\",";
		String Elements = "\"Elements\":\"" + desc + "\",";
		String Importance = "\"Importance\":\"1\",";
		String Urgency = "\"Urgency\":\"1\",";
		String Intricacy = "\"Intricacy\":\"1\",";
		String PaidServices = "\"PaidServices\":\"1\",";
		String PathWay = "\"PathWay\": \"OClient\",";
		String PostItRating = "\"PostItRating\": \"G\",";
		String OpenID = "\"OpenID\":\"" + wxOpenId + "\",";
		String Brand = "\"Brand\":\"\",";
		String Amount = "\"Amount\":\"\",";
		String LicensePlate = "\"LicensePlate\":\"\",";
		String BudID = "\"BudID\":\"" + budId + "\",";
		String Images = "\"Images\": [" + img + "]}";
		return UserID + FeedID + OrgID + OrgName + WONoBasicID  +
				CstID + CstName + ResID +  RSWay + WorkPos + RSPeo + CallPhone + Elements +
				Importance + Urgency + Intricacy + PaidServices + PathWay + PostItRating + OpenID +
				Brand + Amount + LicensePlate + BudID + Images;
	}

	@RequestMapping("/repairHouseList")
	@ResponseBody
	public AjaxResult queryRepairList(@RequestBody RepairRequestVo repairRequestVo){
		AjaxResult ajaxResult = new AjaxResult();
		// 0-租户员工、租客、亲属  1-租户、产权人
		//Integer ownerFlag = 0;
		HashMap map = new HashMap();
		String cstCode = repairRequestVo.getCstCode();
		String wxOpenId = repairRequestVo.getWxOpenId();
		// 房屋列表
		List<HgjHouse> list = new ArrayList<>();
		// 获取登录人身份
		CstInto cstInto = cstIntoMapper.getByWxOpenIdAndStatus_1(wxOpenId);
		// 0-租户 2-产权人
		if(cstInto.getIntoRole() == 0 || cstInto.getIntoRole() == 2){
			HgjHouse hgjHouse = new HgjHouse();
			hgjHouse.setCstCode(cstCode);
			list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
		}
		// 1-员工 3-租客 4-亲属
		if(cstInto.getIntoRole() == 1 || cstInto.getIntoRole() == 3 || cstInto.getIntoRole() == 4){
			List<String> houseIdList = new ArrayList<>();
			// 查询租户员工、租客、亲属已入住房间
			List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(cstCode, wxOpenId);
			for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
				houseIdList.add(cstIntoHouse.getHouseId());
			}
			HgjHouse hgjHouse = new HgjHouse();
			hgjHouse.setCstCode(cstCode);
			hgjHouse.setHouseIdList(houseIdList);
			list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
		}

//		// 获取当前客户房间列表
//		CstInto cstInto = new CstInto();
//		cstInto.setCstCode(cstCode);
//		cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
//		List<CstInto> cstIntos = cstIntoMapper.getList(cstInto);
//		// 判断登录人是否是租户、产权人,条件cstCode,wxOpenId,intoRole=租户、产权人
//		List<CstInto> cstIntoFilter = cstIntos.stream().filter(into -> (into.getIntoRole() == Constant.INTO_ROLE_CST || into.getIntoRole() == Constant.INTO_ROLE_PROPERTY_OWNER) && into.getWxOpenId().equals(wxOpenId)).collect(Collectors.toList());
//		// 如果不是租户、产权人才会查询租户员工、租客、同住人的房屋
//		if(cstIntoFilter.isEmpty()){
//			// 查询租户员工、租客、同住人已入住房间
//			List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(cstCode, wxOpenId);
//			for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
//				houseIdList.add(cstIntoHouse.getHouseId());
//			}
//		}
//		else {
//			ownerFlag = 1;
//		}
//		HgjHouse hgjHouse = new HgjHouse();
//		hgjHouse.setCstCode(cstCode);
//		hgjHouse.setHouseIdList(houseIdList);
//		//List<HgjHouse> list = syHouseDaoMapper.getListByCstCode(hgjHouse);
//		List<HgjHouse> list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
//		if(!list.isEmpty()){
//			// 获取房屋业主
//			List<CstInto> ownerList = cstIntoMapper.getByCstCodeAndIntoRole(cstCode);
//			// 查询每个房屋的租户
//			for(HgjHouse house : list){
//				// 租客、同住人集合
//				List<CstInto> cstIntoList = cstIntoMapper.getListByHouseId(house.getId());
//				// 房主集合
//				cstIntoList.addAll(ownerList);
//				// 排序
//				cstIntoList = cstIntoList.stream().sorted(Comparator.comparing(CstInto::getIntoRole)).collect(Collectors.toList());
//				// 过滤掉未入住的
//				//cstIntoList = cstIntoList.stream().filter(c -> c.getIntoStatus() == Constant.INTO_STATUS_Y && c.getHouseIntoStatus() == Constant.INTO_STATUS_Y).collect(Collectors.toList());
//
//				house.setCstIntoList(cstIntoList);
//			}
//		}
		map.put("list", list);
		//map.put("ownerFlag", ownerFlag);
		ajaxResult.setRespCode(Constant.SUCCESS);
		ajaxResult.setMessage(Constant.SUCCESS_RESULT_MESSAGE);
		ajaxResult.setData(map);
		return ajaxResult;
	}

//	@RequestMapping("/repairHouseList")
//	@ResponseBody
//    public AjaxResult queryRepairList(@RequestBody RepairRequestVo repairRequestVo){
//        AjaxResult ajaxResult = new AjaxResult();
//        // 业主标识 1-业主 0-租户
//        Integer ownerFlag = 0;
//		HashMap map = new HashMap();
//		List<String> houseIdList = new ArrayList<>();
//		String cstCode = repairRequestVo.getCstCode();
//		String wxOpenId = repairRequestVo.getWxOpenId();
//		CstInto cstInto = new CstInto();
//		cstInto.setCstCode(cstCode);
//		List<CstInto> cstIntos = cstIntoMapper.getList(cstInto);
//		// 判断登录人是否是业主,条件cstCode,wxOpenId,intoRole=房主
//		List<CstInto> cstIntoFilter = cstIntos.stream().filter(into -> into.getIntoRole() == Constant.INTO_ROLE_OWNER && into.getWxOpenId().equals(wxOpenId)).collect(Collectors.toList());
//		// 如果不是房主才会查询租户的房屋
//		if(cstIntoFilter.isEmpty()){
//			if(!cstIntos.isEmpty()){
//				for (CstInto cst : cstIntos){
//					if(StringUtils.isNotBlank(cst.getHouseId()) && cst.getIntoRole() == Constant.INTO_ROLE_TENANT && cst.getIntoStatus() == Constant.INTO_STATUS_Y){
//						houseIdList.add(cst.getHouseId());
//					}
//				}
//			}
//		}else {
//			ownerFlag = 1;
//		}
//		HgjHouse hgjHouse = new HgjHouse();
//		hgjHouse.setCstCode(cstCode);
//		hgjHouse.setHouseIdList(houseIdList);
//		// List<HgjHouse> list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
//		List<HgjHouse> list = syHouseDaoMapper.getListByCstCode(hgjHouse);
//		if(!list.isEmpty()){
//			// 获取房屋业主
//			CstInto cs = new CstInto();
//			cs.setCstCode(cstCode);
//			cs.setIntoRole(Constant.INTO_ROLE_OWNER);
//			List<CstInto> ownerList = cstIntoMapper.getList(cs);
//			//ownerList = ownerList.stream().filter(owner -> !owner.getWxOpenId().equals(wxOpenId)).collect(Collectors.toList());
//			// 查询每个房屋的租户
//			for(HgjHouse house : list){
//				CstInto cst = new CstInto();
//				cst.setHouseId(house.getId());
//				// 租户集合
//				List<CstInto> cstIntoList = cstIntoMapper.getList(cst);
//				// 房主集合
//				cstIntoList.addAll(ownerList);
//				// 排序
//				cstIntoList = cstIntoList.stream().sorted(Comparator.comparing(CstInto::getIntoRole)).collect(Collectors.toList());
//				// 如果是租户登录，过滤掉未入住的租户
//				if(ownerFlag == 0){
//					cstIntoList = cstIntoList.stream().filter(c -> c.getIntoStatus() == Constant.INTO_STATUS_Y).collect(Collectors.toList());
//				}
//				house.setCstIntoList(cstIntoList);
//			}
//		}
//		map.put("list", list);
//		map.put("ownerFlag", ownerFlag);
//		ajaxResult.setRespCode(Constant.SUCCESS);
//		ajaxResult.setMessage(Constant.SUCCESS_RESULT_MESSAGE);
//		ajaxResult.setData(map);
//		return ajaxResult;
//    }

	@ResponseBody
	@RequestMapping("/queryRepairLog.do")
	public RepairResponseVo queryRepairLog(@RequestBody RepairRequestVo repairRequestVo) {
		RepairResponseVo repairResponseVo = new RepairResponseVo();
		RepairLog repairLog = new RepairLog();
		String repairNum = repairRequestVo.getRepairNum();
		repairLog.setRepairStatus(repairRequestVo.getRepairStatus());
		repairLog.setProjectNum(repairRequestVo.getProNum());
		repairLog.setCstCode(repairRequestVo.getCstCode());
		repairLog.setWxOpenId(repairRequestVo.getWxOpenId());
		repairLog.setRepairNum(repairNum);
		List<String> houseIdList = new ArrayList<>();
		// 查询委托人、住户已入住房间
		List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(repairRequestVo.getCstCode(), repairRequestVo.getWxOpenId());
		for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
			houseIdList.add(cstIntoHouse.getHouseId());
		}
		repairLog.setHouseIdList(houseIdList);
		PageHelper.offsetPage((repairRequestVo.getPageNum()-1) * repairRequestVo.getPageSize(),repairRequestVo.getPageSize());
		List<RepairLog> list = repairLogDaoMapper.getList(repairLog);
		PageInfo<RepairLog> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)repairRequestVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		repairResponseVo.setPages(pageNumTotal);
		repairResponseVo.setTotalNum((int) pageInfo.getTotal());
		repairResponseVo.setPageSize(repairRequestVo.getPageSize());
		if(list != null){

//			if(repairNum != null) {
//				// 获取图片
//				String imgPath = list.get(0).getImage();
//				String base64Img = "";
//				try {
//					// 创建BufferedReader对象，从本地文件中读取
//					BufferedReader reader = new BufferedReader(new FileReader(imgPath));
//					// 逐行读取文件内容
//					String line = "";
//					while ((line = reader.readLine()) != null) {
//						base64Img += line;
//					}
//					// 关闭文件
//					reader.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				String[] fileList = base64Img.split(",");
//				//logger.info("报修图片:" + base64Img);
//				repairResponseVo.setFileList(fileList);
//			}

			if(StringUtils.isNotBlank(repairNum)) {
				// 获取文件路径
				String imgPath = list.get(0).getImage();
				// 拼接远程文件地址
				String fileUrl = Constant.REMOTE_FILE_URL + "/" + imgPath;
				String fileContent = FileSendClient.downloadFileContent(fileUrl);
				if(StringUtils.isNotBlank(fileContent)) {
					String[] fileList = fileContent.split(",");
					repairResponseVo.setFileList(fileList);
				}
			}

			// 获取思源报修单状态
			List<String> woNoList = new ArrayList<>();
			for(RepairLog r : list){
				woNoList.add(r.getRepairNum());
			}

//			if(!woNoList.isEmpty()) {
//				List<WorkOrd> syWorkOrdList = workOrdDaoMapper.getList(woNoList);
//				logger.info("syWorkOrdList返回记录数");
//				logger.info(syWorkOrdList != null ? syWorkOrdList.size() + "":0 + "");
//				for (RepairLog rl : list) {
//					List<WorkOrd> syWorkOrdListFilter = syWorkOrdList.stream().filter(syOl -> rl.getRepairNum().equals(syOl.getWoNo())).collect(Collectors.toList());
//					if (syWorkOrdListFilter != null) {
//						rl.setRepairStatus(syWorkOrdListFilter.get(0).getWorkOrdState());
//					}
//				}
//			}
		}

		// 根据状态查询的时候过滤list
//		List<RepairLog> listFilter = new ArrayList<>();
//		if("0".equals(repairStatus) || repairStatus == null){
//			// 全部
//			listFilter = list;
//		}
//		if("1".equals(repairStatus)){
//			// 已提交
//			listFilter = list.stream().filter(l -> "WOSta_Sub".equals(l.getRepairStatus())).collect(Collectors.toList());
//		}
//		if("2".equals(repairStatus)){
//			// 处理中
//			listFilter = list.stream().filter(l -> "WOSta_Proc".equals(l.getRepairStatus())).collect(Collectors.toList());
//		}
//		if("3".equals(repairStatus)){
//			// 已完工 包括 WOSta_Visit-已回访,WOSta_Close-已关闭
//			listFilter = list.stream().filter(l -> "WOSta_Finish".equals(l.getRepairStatus())
//					|| "WOSta_Visit".equals(l.getRepairStatus()) || "WOSta_Close".equals(l.getRepairStatus())
//			).collect(Collectors.toList());
//		}
//		repairResponseVo.setRepairLogList(listFilter);
		repairResponseVo.setRepairLogList(list);
		repairResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		repairResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		repairResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return repairResponseVo;
	}

	@ResponseBody
	@RequestMapping("/repair/addRepairMsg.do")
	public RepairResponseVo addRepairMsg(@RequestBody RepairRequestVo repairRequestVo) {
		RepairResponseVo repairResponseVo = new RepairResponseVo();
		// 根据单号查询报修单ID
		WorkOrd workOrd = workOrdDaoMapper.getCsWorkOrd(repairRequestVo.getRepairNum(),"WOSta_Finish");
		if(workOrd == null){
			workOrd = workOrdDaoMapper.getCsWorkOrd(repairRequestVo.getRepairNum(),"WOSta_Visit");
		}
		// 根据报修单id查询思源已回访记录
		List<ReturnVisit> list = new ArrayList<>();
		if(workOrd != null){
			list = returnVisitDaoMapper.getList(workOrd.getId());
		}
		if(!list.isEmpty()){
			repairResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
			repairResponseVo.setErrCode(JiasvBasicRespCode.RESULT_FAILED.getRespCode());
			repairResponseVo.setErrDesc("客服已回访!");
		}else {
			String repairScore = repairRequestVo.getRepairScore();
			String totalScore = "";
			if ("1".equals(repairScore)) totalScore = "20";
			if ("2".equals(repairScore)) totalScore = "40";
			if ("3".equals(repairScore)) totalScore = "60";
			if ("4".equals(repairScore)) totalScore = "80";
			if ("5".equals(repairScore)) totalScore = "100";
			// 满意度
			String hgjSatisFaction = repairRequestVo.getSatisFaction();
			String sySatisFaction = "";
			if ("0".equals(hgjSatisFaction)) {
				sySatisFaction = "100";
			} else {
				sySatisFaction = "50";
			}
			// 获取思源接口地址
			ConstantConfig constantConfig = constantConfDaoMapper.getByKey("sy_url");
			// 获取token
			String token = SyPostClient.getToken(constantConfig.getConfigValue());

			//workOrd.setId("2307201424330001006P");
			String p7 = initReturnVisit(workOrd.getId(), sySatisFaction, totalScore, repairRequestVo.getRepairMsg());

			// 获取请求结果, 调用思源接口 94-客服回访接口，修改工单状态为已回访
			JSONObject jsonObject = SyPostClient.userRev2ServiceCustomerServiceReturn("UserRev2_Service_CustomerServiceReturn", p7, token, constantConfig.getConfigValue());
			String status = jsonObject.getString("Status");
			String msg = jsonObject.getString("Msg");
			if ("1".equals(status)) {
				RepairLog repairLog = new RepairLog();
				repairLog.setRepairNum(repairRequestVo.getRepairNum());
				repairLog.setRepairScore(repairScore);
				repairLog.setRepairMsg(repairRequestVo.getRepairMsg());
				repairLog.setSatisFaction(hgjSatisFaction);
				repairLog.setRepairStatus("WOSta_Visit");
				repairLogDaoMapper.update(repairLog);
				repairResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
				repairResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
				repairResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
			} else {
				repairResponseVo.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
				repairResponseVo.setErrCode(JiasvBasicRespCode.RESULT_FAILED.getRespCode());
				repairResponseVo.setErrDesc(JiasvBasicRespCode.RESULT_FAILED.getRespDesc());
			}
		}
		return repairResponseVo;
	}


	@ResponseBody
	@RequestMapping("/queryRepairCostDetail.do")
	public RepairResponseVo queryRepairCostDetail(@RequestBody RepairRequestVo repairRequestVo) {
		RepairResponseVo repairResponseVo = new RepairResponseVo();
		String repairNum = repairRequestVo.getRepairNum();
		// 根据单号,已完工状态查询报修单ID
		WorkOrd workOrd = workOrdDaoMapper.getCsWorkOrd(repairNum,"WOSta_Visit");
		// 获取材料明细
		List<Material> list = new ArrayList<>();
		if(workOrd != null){
			list = materialDaoMapper.getList(workOrd.getId());
		}
		// 获取人工费、材料费
		RepairLog repairLog = repairLogDaoMapper.getByRepNum(repairNum);
		if(repairLog != null){
			repairResponseVo.setLabourCost(repairLog.getLabourCost());
			repairResponseVo.setMaterialCost(repairLog.getMaterialCost());
		}
		repairResponseVo.setMaterialList(list);
		repairResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		repairResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		repairResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return repairResponseVo;
	}


	// 组装参数-客服回访
	public String initReturnVisit(String repairId, String satisFaction, String totalScore, String desc){
		String WOID = "{ \"WOID\":\"" + repairId + "\",";
		String ReturnVisitWay = "\"ReturnVisitWay\":\"Tel\",";
		String ReturnVisitDate = "\"ReturnVisitDate\":\"" + DateUtils.strYmdHms() + "\",";
		String Object = "\"Object\":\"17082215304300020066\",";
		String ObjectName = "\"ObjectName\":\"KF01\",";
		String SatisfiedVisit = "\"SatisfiedVisit\":\"" + satisFaction + "\",";
		//String FailureCause = "\"FailureCause\":\"很满意\",";
		String Remark = "\"Remark\":\"" + desc + "\",";
		String TotalScore = "\"TotalScore\":\"" + totalScore + "\",";
		String VisitState = "\"VisitState\":\"1\",";
		String UserId = "\"UserId\":\"Sam\",";
		UserId += "}";
		return WOID + ReturnVisitWay + ReturnVisitDate + Object + ObjectName + SatisfiedVisit + Remark + TotalScore + VisitState + UserId;
	}


	public static void main(String[] args) {
		// 思源接口-正式环境
		// String url = "http://192.168.5.201:4321/NetApp/CstService.asmx/GetService";
		// 思源接口-测试环境
		String url = "http://192.168.99.1:4321/NetApp/CstService.asmx/GetService";
		// 获取token
		String token = SyPostClient.getToken(url);
		RepairController repairController = new RepairController();
		// KHBX20230801005 - 100    2308011415310001006P   	Satisfaction2 -满意
		// KHBX20230801004 - 90 	2308011207060001006P   	Satisfaction2 -满意
		// KHBX20230801003 - 80 	2308011058100001006P	Satisfaction2 -满意
		// GGBX20230731028 - 70		2307311604210001009U	Satisfaction2 -满意
		// KHBX20230728007 - 60		2307281713330001006P	Satisfaction2 -满意
		// GGBX20230728008 - 50		2307281336160001006P	NotSatisfaction1 - 不满意
		// KHBX20230728002 - 40		2307281135250001006P	NotSatisfaction1 - 不满意
		// GGBX20230726012 - 30		2307261340180001006P	NotSatisfaction1 - 不满意
		// GGBX20230724178b - 150	2023072412122028d418
		// KHBX20230724002
		// CS_ReturnVisit
		String p7 = repairController.initReturnVisit("2307241147450001006P","100" , "100", "111");
		// 获取请求结果, 调用思源接口 94-客服回访接口，修改工单状态为已回访
		JSONObject jsonObject = SyPostClient.userRev2ServiceCustomerServiceReturn("UserRev2_Service_CustomerServiceReturn", p7, token, url);
		String status = jsonObject.getString("Status");
		String msg = jsonObject.getString("Msg");
		System.out.println("status:"+status+"----msg:"+msg);
	}
}

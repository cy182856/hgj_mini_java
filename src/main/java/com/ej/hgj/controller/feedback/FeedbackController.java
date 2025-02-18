package com.ej.hgj.controller.feedback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.AjaxResult;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.config.RepairConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.feedback.FeedbackDaoMapper;
import com.ej.hgj.dao.hu.CstIntoHouseDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.repair.RepairLogDaoMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.config.RepairConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.feedback.FeedBack;
import com.ej.hgj.entity.file.FileMessage;
import com.ej.hgj.entity.file.Request;
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
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.SyPostClient;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.utils.file.FileSendClient;
import com.ej.hgj.vo.ResponseVo;
import com.ej.hgj.vo.feedback.FeedbackRequestVo;
import com.ej.hgj.vo.repair.RepairRequestVo;
import com.ej.hgj.vo.repair.RepairResponseVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.ws.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 问题反馈
 */
@Controller
public class FeedbackController extends BaseController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${upload.path}")
	private String uploadPath;

	@Value("${upload.path.remote}")
	private String uploadPathRemote;

	@Autowired
	private FeedbackDaoMapper feedbackDaoMapper;

	@Autowired
	private HgjCstDaoMapper hgjCstDaoMapper;

	@Autowired
	private ProConfDaoMapper proConfDaoMapper;


	@ResponseBody
	@RequestMapping("/feedback.do")
	public ResponseVo repair(@RequestBody FeedbackRequestVo feedbackRequestVo) {
		ResponseVo responseVo = new ResponseVo();
		// 客户编号
		String cstCode = feedbackRequestVo.getCstCode();
		// 客户微信openId
		String wxOpenId = feedbackRequestVo.getWxOpenId();
		// 项目号
		String proNum = feedbackRequestVo.getProNum();
		// 客户手机号
		String cstPhone = feedbackRequestVo.getCstPhone();
		// 问题反馈描述
		String feedbackDesc = feedbackRequestVo.getFeedbackDesc();
		// 图片
		String[] fileList = feedbackRequestVo.getFileList();

		if(StringUtils.isBlank(cstCode) || StringUtils.isBlank(wxOpenId) || StringUtils.isBlank(proNum)){
			responseVo.setRespCode(MonsterBasicRespCode.REQ_DATA_NULL.getReturnCode());
			responseVo.setErrCode(JiasvBasicRespCode.DATA_NULL.getRespCode());
			responseVo.setErrDesc(JiasvBasicRespCode.DATA_NULL.getRespDesc());
			return responseVo;
		}

		// 客户信息
		HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
		// 项目名
		ProConfig proConfig = proConfDaoMapper.getByProjectNum(proNum);
		String orgName = proConfig.getProjectName();
		FeedBack feedBack = new FeedBack();
		String id = TimestampGenerator.generateSerialNumber();
		feedBack.setId(id);
		feedBack.setProNum(proNum);
		feedBack.setProName(orgName);
		feedBack.setWxOpenId(wxOpenId);
		feedBack.setCstCode(cstCode);
		feedBack.setCstName(hgjCst.getCstName());
		feedBack.setCstPhone(cstPhone);
		feedBack.setFeedbackDesc(feedbackDesc);
		feedBack.setCreateTime(new Date());
		feedBack.setUpdateTime(new Date());
		feedBack.setDeleteFlag(0);
		// 远程文件夹地址
		String folderPathRemote = uploadPathRemote+"/feedback/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
		// 远程文件地址
		String filePathRemote = folderPathRemote + "/" + id+".txt";
		feedBack.setImage(filePathRemote);
		feedbackDaoMapper.save(feedBack);
		// 成功
		responseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		// 失败
		//responseVo.setRespCode(Constant.FAIL_CODE);
		//responseVo.setErrDesc("失败描述");

		// 发送文件
		try {
			// 本地文件地址
			String filePath = saveImg(fileList,id);
			// 读取文件
			byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
			// 创建文件消息对象
			FileMessage fileMessage = new FileMessage(folderPathRemote, id+".txt", fileBytes);
			FileSendClient.sendFile(fileMessage);
		} catch (Exception e) {
			logger.info("Error in Send: " + e.getMessage());
		}
		return responseVo;
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
			File filePath = new File(uploadPath + "/feedback");
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			//创建年月日文件夹
			File ymdFile = new File(uploadPath + "/feedback/" + new SimpleDateFormat("yyyyMMdd").format(new Date()));
			//目录不存在则直接创建
			if (!ymdFile.exists()) {
				ymdFile.mkdirs();
			}
			//在年月日文件夹下面创建txt文本存储图片base64码
			File txtFile = new File(ymdFile.getPath() + "/" + no + ".txt");
			//File txtFile = new File(uploadPath + "/feedback" + no + ".txt");
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

	@ResponseBody
	@RequestMapping("/feedbackQuery.do")
	public ResponseVo feedbackQuery(@RequestBody FeedbackRequestVo feedbackRequestVo) {
		ResponseVo responseVo = new ResponseVo();
		FeedBack feedBack = new FeedBack();
		String id = feedbackRequestVo.getId();
		feedBack.setId(id);
		feedBack.setWxOpenId(feedbackRequestVo.getWxOpenId());
		PageHelper.offsetPage((feedbackRequestVo.getPageNum()-1) * feedbackRequestVo.getPageSize(),feedbackRequestVo.getPageSize());
		List<FeedBack> list = feedbackDaoMapper.getList(feedBack);
		PageInfo<FeedBack> pageInfo = new PageInfo<>(list);
		int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)feedbackRequestVo.getPageSize());
		list = pageInfo.getList();
		logger.info("list返回记录数");
		logger.info(list != null ? list.size() + "":0 + "");
		responseVo.setPages(pageNumTotal);
		responseVo.setTotalNum((int) pageInfo.getTotal());
		responseVo.setPageSize(feedbackRequestVo.getPageSize());
		if(list != null){
			if(StringUtils.isNotBlank(id)) {
				// 获取文件路径
				String imgPath = list.get(0).getImage();
				// 拼接远程文件地址
				String fileUrl = Constant.REMOTE_FILE_URL + "/" + imgPath;
				String fileContent = FileSendClient.downloadFileContent(fileUrl);
				if(StringUtils.isNotBlank(fileContent)) {
					String[] fileList = fileContent.split(",");
					responseVo.setFileList(fileList);
				}
			}
		}
		responseVo.setFeedbackList(list);
		responseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		responseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		responseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return responseVo;
	}

}

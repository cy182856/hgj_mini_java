package com.ej.hgj.controller.hu;

import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.config.WorkTimeConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.hu.HuHgjBindMapper;
import com.ej.hgj.dao.user.UserDaoMapper;
import com.ej.hgj.dao.user.UserDutyPhoneDaoMapper;
import com.ej.hgj.dao.user.UsrConfMapper;
import com.ej.hgj.dao.wechat.WechatPubConfMapper;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.config.WorkTimeConfig;
import com.ej.hgj.entity.cst.CstPayPer;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.user.User;
import com.ej.hgj.entity.user.UserDutyPhone;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.vo.bill.BillResponseVo;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.ej.hgj.vo.user.UserInfoVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class HouseKeepController extends BaseController {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserDaoMapper userDaoMapper;

	@Autowired
	private WorkTimeConfDaoMapper workTimeConfDaoMapper;

	@Autowired
	private UserDutyPhoneDaoMapper userDutyPhoneDaoMapper;

	/**
	 * 获取管家电话，企业微信二维码
	 */
	@ResponseBody
	@RequestMapping("/houseKeepInfo.do")
	public UserInfoVo houseKeepInfo(@RequestBody HouseInfoVO houseInfoVO) {
		UserInfoVo userInfoVo = new UserInfoVo();
		String cstCode = houseInfoVO.getCstCode();
		// 查询工作时间
		WorkTimeConfig workTime = workTimeConfDaoMapper.getWorkTime();
		// 查询所有值班电话
		List<UserDutyPhone> userDutyPhoneList = userDutyPhoneDaoMapper.getList(new UserDutyPhone());
		// 在工作时间内
		Boolean isWorkTime = true;
		try {
			Date startTime = DateUtils.sdfHms.parse(workTime.getStartTime());
			Date endTime = DateUtils.sdfHms.parse(workTime.getEndTime());
			Date tempCurrentTime = new Date();
			String currentTimeStr = DateUtils.sdfHms.format(tempCurrentTime);
			Date currentTime = DateUtils.sdfHms.parse(currentTimeStr);
			if(!(currentTime.after(startTime) && currentTime.before(endTime))) {
				// 不在工作时间内
				isWorkTime = false;
			}
		} catch (ParseException e) {
			e.getMessage();
		}
		List<User> list = userDaoMapper.getByCstCode(cstCode);
		for(User user : list){
			if(isWorkTime == true){
				user.setIsWorkTime(1);
			}else {
				user.setIsWorkTime(0);
				List<UserDutyPhone> userDutyPhoneListFilter = userDutyPhoneList.stream().filter(dutyPhone -> dutyPhone.getMobile().equals(user.getUserId())).collect(Collectors.toList());
				if(!userDutyPhoneListFilter.isEmpty()){
					user.setMobile(userDutyPhoneListFilter.get(0).getPhone());
				}else {
					user.setMobile(null);
				}
			}
			// 企微二维码转换
			if(StringUtils.isNotBlank(user.getQrCode())){
				String base64Image = "";
				try {
					BufferedImage image = ImageIO.read(new File(user.getQrCode()));
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "png", baos);
					byte[] imageBytes = baos.toByteArray();
					base64Image = Base64.getEncoder().encodeToString(imageBytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
				user.setQrCode(base64Image);
			}
		}
		userInfoVo.setUserList(list);
		userInfoVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		userInfoVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		userInfoVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return userInfoVo;
	}

}

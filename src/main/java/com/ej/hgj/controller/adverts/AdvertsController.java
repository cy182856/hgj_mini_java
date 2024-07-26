package com.ej.hgj.controller.adverts;

import com.ej.hgj.base.BaseReqVo;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.adverts.AdvertsDaoMapper;
import com.ej.hgj.dao.config.WorkTimeConfDaoMapper;
import com.ej.hgj.dao.user.UserDaoMapper;
import com.ej.hgj.dao.user.UserDutyPhoneDaoMapper;
import com.ej.hgj.entity.adverts.Adverts;
import com.ej.hgj.entity.config.WorkTimeConfig;
import com.ej.hgj.entity.user.User;
import com.ej.hgj.entity.user.UserDutyPhone;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.vo.adverts.AdvertsVo;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.ej.hgj.vo.user.UserInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class AdvertsController extends BaseController {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private AdvertsDaoMapper advertsDaoMapper;

	/**
	 * 获取广告
	 */
	@ResponseBody
	@RequestMapping("/queryAdverts.do")
	public AdvertsVo queryAdverts(@RequestBody BaseReqVo baseReqVo) {
		AdvertsVo advertsVo = new AdvertsVo();
		String proNum = baseReqVo.getProNum();
		Adverts adverts = new Adverts();
		adverts.setIsShow(0);
		adverts.setProNum(proNum);
		List<Adverts> list = advertsDaoMapper.getList(adverts);
		Adverts advertsData = new Adverts();
		if(!list.isEmpty()){
			advertsData = list.get(0);
			if(StringUtils.isNotBlank(advertsData.getImgPath())){
				String base64Image = "";
				try {
					BufferedImage image = ImageIO.read(new File(advertsData.getImgPath()));
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "png", baos);
					byte[] imageBytes = baos.toByteArray();
					base64Image = Base64.getEncoder().encodeToString(imageBytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
				advertsData.setImgPath(base64Image);
			}
		}

		advertsVo.setAdverts(advertsData);
		advertsVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
		advertsVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
		advertsVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
		return advertsVo;
	}

}

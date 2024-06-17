package com.ej.hgj.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.config.RepairConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.repair.RepairLog;
import com.ej.hgj.entity.workord.WorkPos;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.utils.DateUtils;
import com.ej.hgj.utils.SyPostClient;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.repair.RepairRequestVo;
import com.ej.hgj.vo.repair.RepairResponseVo;
import com.ej.hgj.vo.user.UserInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController extends BaseController {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @ResponseBody
    @RequestMapping("/queryPersonData")
    public UserInfoVo queryMutip(@RequestBody UserInfoVo userInfoVo) {
        logger.info("-----------------获取个人资料--------------------");
        UserInfoVo userVoRes = new UserInfoVo();
        String cstCode = userInfoVo.getCstCode();
        String wxOpenId = userInfoVo.getWxOpenId();
        String proNum = userInfoVo.getProNum();
        CstInto cstInto = cstIntoMapper.queryPersonData(wxOpenId,cstCode,proNum);
        userVoRes.setUserName(cstInto.getUserName());
        userVoRes.setPhone(cstInto.getPhone());
        userVoRes.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        return userVoRes;
    }

    @ResponseBody
    @RequestMapping("/updatePersonData")
    public UserInfoVo repair(@RequestBody UserInfoVo userInfoVo) {
        UserInfoVo userVoRes = new UserInfoVo();
        String cstCode = userInfoVo.getCstCode();
        String wxOpenId = userInfoVo.getWxOpenId();
        String proNum = userInfoVo.getProNum();
        String userName = userInfoVo.getUserName();
        String phone = userInfoVo.getPhone();
        CstInto cstInto = new CstInto();
        cstInto.setCstCode(cstCode);
        cstInto.setProjectNum(proNum);
        cstInto.setWxOpenId(wxOpenId);
        cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
        List<CstInto> list = cstIntoMapper.getList(cstInto);
        if(!list.isEmpty()){
            CstInto cInto = list.get(0);
            CstInto pram = new CstInto();
            pram.setId(cInto.getId());
            pram.setUserName(userName);
            pram.setPhone(phone);
            pram.setUpdateTime(new Date());
            cstIntoMapper.update(pram);
            userVoRes.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
            userVoRes.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
            userVoRes.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
        }else {
            userVoRes.setRespCode(Constant.FAIL_CODE);
            userVoRes.setErrDesc("未查到用户信息!");
        }
        return userVoRes;
    }
}

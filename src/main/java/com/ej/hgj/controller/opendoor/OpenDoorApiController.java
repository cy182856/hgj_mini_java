/**
 *
 * 上海云之富金融信息服务有限公司
 * Copyright (c) 2014-2020 YunCF,Inc.All Rights Reserved.
 */
package com.ej.hgj.controller.opendoor;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.opendoor.OpenDoorCodeDaoMapper;
import com.ej.hgj.dao.opendoor.OpenDoorLogDaoMapper;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.opendoor.OpenDoorCode;
import com.ej.hgj.entity.opendoor.OpenDoorLog;
import com.ej.hgj.entity.visit.VisitLog;
import com.ej.hgj.service.wechat.WechatService;
import com.ej.hgj.utils.GenerateUniqueIdUtil;
import com.ej.hgj.utils.RandomNumberGenerator;
import com.ej.hgj.utils.SecurityUtil;
import com.ej.hgj.utils.bill.TimestampGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/pass/code")
public class OpenDoorApiController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OpenDoorCodeDaoMapper openDoorCodeDaoMapper;

    @Autowired
    private OpenDoorLogDaoMapper openDoorLogDaoMapper;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject query(String code) {
        JSONObject jsonObject = new JSONObject();
        if(StringUtils.isBlank(code)){
            jsonObject.put("code", "999");
            jsonObject.put("desc", "请求参数错误");
            return jsonObject;
        }
        OpenDoorCode openDoorCode = openDoorCodeDaoMapper.getByRandNum(code);
        jsonObject.put("code", "000");
        jsonObject.put("desc", "成功");
        jsonObject.put("data", openDoorCode);
        return jsonObject;
    }


    @RequestMapping(value = "/callBack", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject callBack(String code, String cardNum, String phone) {
        JSONObject jsonObject = new JSONObject();
        if(StringUtils.isBlank(code)){
            jsonObject.put("code", "999");
            jsonObject.put("desc", "code参数不存在");
            return jsonObject;
        }
        if(StringUtils.isBlank(cardNum)){
            jsonObject.put("code", "999");
            jsonObject.put("desc", "cardNum参数不存在");
            return jsonObject;
        }
        if(StringUtils.isBlank(phone)){
            jsonObject.put("code", "999");
            jsonObject.put("desc", "phone参数不存在");
            return jsonObject;
        }
        OpenDoorLog openDoorLog = new OpenDoorLog();
        openDoorLog.setId(GenerateUniqueIdUtil.getGuid());
        openDoorLog.setType(2);
        openDoorLog.setCardNum(cardNum);
        openDoorLog.setRandNum(code);
        openDoorLog.setPhone(phone);
        openDoorLog.setCreateTime(new Date());
        openDoorLog.setUpdateTime(new Date());
        openDoorLog.setDeleteFlag(0);
        openDoorLogDaoMapper.save(openDoorLog);

        // 更新快速码转态为失效
        openDoorCodeDaoMapper.updateByRandNum(code);
        jsonObject.put("code", "000");
        jsonObject.put("desc", "成功");
        return jsonObject;
    }
}

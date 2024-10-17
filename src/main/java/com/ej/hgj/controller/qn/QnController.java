/**
 *
 * 上海云之富金融信息服务有限公司
 * Copyright (c) 2014-2020 YunCF,Inc.All Rights Reserved.
 */
package com.ej.hgj.controller.qn;

import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.qn.QnDaoMapper;
import com.ej.hgj.dao.tag.TagCstDaoMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.qn.FormData;
import com.ej.hgj.entity.qn.Qn;
import com.ej.hgj.entity.tag.TagCst;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.vo.qn.QnVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@CrossOrigin
//@RestController
//@RequestMapping("/form")
@Controller
public class QnController extends BaseController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private QnDaoMapper qnDaoMapper;

    @Autowired
    private TagCstDaoMapper tagCstDaoMapper;

    @RequestMapping("/qn/query.do")
    @ResponseBody
    public QnVo queryVisitInfos(@RequestBody QnVo qnVo) {
        String wxOpenId = qnVo.getWxOpenId();
        PageHelper.offsetPage((qnVo.getPageNum()-1) * qnVo.getPageSize(),qnVo.getPageSize());
        Qn qn = new Qn();
        qn.setProNum(qnVo.getProNum());
        qn.setMiniIsShow(1);
        qn.setWxOpenId(wxOpenId);
        List<Qn> list = qnDaoMapper.getList(qn);
        PageInfo<Qn> pageInfo = new PageInfo<>(list);
        int pageNumTotal = (int) Math.ceil((double)pageInfo.getTotal()/(double)qnVo.getPageSize());
        list = pageInfo.getList();
        logger.info("list返回记录数");
        logger.info(list != null ? list.size() + "":0 + "");
        qnVo.setPages(pageNumTotal);
        qnVo.setTotalNum((int) pageInfo.getTotal());
        qnVo.setPageSize(qnVo.getPageSize());
        qnVo.setList(list);
        qnVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        return qnVo;
    }

    @ResponseBody
    @RequestMapping("/queryQnGatewayUrl.do")
    public BaseRespVo queryQnGatewayUrl(@RequestBody BaseRespVo baseRespVo) {
        BaseRespVo responseVo = new BaseRespVo();
        String proNum = baseRespVo.getProNum();
        ConstantConfig config = constantConfDaoMapper.getByProNumAndKey(proNum, Constant.QN_GATEWAY_URL);
        Map map = new HashMap();
        map.put("gatewayUrl",config.getConfigValue());
        responseVo.setData(map);
        responseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        responseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
        responseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
        return responseVo;
    }

    @RequestMapping(value="/callback", method =RequestMethod.POST)
    public void receive(@RequestBody FormData data, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("========接收表单数据:"+data+"=============");
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/json");
        response.setCharacterEncoding("UTF-8");

        try {

        }catch (Exception e) {
            logger.info(e.getMessage(), "处理表单数据失败");
        }
        // 响应消息
        PrintWriter out = response.getWriter();
        out.print("200");
        out.close();
    }
}

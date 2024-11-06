package com.ej.hgj.controller.tenant;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseReqVo;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.AjaxResult;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoHouseDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.hu.HuHgjBindMapper;
import com.ej.hgj.dao.user.UsrConfMapper;
import com.ej.hgj.dao.wechat.WechatPubConfMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoHouse;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.hu.MutipUserVo;
import com.ej.hgj.entity.login.MiniProSession;
import com.ej.hgj.entity.login.MutipUsrVo;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.request.hu.HuCheckInRequest;
import com.ej.hgj.service.hu.HuService;
import com.ej.hgj.sy.dao.house.SyHouseDaoMapper;
import com.ej.hgj.utils.TokenUtils;
import com.ej.hgj.utils.WechatMiniProUtils;
import com.ej.hgj.vo.bill.BillResponseVo;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.ej.hgj.vo.repair.RepairRequestVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/tenant")
public class TenantController extends BaseController {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @Autowired
    private HgjHouseDaoMapper hgjHouseDaoMapper;

    @Autowired
    private CstIntoHouseDaoMapper cstIntoHouseDaoMapper;

    @Autowired
    private HuService huService;

    /**
     * 租客管理查询
     * @param baseReqVo
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public AjaxResult queryRepairList(@RequestBody BaseReqVo baseReqVo){
        AjaxResult ajaxResult = new AjaxResult();
        // 0-租户员工、租客、同住人  1-租户、产权人
        Integer ownerFlag = 0;
        HashMap map = new HashMap();
        List<String> houseIdList = new ArrayList<>();
        String cstCode = baseReqVo.getCstCode();
        String wxOpenId = baseReqVo.getWxOpenId();
        // 获取当前客户房间列表
        CstInto cstInto = new CstInto();
        cstInto.setCstCode(cstCode);
        cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
        List<CstInto> cstIntos = cstIntoMapper.getList(cstInto);
        // 判断登录人是否是租户、产权人,条件cstCode,wxOpenId,intoRole=租户、产权人
        List<CstInto> cstIntoFilter = cstIntos.stream().filter(into -> (into.getIntoRole() == Constant.INTO_ROLE_CST || into.getIntoRole() == Constant.INTO_ROLE_PROPERTY_OWNER) && into.getWxOpenId().equals(wxOpenId)).collect(Collectors.toList());
        // 如果不是租户、产权人才会查询租户员工、租客、同住人的房屋
        if(cstIntoFilter.isEmpty()){
            // 查询租户员工、租客、同住人已入住房间
            List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(cstCode, wxOpenId);
            for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
                houseIdList.add(cstIntoHouse.getHouseId());
            }
        }else {
            ownerFlag = 1;
        }
        HgjHouse hgjHouse = new HgjHouse();
        hgjHouse.setCstCode(cstCode);
        hgjHouse.setHouseIdList(houseIdList);
        //List<HgjHouse> list = syHouseDaoMapper.getListByCstCode(hgjHouse);
        List<HgjHouse> list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
        if(!list.isEmpty()){
            // 获取房屋业主
            List<CstInto> ownerList = cstIntoMapper.getByCstCodeAndIntoRole(cstCode);
            // 查询每个房屋的租户
            for(HgjHouse house : list){
                // 租客、同住人集合
                List<CstInto> cstIntoList = cstIntoMapper.getListByHouseId(house.getId());
                // 房主集合
                cstIntoList.addAll(ownerList);
                // 排序
                cstIntoList = cstIntoList.stream().sorted(Comparator.comparing(CstInto::getIntoRole)).collect(Collectors.toList());
                // 过滤掉未入住的
                //cstIntoList = cstIntoList.stream().filter(c -> c.getIntoStatus() == Constant.INTO_STATUS_Y && c.getHouseIntoStatus() == Constant.INTO_STATUS_Y).collect(Collectors.toList());

                house.setCstIntoList(cstIntoList);
            }
        }
        map.put("list", list);
        map.put("ownerFlag", ownerFlag);
        ajaxResult.setRespCode(Constant.SUCCESS);
        ajaxResult.setMessage(Constant.SUCCESS_RESULT_MESSAGE);
        ajaxResult.setData(map);
        return ajaxResult;
    }


    /**
     * 同意，拒绝，移除
     */
    @ResponseBody
    @RequestMapping("/operate")
    public BillResponseVo cohabitOperate(@RequestBody HouseInfoVO houseInfoVO) {
        BillResponseVo billResponseVo = new BillResponseVo();
        huService.updateStatus(houseInfoVO);
        billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
        billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
        return billResponseVo;
    }

}

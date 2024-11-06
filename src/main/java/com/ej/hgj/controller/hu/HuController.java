package com.ej.hgj.controller.hu;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.AjaxResult;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.*;
import com.ej.hgj.dao.user.UsrConfMapper;
import com.ej.hgj.dao.wechat.WechatPubConfMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.*;
import com.ej.hgj.entity.login.MiniProSession;
import com.ej.hgj.entity.login.MutipUsrVo;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.request.hu.HuCheckInRequest;
import com.ej.hgj.service.hu.HuService;
import com.ej.hgj.sy.dao.house.SyHouseDaoMapper;
import com.ej.hgj.utils.HttpClientUtil;
import com.ej.hgj.utils.TokenUtils;
import com.ej.hgj.utils.WechatMiniProUtils;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.utils.exception.BusinessException;
import com.ej.hgj.vo.bill.BillResponseVo;
import com.ej.hgj.vo.hu.CardPermVo;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.ej.hgj.vo.repair.RepairRequestVo;
import org.apache.commons.httpclient.HttpURL;
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
public class HuController extends BaseController {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HuHgjBindMapper huHgjBindMapper;

    @Autowired
    private WechatPubConfMapper wechatPubConfMapper;

    @Autowired
    private UsrConfMapper usrConfMapper;

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @Autowired
    private HgjCstDaoMapper hgjCstDaoMapper;

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Autowired
    private ProConfDaoMapper proConfDaoMapper;

    @Autowired
    private HgjHouseDaoMapper hgjHouseDaoMapper;

    @Autowired
    private CstIntoHouseDaoMapper cstIntoHouseDaoMapper;

    @Autowired
    private SyHouseDaoMapper syHouseDaoMapper;

    @Autowired
    private HuService huService;

    @Autowired
    private CstIntoCardMapper cstIntoCardMapper;

    @ResponseBody
    @RequestMapping({"/queryMutipUsr","/queryMutipUsr.do"})
    public BaseRespVo queryMutip(@RequestBody MutipUsrVo mutipUsrVo) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        logger.info("-----------------获取用户信息--------------------");
        MutipUserVo mutipUserVoResponse = new MutipUserVo();
        String cstCode = mutipUsrVo.getCstCode();
        String code = mutipUsrVo.getCode();
        String isCHeck = "N";
        String wxOpenId = mutipUsrVo.getWxOpenId();
        String userName = "";
        if (isBank(wxOpenId) && isBank(code)) {
            logger.info("用户信息缺失");
        }
        if (isNotBank(code) && isBank(wxOpenId)) {
            ConstantConfig constantConfig = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
            MiniProSession miniProSession = WechatMiniProUtils.jscode2session(constantConfig.getAppId(), constantConfig.getAppSecret(), code);
            wxOpenId = miniProSession.getOpenid();
            CstInto cstInto = cstIntoMapper.getByWxOpenIdAndStatus_1(wxOpenId);
            if(cstInto == null && StringUtils.isBlank(cstCode)){
                mutipUserVoResponse.setErrCode("01012012");
                mutipUserVoResponse.setErrDesc("账户未开通");
                return mutipUserVoResponse;
            }
            // 更新用户unionId
            String unionId =  miniProSession.getUnionid();
            if(cstInto != null && StringUtils.isBlank(cstInto.getUnionId())){
                // 更新用户unionId
                cstInto.setUpdateTime(new Date());
                cstInto.setUnionId(unionId);
                cstIntoMapper.update(cstInto);
            }
            // 1.客户编号为空或者客户编号不为空但是与入住的客户编号不一致时，都取入住的客户编号
            if(StringUtils.isBlank(cstCode) ||
                    (StringUtils.isNotBlank(cstCode) && cstInto != null
                            && !cstCode.equals(cstInto.getCstCode()))){
                cstCode = cstInto.getCstCode();
            }
            logger.info("利用换取wxOpenId,code:" + code + "||wxOpenId:" + wxOpenId);
        }
        if(StringUtils.isBlank(wxOpenId)){
            mutipUserVoResponse.setRespCode(MonsterBasicRespCode.WX_OPENID_FAILED.getReturnCode());
            mutipUserVoResponse.setErrDesc(MonsterBasicRespCode.WX_OPENID_FAILED.getCodeDesc());
            return mutipUserVoResponse;
        }

        // 获取项目号、客户名称
        HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
        String proNum = hgjCst.getOrgId();
        String cstName = hgjCst.getCstName();

        // 获取项目名
        ProConfig proConfig = proConfDaoMapper.getByProjectNum(proNum);
        String proName = proConfig.getProjectName();

        // 检查是否入住
        CstInto cstInto = new CstInto();
        cstInto.setCstCode(cstCode);
        cstInto.setWxOpenId(wxOpenId);
        cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
        List<CstInto> cstIntoList = cstIntoMapper.getList(cstInto);
        if(!cstIntoList.isEmpty()){
            isCHeck = "Y";
            userName = cstIntoList.get(0).getUserName();
            //houseId = cstIntoList.get(0).getHouseId();
        }
        // 当入住角色是 1-租户员工,3-租客,4-同住人 时查询入住房屋列表
        String cstIntoId = mutipUsrVo.getCstIntoId();
        if(StringUtils.isNotBlank(cstIntoId)){
            CstInto cstIntoInfo = cstIntoMapper.getById(cstIntoId);
            List<String> houseList = new ArrayList<>();
            if(cstIntoInfo != null){
                if(cstIntoInfo.getIntoRole() == Constant.INTO_ROLE_ENTRUST || cstIntoInfo.getIntoRole() == Constant.INTO_ROLE_HOUSEHOLD || cstIntoInfo.getIntoRole() == Constant.INTO_ROLE_COHABIT){
                    List<HgjHouse> hgjHouseList = hgjHouseDaoMapper.getByCstIntoId(cstIntoId);
                    if(!hgjHouseList.isEmpty()){
                        for(HgjHouse hgjHouse : hgjHouseList){
                            houseList.add(hgjHouse.getResName());
                        }
                    }
                    // 当入住角色是 0-租户,2-产权人时查询入住房屋列表
                }else {
                    HgjHouse hgjHouse = new HgjHouse();
                    hgjHouse.setCstCode(cstCode);
                    List<HgjHouse> list = syHouseDaoMapper.getListByCstCode(hgjHouse);
                    if(!list.isEmpty()){
                        for(HgjHouse house : list){
                            houseList.add(house.getResName());
                        }
                    }
                }
                mutipUserVoResponse.setHouseList(houseList);
            }
        }
        // 获取房屋名称列表
//            if(StringUtils.isNotBlank(houseId)){
//               HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
//                houseName = hgjHouse.getResName();
//            }
        // 获取项目名称
        mutipUserVoResponse.setCstCode(cstCode);
        mutipUserVoResponse.setCstName(cstName);
        mutipUserVoResponse.setWxOpenId(wxOpenId);
        mutipUserVoResponse.setProNum(proNum);
        mutipUserVoResponse.setProName(proName);
        mutipUserVoResponse.setIsCHeck(isCHeck);
        mutipUserVoResponse.setUserName(userName);

        // mutipUserVoResponse.setHouseId(houseId);
        // mutipUserVoResponse.setHouseName(houseName);
        String token = TokenUtils.getToken(wxOpenId, wxOpenId);
        mutipUserVoResponse.setToken(token);
        mutipUserVoResponse.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        return mutipUserVoResponse;

    }

    /**
     * 客户入住
     * @param huCheckInRequest
     * @return
     */
    @RequestMapping("/hu/houseBind")
    @ResponseBody
    public JSONObject houseBind(@RequestBody HuCheckInRequest huCheckInRequest) {
        JSONObject jsonObject = new JSONObject();
        if(StringUtils.isBlank(huCheckInRequest.getCstCode())){
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "客户编号为空!");
            return jsonObject;
        }
        if(StringUtils.isBlank(huCheckInRequest.getWxOpenId())){
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "微信号为空!");
            return jsonObject;
        }
        if(StringUtils.isBlank(huCheckInRequest.getUserName())){
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "用户名为空!");
            return jsonObject;
        }
        if(StringUtils.isBlank(huCheckInRequest.getPhone())){
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "手机号为空!");
            return jsonObject;
        }
        CstInto cstInto = cstIntoMapper.getById(huCheckInRequest.getCstIntoId());
        if(cstInto == null){
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "入住信息不存在!");
            return jsonObject;
        }
        return huService.updateIntoStatus(jsonObject, huCheckInRequest, cstInto);
    }


    /**
     * 同意，拒绝，移除
     */
    @ResponseBody
    @RequestMapping("hu/operate")
    public BillResponseVo operate(@RequestBody HouseInfoVO houseInfoVO) {
        BillResponseVo billResponseVo = new BillResponseVo();
        huService.updateStatus(houseInfoVO);
        billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
        billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
        return billResponseVo;
    }

    /**
     * 卡权限设置
     */
    @ResponseBody
    @RequestMapping("hu/cardPerm")
    public AjaxResult cardPerm(@RequestBody CardPermVo cardPermVo) {
       AjaxResult ajaxResult = new AjaxResult();
       String proNum = cardPermVo.getProNum();
       String wxOpenId = cardPermVo.getWxOpenId();
       String tenantWxOpenId = cardPermVo.getTenantWxOpenId();
       String cstCode = cardPermVo.getCstCode();
       // 删除卡权限
       cstIntoCardMapper.deleteCardPerm(proNum,cstCode,tenantWxOpenId);
       // 新增卡权限
       Integer[] cardIds = cardPermVo.getCardIds();
       if(cardIds != null && cardIds.length > 0){
           for(int i = 0; i<cardIds.length; i++){
               CstIntoCard cstIntoCard = new CstIntoCard();
               cstIntoCard.setId(TimestampGenerator.generateSerialNumber());
               cstIntoCard.setProNum(proNum);
               cstIntoCard.setCardId(cardIds[i]);
               cstIntoCard.setWxOpenId(tenantWxOpenId);
               cstIntoCard.setCstCode(cstCode);
               cstIntoCard.setCreateBy(wxOpenId);
               cstIntoCard.setUpdateBy(wxOpenId);
               cstIntoCard.setCreateTime(new Date());
               cstIntoCard.setUpdateTime(new Date());
               cstIntoCard.setDeleteFlag(Constant.DELETE_FLAG_NOT);
               cstIntoCardMapper.save(cstIntoCard);
           }
       }
        ajaxResult.setRespCode(Constant.SUCCESS);
        ajaxResult.setMessage(Constant.SUCCESS_RESULT_MESSAGE);
        return ajaxResult;
    }

    /**
     * 我的房屋列表
     */
    @RequestMapping("hu/list")
    @ResponseBody
    public AjaxResult queryRepairList(@RequestBody RepairRequestVo repairRequestVo){
        AjaxResult ajaxResult = new AjaxResult();
        Integer ownerFlag = 0;
        HashMap map = new HashMap();

        String cstCode = repairRequestVo.getCstCode();
        String wxOpenId = repairRequestVo.getWxOpenId();
        // 获取登录人身份
        CstInto cstInto = cstIntoMapper.getByWxOpenIdAndStatus_1(wxOpenId);
        // 0-租户 2-产权人
        if(cstInto.getIntoRole() == 0 || cstInto.getIntoRole() == 2){
            ownerFlag = 1;
        }
        // 房屋列表
        List<HgjHouse> list = new ArrayList<>();
        // 租户、产权人房屋查询
        if(ownerFlag == 1){
            HgjHouse hgjHouse = new HgjHouse();
            hgjHouse.setCstCode(cstCode);
            list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
        }
        // 员工、租客、亲属
        if(ownerFlag == 0){
            // 查询租户员工、租客、亲属已入住房间
            List<String> houseIdList = new ArrayList<>();
            List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstCodeAndWxOpenId(cstCode, wxOpenId);
            for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
                houseIdList.add(cstIntoHouse.getHouseId());
            }
            HgjHouse hgjHouse = new HgjHouse();
            hgjHouse.setCstCode(cstCode);
            hgjHouse.setHouseIdList(houseIdList);
            list = hgjHouseDaoMapper.getListByCstCode(hgjHouse);
        }

        if(!list.isEmpty()){
            // 根据客户编号查询卡权限
            CstIntoCard cstIntoCard = new CstIntoCard();
            cstIntoCard.setCstCode(cstCode);
            List<CstIntoCard> cstIntoCardList = cstIntoCardMapper.getList(cstIntoCard);
            // 根据客户编号查询房屋业主、租户
            List<CstInto> ownerList = cstIntoMapper.getByCstCodeAndIntoRole(cstCode);
            for(HgjHouse house : list){
                // 员工、租客、亲属集合
                List<CstInto> cstIntoList = cstIntoMapper.getListByHouseId(house.getId());
                // 查询卡权限
                for(CstInto cst : cstIntoList){
                    // 查询是否有游泳卡
                    List<CstIntoCard> cstIntoCardListFilterSwim = cstIntoCardList.stream().filter(cstIntoCard1 -> cst.getWxOpenId().equals(cstIntoCard1.getWxOpenId()) && cstIntoCard1.getCardId() == 1).collect(Collectors.toList());
                    if(!cstIntoCardListFilterSwim.isEmpty()){
                        cst.setSwimCardChecked(true);
                    }else {
                        cst.setSwimCardChecked(false);
                    }
                    // 查询是否有停车卡
                    List<CstIntoCard> cstIntoCardListFilterPark = cstIntoCardList.stream().filter(cstIntoCard1 -> cst.getWxOpenId().equals(cstIntoCard1.getWxOpenId()) && cstIntoCard1.getCardId() == 2).collect(Collectors.toList());
                    if(!cstIntoCardListFilterPark.isEmpty()){
                        cst.setParkCardChecked(true);
                    }else {
                        cst.setParkCardChecked(false);
                    }

                }
                // 每个房屋添加 业主、租户 信息
                cstIntoList.addAll(ownerList);
                // 排序
                cstIntoList = cstIntoList.stream().sorted(Comparator.comparing(CstInto::getIntoRole)).collect(Collectors.toList());
                house.setCstIntoList(cstIntoList);
            }
        }
        map.put("list", list);
        map.put("ownerFlag", ownerFlag);
        map.put("proNum",cstInto.getProjectNum());
        ajaxResult.setRespCode(Constant.SUCCESS);
        ajaxResult.setMessage(Constant.SUCCESS_RESULT_MESSAGE);
        ajaxResult.setData(map);
        return ajaxResult;
    }

    //    /**
//     * 房主对同住人操作，同意，拒绝，移除
//     */
//    @ResponseBody
//    @RequestMapping("/cohabitOperate.do")
//    public BillResponseVo cohabitOperate(@RequestBody HouseInfoVO houseInfoVO) {
//        BillResponseVo billResponseVo = new BillResponseVo();
//        String id = houseInfoVO.getId();
//        // 同意
//        if("agree".equals(houseInfoVO.getButtonType())){
//            CstInto cstInto = new CstInto();
//            cstInto.setId(id);
//            cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
//            cstInto.setUpdateTime(new Date());
//            cstIntoMapper.update(cstInto);
//            // 拒绝，移除
//        }else if("refuse".equals(houseInfoVO.getButtonType()) || "remove".equals(houseInfoVO.getButtonType())){
//            CstInto cstInto = new CstInto();
//            cstInto.setId(id);
//            cstInto.setDeleteFlag(Constant.DELETE_FLAG_YES);
//            cstInto.setUpdateTime(new Date());
//            cstIntoMapper.update(cstInto);
//        }
//        billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
//        billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
//        billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
//        return billResponseVo;
//    }

//    public void updateIntoStatus(HuCheckInRequest huCheckInRequest){
//        // 更新用户入住状态为待审核
//        CstInto cInto = new CstInto();
//        cInto.setId(huCheckInRequest.getCstIntoId());
//        cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//        cInto.setUserName(huCheckInRequest.getUserName());
//        cInto.setIntoStatus(Constant.INTO_STATUS_A);
//        cInto.setUpdateTime(new Date());
//        cstIntoMapper.update(cInto);
//        // 同时更新入住房间表
//        CstIntoHouse cstIntoHouse = new CstIntoHouse();
//        cstIntoHouse.setCstIntoId(huCheckInRequest.getCstIntoId());
//        cstIntoHouse.setIntoStatus(Constant.INTO_STATUS_A);
//        cstIntoHouse.setUpdateTime(new Date());
//        cstIntoHouseDaoMapper.updateByCstIntoId(cstIntoHouse);
//    }

    /**
     * 房主入住
     * @param huCheckInRequest
     * @return
     */
//    @RequestMapping("/hu/houseBind/owner")
//    @ResponseBody
//    public JSONObject houseBindOwner(@RequestBody HuCheckInRequest huCheckInRequest) {
//        JSONObject jsonObject = new JSONObject();
//        if(StringUtils.isBlank(huCheckInRequest.getCstCode())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "客户编号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getWxOpenId())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "微信号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getUserName())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "用户名为空!");
//            return jsonObject;
//        }
//        // 根据微信号查询入住信息
//        List<CstInto> cstIntoList = cstIntoMapper.getListByWxOpenId(huCheckInRequest.getWxOpenId());
//        // 入住条件 1.微信号未绑定过房屋
//        if(cstIntoList.isEmpty()){
//            CstInto cInto = new CstInto();
//            cInto.setId(TimestampGenerator.generateSerialNumber());
//            cInto.setCstCode(huCheckInRequest.getCstCode());
//            cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//            cInto.setUserName(huCheckInRequest.getUserName());
//            cInto.setIntoRole(Constant.INTO_ROLE_CST);
//            cInto.setIntoStatus(Constant.INTO_STATUS_Y);
//            cInto.setCreateTime(new Date());
//            cInto.setUpdateTime(new Date());
//            cInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
//            cstIntoMapper.save(cInto);
//            jsonObject.put("respCode", Constant.SUCCESS);
//            logger.info("---------------"+huCheckInRequest.getCstCode()+"房主入住成功-----------------");
//        }else {
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "请勿重复绑定!");
//        }
//        return jsonObject;
//    }

    /**
     * 租户入住
     * @return
     */
//    @RequestMapping("/hu/houseBind/tenant")
//    @ResponseBody
//    public JSONObject houseBindTenant(@RequestBody HuCheckInRequest huCheckInRequest) {
//        JSONObject jsonObject = new JSONObject();
//        if(StringUtils.isBlank(huCheckInRequest.getCstCode())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "客户编号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getWxOpenId())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "微信号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getUserName())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "用户名为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getHouseId())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "房屋号为空!");
//            return jsonObject;
//        }
//        // 根据微信号查询入住信息
//        List<CstInto> intoList = cstIntoMapper.getListByWxOpenId(huCheckInRequest.getWxOpenId());
//        if(intoList.isEmpty()){
//            // 直接入住
//            saveTenantInto(huCheckInRequest);
//            jsonObject.put("respCode", Constant.SUCCESS);
//        }else {
//            // 查询微信号是否绑定过其他客户的房屋
//            List<CstInto> cstIntoFilter_1 = intoList.stream().filter(cstInto_1 -> !cstInto_1.getCstCode().equals(huCheckInRequest.getCstCode())).collect(Collectors.toList());
//            // 查询微信号是否绑定过该客户的房屋
//            CstInto cstInto = new CstInto();
//            cstInto.setHouseId(huCheckInRequest.getHouseId());
//            cstInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//            List<CstInto> cstIntoList_2 = cstIntoMapper.getList(cstInto);
//            // 查询微信号是否是该客户的业主
//            List<CstInto> cstIntoFilter_3 = intoList.stream().filter(cstInto_2 -> cstInto_2.getCstCode().equals(huCheckInRequest.getCstCode()) && cstInto_2.getIntoRole() == Constant.INTO_ROLE_CST ).collect(Collectors.toList());
//            // 入住条件 1.微信号未绑定过其他客户的房屋 2.微信号未绑定过该客户的房屋 3.微信号不是这个客户的业主
//            if(cstIntoFilter_1.isEmpty() && cstIntoList_2.isEmpty() && cstIntoFilter_3.isEmpty()){
//                saveTenantInto(huCheckInRequest);
//                jsonObject.put("respCode", Constant.SUCCESS);
//            }else {
//                jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//                jsonObject.put("errDesc", "请勿重复绑定!");
//            }
//        }
//        return jsonObject;
//    }

//    public void saveTenantInto(HuCheckInRequest huCheckInRequest){
//        CstInto cInto = new CstInto();
//        cInto.setId(TimestampGenerator.generateSerialNumber());
//        cInto.setCstCode(huCheckInRequest.getCstCode());
//        cInto.setHouseId(huCheckInRequest.getHouseId());
//        cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//        cInto.setUserName(huCheckInRequest.getUserName());
//        cInto.setIntoRole(Constant.INTO_ROLE_ENTRUST);
//        cInto.setIntoStatus(Constant.INTO_STATUS_N);
//        cInto.setCreateTime(new Date());
//        cInto.setUpdateTime(new Date());
//        cInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
//        cstIntoMapper.save(cInto);
//        logger.info("---------------"+huCheckInRequest.getCstCode()+"租户入住申请成功-----------------");
//    }

//    @ResponseBody
//    @RequestMapping({"/queryMutipUsr","/queryMutipUsr.do"})
//    public BaseRespVo queryMutip(@RequestBody MutipUsrVo mutipUsrVo) {
//        logger.info("-----------------获取用户信息--------------------");
//        MutipUserVo mutipUserVo = new MutipUserVo();
//         String cstCode = mutipUsrVo.getCstCode();
//        String code = mutipUsrVo.getCode();
//        String isCHeck = "N";
//        String wxOpenId = mutipUsrVo.getWxOpenId();
//        String userName = "";
//        String houseId = mutipUsrVo.getHouseId();
//        String houseName = "";
//        if (isBank(wxOpenId) && isBank(code)) {
//            logger.info("用户信息缺失");
//        }
//        try {
//            if (isNotBank(code) && isBank(wxOpenId)) {
//                ConstantConfig constantConfig = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
//                wxOpenId = WechatMiniProUtils.jscode2session(constantConfig.getAppId(), constantConfig.getAppSecret(), code).getOpenid();
//                List<CstInto> cstIntoList = cstIntoMapper.getListByWxOpenId(wxOpenId);
//                if(cstIntoList.isEmpty() && StringUtils.isBlank(cstCode)){
//                    mutipUserVo.setErrCode("01012012");
//                    mutipUserVo.setErrDesc("账户未开通");
//                    return mutipUserVo;
//                }
//
//                // 1.客户编号为空或者客户编号不为空但是与入住的客户编号不一致时，都取入住的客户编号
//                if(StringUtils.isBlank(cstCode) ||
//                        (StringUtils.isNotBlank(cstCode) && !cstIntoList.isEmpty()
//                                && !cstCode.equals(cstIntoList.get(0).getCstCode()))){
//                    cstCode = cstIntoList.get(0).getCstCode();
//                }
//                logger.info("利用换取wxOpenId,code:" + code + "||wxOpenId:" + wxOpenId);
//            }
//            if(StringUtils.isBlank(wxOpenId)){
//                mutipUserVo.setRespCode(MonsterBasicRespCode.WX_OPENID_FAILED.getReturnCode());
//                mutipUserVo.setErrDesc(MonsterBasicRespCode.WX_OPENID_FAILED.getCodeDesc());
//                return mutipUserVo;
//            }
//
//            // 获取项目号、客户名称
//            HgjCst hgjCst = hgjCstDaoMapper.getByCstCode(cstCode);
//            String proNum = hgjCst.getOrgId();
//            String cstName = hgjCst.getCstName();
//
//            // 获取项目名
//            ProConfig proConfig = proConfDaoMapper.getByProjectNum(proNum);
//            String proName = proConfig.getProjectName();
//
//            // 检查是否入住
//            CstInto cstInto = new CstInto();
//            cstInto.setCstCode(cstCode);
//            cstInto.setWxOpenId(wxOpenId);
//            cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
//            if(StringUtils.isNotBlank(houseId)){
//                cstInto.setHouseId(houseId);
//            }
//            List<CstInto> cstIntoList = cstIntoMapper.getList(cstInto);
//            if(!cstIntoList.isEmpty()){
//                isCHeck = "Y";
//                userName = cstIntoList.get(0).getUserName();
//                houseId = cstIntoList.get(0).getHouseId();
//            }
//
//            // 获取房屋名称
//            if(StringUtils.isNotBlank(houseId)){
//                HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
//                houseName = hgjHouse.getResName();
//            }
//            // 获取项目名称
//            mutipUserVo.setCstCode(cstCode);
//            mutipUserVo.setCstName(cstName);
//            mutipUserVo.setWxOpenId(wxOpenId);
//            mutipUserVo.setProNum(proNum);
//            mutipUserVo.setProName(proName);
//            mutipUserVo.setIsCHeck(isCHeck);
//            mutipUserVo.setUserName(userName);
//            mutipUserVo.setHouseId(houseId);
//            mutipUserVo.setHouseName(houseName);
//            mutipUserVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
//            return mutipUserVo;
//        } catch (BusinessException e) {
//            logger.info("获取用户信息失败");
//            e.getMessage();
//        } catch (Exception e) {
//            logger.info("获取用户信息系统异常");
//            e.getMessage();
//        }
//        return null;
//    }

    //    @RequestMapping("/hu/houseBind")
//    @ResponseBody
//    public JSONObject userSelfClaim(@RequestBody HuCheckInRequest huCheckInRequest) {
//        JSONObject jsonObject = new JSONObject();
//        if(StringUtils.isBlank(huCheckInRequest.getCstCode())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "客户编号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getWxOpenId())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "微信号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getUserName())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "用户名为空!");
//            return jsonObject;
//        }
//        List<CstInto> cstIntoList = cstIntoMapper.getByCstCode(huCheckInRequest.getCstCode());
//        CstInto cstInto = cstIntoMapper.getByWxOpenId(huCheckInRequest.getWxOpenId());
//
//        // 客户第一次入住默认设置为房主
//        if(cstIntoList.isEmpty()){
//            CstInto cInto = new CstInto();
//            cInto.setId(TimestampGenerator.generateSerialNumber());
//            cInto.setCstCode(huCheckInRequest.getCstCode());
//            cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//            cInto.setUserName(huCheckInRequest.getUserName());
//            cInto.setIntoRole(Constant.INTO_ROLE_OWNER);
//            cInto.setIntoStatus(Constant.INTO_STATUS_Y);
//            cInto.setCreateTime(new Date());
//            cInto.setUpdateTime(new Date());
//            cInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
//            cstIntoMapper.save(cInto);
//            jsonObject.put("respCode", Constant.SUCCESS);
//            logger.info("---------------"+huCheckInRequest.getCstCode()+"房主入住成功-----------------");
//        }else if(cstInto == null){
//            // 同住人入住
//            CstInto cInto = new CstInto();
//            cInto.setId(TimestampGenerator.generateSerialNumber());
//            cInto.setCstCode(huCheckInRequest.getCstCode());
//            cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//            cInto.setUserName(huCheckInRequest.getUserName());
//            cInto.setIntoRole(Constant.INTO_ROLE_COHABIT);
//            cInto.setIntoStatus(Constant.INTO_STATUS_N);
//            cInto.setCreateTime(new Date());
//            cInto.setUpdateTime(new Date());
//            cInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
//            cstIntoMapper.save(cInto);
//            jsonObject.put("respCode", "001");
//            jsonObject.put("errDesc", "入住申请成功!");
//            logger.info("---------------"+huCheckInRequest.getCstCode()+"同住人入住申请成功-----------------");
//        }else {
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "请勿重复绑定!");
//        }
//        return jsonObject;
//    }

    //    @RequestMapping("/hu/houseBind")
//    @ResponseBody
//    public JSONObject houseBind(@RequestBody HuCheckInRequest huCheckInRequest) {
//        JSONObject jsonObject = new JSONObject();
//        String cstIntoId = huCheckInRequest.getCstIntoId();
//        if(StringUtils.isBlank(huCheckInRequest.getCstCode())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "客户编号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getWxOpenId())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "微信号为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(huCheckInRequest.getUserName())){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "用户名为空!");
//            return jsonObject;
//        }
//        if(StringUtils.isBlank(cstIntoId)){
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "入住编号为空!");
//            return jsonObject;
//        }
//        CstInto cstInto = cstIntoMapper.getById(cstIntoId);
//        // 根据微信号与入住状态 1-已入住(客户、产权人) 3-待审核(委托人、住户)查出一条入住信息
//        CstInto cstIntoByWxOpenId = cstIntoMapper.getListByWxOpenIdAndStatus(huCheckInRequest.getWxOpenId());
//        // 客户,产权人入住条件 1.微信号未绑定过房屋 2.并且是未入住状态
//        Integer intoRole = cstInto.getIntoRole();
//        // 根据角色更新入住状态为已入住 客户-0 与产权人-2
//        if(intoRole == Constant.INTO_ROLE_CST || intoRole == Constant.INTO_ROLE_PROPERTY_OWNER){
//            if(cstIntoByWxOpenId == null){
//                // 更新为已入住
//                CstInto cInto = new CstInto();
//                cInto.setId(huCheckInRequest.getCstIntoId());
//                cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
//                cInto.setUserName(huCheckInRequest.getUserName());
//                cInto.setIntoStatus(Constant.INTO_STATUS_Y);
//                cInto.setUpdateTime(new Date());
//                cstIntoMapper.update(cInto);
//                jsonObject.put("respCode", Constant.SUCCESS);
//                logger.info("---------------"+huCheckInRequest.getCstCode()+"客户或者产权人入住成功-----------------");
//            }
//          // 根据角色更新入住状态为待审核 委托人-1 与 住户-3
//        }else if(cstInto.getIntoRole() == Constant.INTO_ROLE_ENTRUST  || cstInto.getIntoRole() == Constant.INTO_ROLE_HOUSEHOLD){
//            // 1.第一次入住,微信号未查到数据,修改状态为待审核
//            if(cstIntoByWxOpenId == null){
//                updateIntoStatus(huCheckInRequest);
//                jsonObject.put("respCode", Constant.SUCCESS);
//            }else {
//                // 2.第二次入住
//                // 检验入住客户号是否是同一个
//                if(!cstIntoByWxOpenId.getCstCode().equals(huCheckInRequest.getCstCode())){
//                    jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//                    jsonObject.put("errDesc", "入住客户不是同一个!");
//                    return jsonObject;
//                }
//                // 检验入住的房间号，如果之前是未入住状态，将状态改为待审核
//                // a.根据入住编号查询将要入住的房间号
//                List<CstIntoHouse> cstIntoHouseListByCstIntoId = cstIntoHouseDaoMapper.getByCstIntoId(cstIntoId);
//                // b.根据微信号查询状态为已入住、待审核的房间
//                List<CstIntoHouse> cstIntoHouseListByWxOpenId = cstIntoHouseDaoMapper.getByWxOpenId(huCheckInRequest.getWxOpenId());
//                // c.筛选出需要入住的房间
//                List<CstIntoHouse> cstIntoHouseList = new ArrayList<>();
//                if(!cstIntoHouseListByCstIntoId.isEmpty()){
//                    for(CstIntoHouse cstIntoHouseByCstIntoId : cstIntoHouseListByCstIntoId){
//                        List<CstIntoHouse> cstIntoHouseListByWxOpenIdFilter = cstIntoHouseListByWxOpenId.stream().filter(cstIntoHouseByWxOpenId -> cstIntoHouseByCstIntoId.getHouseId().equals(cstIntoHouseByWxOpenId.getHouseId())).collect(Collectors.toList());
//                        if(cstIntoHouseListByWxOpenIdFilter.isEmpty()){
//                            cstIntoHouseList.add(cstIntoHouseByCstIntoId);
//                        }
//                    }
//                    // 给当前微信号插入新的房间，将状态修改为待审核
//                    if(!cstIntoHouseList.isEmpty()){
//                        for(CstIntoHouse cstIntoHouse : cstIntoHouseList){
//                            cstIntoHouse.setId(TimestampGenerator.generateSerialNumber());
//                            cstIntoHouse.setCstIntoId(cstIntoByWxOpenId.getId());
//                            cstIntoHouse.setIntoStatus(Constant.INTO_STATUS_A);
//                            cstIntoHouse.setCreateTime(new Date());
//                            cstIntoHouse.setUpdateTime(new Date());
//                        }
//                        cstIntoHouseDaoMapper.insertList(cstIntoHouseList);
//                    }
//                    // 删除未入住房间
//                    cstIntoHouseDaoMapper.deleteByCstIntoId(cstIntoId);
//                }
//                jsonObject.put("respCode", Constant.SUCCESS);
//            }
//
//            logger.info("---------------"+huCheckInRequest.getCstCode()+"委托人或者住户入住成功-----------------");
//        }else {
//            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
//            jsonObject.put("errDesc", "请勿重复绑定!");
//        }
//        return jsonObject;
//    }

}

package com.ej.hgj.controller.hu;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.base.BaseRespVo;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.controller.base.BaseController;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.config.ProConfDaoMapper;
import com.ej.hgj.dao.cst.HgjCstDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.hu.HgjHouseDaoMapper;
import com.ej.hgj.dao.hu.HuHgjBindMapper;
import com.ej.hgj.dao.wechat.WechatPubConfMapper;
import com.ej.hgj.dao.user.UsrConfMapper;
import com.ej.hgj.entity.bill.BillMerge;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.cst.CstPayPer;
import com.ej.hgj.entity.cst.HgjCst;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.HgjHouse;
import com.ej.hgj.entity.hu.MutipUserVo;
import com.ej.hgj.entity.ext.HuHgjBindExtDo;
import com.ej.hgj.entity.login.MutipUsrVo;
import com.ej.hgj.entity.user.UsrConfDO;
import com.ej.hgj.enums.JiabsvBasicRespCode;
import com.ej.hgj.enums.JiasvBasicRespCode;
import com.ej.hgj.enums.MonsterBasicRespCode;
import com.ej.hgj.request.hu.HuCheckInRequest;
import com.ej.hgj.request.hu.QueryManyUsrInfoRequest;
import com.ej.hgj.result.dto.HuHgjBindDto;
import com.ej.hgj.result.hu.QueryManyUsrInfoResult;
import com.ej.hgj.utils.GenerateUniqueIdUtil;
import com.ej.hgj.utils.WechatMiniProUtils;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.utils.exception.BusinessException;
import com.ej.hgj.vo.bill.BillRequestVo;
import com.ej.hgj.vo.bill.BillResponseVo;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.platform.commons.util.StringUtils.isNotBlank;


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

    /***
     * 业主房屋绑定
     */
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

    /**
     * 房主入住
     * @param huCheckInRequest
     * @return
     */
    @RequestMapping("/hu/houseBind/owner")
    @ResponseBody
    public JSONObject houseBindOwner(@RequestBody HuCheckInRequest huCheckInRequest) {
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
        // 根据微信号查询入住信息
        List<CstInto> cstIntoList = cstIntoMapper.getListByWxOpenId(huCheckInRequest.getWxOpenId());
        // 入住条件 1.微信号未绑定过房屋
        if(cstIntoList.isEmpty()){
            CstInto cInto = new CstInto();
            cInto.setId(TimestampGenerator.generateSerialNumber());
            cInto.setCstCode(huCheckInRequest.getCstCode());
            cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
            cInto.setUserName(huCheckInRequest.getUserName());
            cInto.setIntoRole(Constant.INTO_ROLE_OWNER);
            cInto.setIntoStatus(Constant.INTO_STATUS_Y);
            cInto.setCreateTime(new Date());
            cInto.setUpdateTime(new Date());
            cInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
            cstIntoMapper.save(cInto);
            jsonObject.put("respCode", Constant.SUCCESS);
            logger.info("---------------"+huCheckInRequest.getCstCode()+"房主入住成功-----------------");
        }else {
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "请勿重复绑定!");
        }
        return jsonObject;
    }

    /**
     * 租户入住
     * @param huCheckInRequest
     * @return
     */
    @RequestMapping("/hu/houseBind/tenant")
    @ResponseBody
    public JSONObject houseBindTenant(@RequestBody HuCheckInRequest huCheckInRequest) {
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
        if(StringUtils.isBlank(huCheckInRequest.getHouseId())){
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "房屋号为空!");
            return jsonObject;
        }
        // 根据微信号查询入住信息
        List<CstInto> intoList = cstIntoMapper.getListByWxOpenId(huCheckInRequest.getWxOpenId());
        if(intoList.isEmpty()){
            // 直接入住
            saveTenantInto(huCheckInRequest);
            jsonObject.put("respCode", Constant.SUCCESS);
        }else {
            // 查询微信号是否绑定过其他客户的房屋
            List<CstInto> cstIntoFilter_1 = intoList.stream().filter(cstInto_1 -> !cstInto_1.getCstCode().equals(huCheckInRequest.getCstCode())).collect(Collectors.toList());
            // 查询微信号是否绑定过该客户的房屋
            CstInto cstInto = new CstInto();
            cstInto.setHouseId(huCheckInRequest.getHouseId());
            cstInto.setWxOpenId(huCheckInRequest.getWxOpenId());
            List<CstInto> cstIntoList_2 = cstIntoMapper.getList(cstInto);
            // 查询微信号是否是该客户的业主
            List<CstInto> cstIntoFilter_3 = intoList.stream().filter(cstInto_2 -> cstInto_2.getCstCode().equals(huCheckInRequest.getCstCode()) && cstInto_2.getIntoRole() == Constant.INTO_ROLE_OWNER ).collect(Collectors.toList());
            // 入住条件 1.微信号未绑定过其他客户的房屋 2.微信号未绑定过该客户的房屋 3.微信号不是这个客户的业主
            if(cstIntoFilter_1.isEmpty() && cstIntoList_2.isEmpty() && cstIntoFilter_3.isEmpty()){
                saveTenantInto(huCheckInRequest);
                jsonObject.put("respCode", Constant.SUCCESS);
            }else {
                jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
                jsonObject.put("errDesc", "请勿重复绑定!");
            }
        }
        return jsonObject;
    }

    public void saveTenantInto(HuCheckInRequest huCheckInRequest){
        CstInto cInto = new CstInto();
        cInto.setId(TimestampGenerator.generateSerialNumber());
        cInto.setCstCode(huCheckInRequest.getCstCode());
        cInto.setHouseId(huCheckInRequest.getHouseId());
        cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
        cInto.setUserName(huCheckInRequest.getUserName());
        cInto.setIntoRole(Constant.INTO_ROLE_TENANT);
        cInto.setIntoStatus(Constant.INTO_STATUS_N);
        cInto.setCreateTime(new Date());
        cInto.setUpdateTime(new Date());
        cInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
        cstIntoMapper.save(cInto);
        logger.info("---------------"+huCheckInRequest.getCstCode()+"租户入住申请成功-----------------");
    }

    @ResponseBody
    @RequestMapping({"/queryMutipUsr","/queryMutipUsr.do"})
    public BaseRespVo queryMutip(@RequestBody MutipUsrVo mutipUsrVo) {
        logger.info("-----------------获取用户信息--------------------");
        MutipUserVo mutipUserVo = new MutipUserVo();
         String cstCode = mutipUsrVo.getCstCode();
        String code = mutipUsrVo.getCode();
        String isCHeck = "N";
        String wxOpenId = mutipUsrVo.getWxOpenId();
        String userName = "";
        String houseId = mutipUsrVo.getHouseId();
        String houseName = "";
        if (isBank(wxOpenId) && isBank(code)) {
            logger.info("用户信息缺失");
        }
        try {
            if (isNotBank(code) && isBank(wxOpenId)) {
                ConstantConfig constantConfig = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
                wxOpenId = WechatMiniProUtils.jscode2session(constantConfig.getAppId(), constantConfig.getAppSecret(), code).getOpenid();
                List<CstInto> cstIntoList = cstIntoMapper.getListByWxOpenId(wxOpenId);
                if(cstIntoList.isEmpty() && StringUtils.isBlank(cstCode)){
                    mutipUserVo.setErrCode("01012012");
                    mutipUserVo.setErrDesc("账户未开通");
                    return mutipUserVo;
                }

                // 1.客户编号为空或者客户编号不为空但是与入住的客户编号不一致时，都取入住的客户编号
                if(StringUtils.isBlank(cstCode) ||
                        (StringUtils.isNotBlank(cstCode) && !cstIntoList.isEmpty()
                                && !cstCode.equals(cstIntoList.get(0).getCstCode()))){
                    cstCode = cstIntoList.get(0).getCstCode();
                }
                logger.info("利用换取wxOpenId,code:" + code + "||wxOpenId:" + wxOpenId);
            }
            if(StringUtils.isBlank(wxOpenId)){
                mutipUserVo.setRespCode(MonsterBasicRespCode.WX_OPENID_FAILED.getReturnCode());
                mutipUserVo.setErrDesc(MonsterBasicRespCode.WX_OPENID_FAILED.getCodeDesc());
                return mutipUserVo;
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
            if(StringUtils.isNotBlank(houseId)){
                cstInto.setHouseId(houseId);
            }
            List<CstInto> cstIntoList = cstIntoMapper.getList(cstInto);
            if(!cstIntoList.isEmpty()){
                isCHeck = "Y";
                userName = cstIntoList.get(0).getUserName();
                houseId = cstIntoList.get(0).getHouseId();
            }

            // 获取房屋名称
            if(StringUtils.isNotBlank(houseId)){
                HgjHouse hgjHouse = hgjHouseDaoMapper.getById(houseId);
                houseName = hgjHouse.getResName();
            }
            // 获取项目名称
            mutipUserVo.setCstCode(cstCode);
            mutipUserVo.setCstName(cstName);
            mutipUserVo.setWxOpenId(wxOpenId);
            mutipUserVo.setProNum(proNum);
            mutipUserVo.setProName(proName);
            mutipUserVo.setIsCHeck(isCHeck);
            mutipUserVo.setUserName(userName);
            mutipUserVo.setHouseId(houseId);
            mutipUserVo.setHouseName(houseName);
            mutipUserVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
            return mutipUserVo;
        } catch (BusinessException e) {
            logger.info("获取用户信息失败");
            e.getMessage();
        } catch (Exception e) {
            logger.info("获取用户信息系统异常");
            e.getMessage();
        }
        return null;
    }

    /**
     * 房主对同住人操作，同意，拒绝，移除
     */
    @ResponseBody
    @RequestMapping("/cohabitOperate.do")
    public BillResponseVo cohabitOperate(@RequestBody HouseInfoVO houseInfoVO) {
        BillResponseVo billResponseVo = new BillResponseVo();
        String id = houseInfoVO.getId();
        // 同意
        if("agree".equals(houseInfoVO.getButtonType())){
            CstInto cstInto = new CstInto();
            cstInto.setId(id);
            cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
            cstInto.setUpdateTime(new Date());
            cstIntoMapper.update(cstInto);
        // 拒绝，移除
        }else if("refuse".equals(houseInfoVO.getButtonType()) || "remove".equals(houseInfoVO.getButtonType())){
            CstInto cstInto = new CstInto();
            cstInto.setId(id);
            cstInto.setDeleteFlag(Constant.DELETE_FLAG_YES);
            cstInto.setUpdateTime(new Date());
            cstIntoMapper.update(cstInto);
        }
        billResponseVo.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
        billResponseVo.setErrCode(JiasvBasicRespCode.SUCCESS.getRespCode());
        billResponseVo.setErrDesc(JiasvBasicRespCode.SUCCESS.getRespDesc());
        return billResponseVo;
    }
}
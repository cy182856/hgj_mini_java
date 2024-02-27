package com.ej.hgj.service.login;

import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.config.ConstantConfDaoMapper;
import com.ej.hgj.dao.login.LoginDaoMapper;
import com.ej.hgj.dao.user.UsrConfMapper;
import com.ej.hgj.entity.config.ConstantConfig;
import com.ej.hgj.entity.ext.HuHgjBindExtDo;
import com.ej.hgj.entity.login.*;
import com.ej.hgj.entity.user.UsrConfDO;
import com.ej.hgj.enums.*;
import com.ej.hgj.utils.WechatMiniProUtils;
import com.ej.hgj.utils.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class LoginServiceImpl implements LoginService{
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoginDaoMapper loginDaoMapper;

    @Autowired
    private UsrConfMapper usrConfMapper;

    @Autowired
    private ConstantConfDaoMapper constantConfDaoMapper;

    @Override
    public LoginInfo doLogin(String code, String custId, String wxSeqId, String openId, String huSeqId) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        HuLoginRequest huLoginRequest = new HuLoginRequest();
        if(StringUtils.isBlank(openId)){
            logger.info("用户首次登录或者做重登录操作");
            ConstantConfig constantConfig = constantConfDaoMapper.getByKey(Constant.MINI_PROGRAM_APP_EJ_ZHSQ);
            openId = WechatMiniProUtils.jscode2session(constantConfig.getAppId(), constantConfig.getAppSecret(), code).getOpenid();
        }
        huLoginRequest.setCustId(custId);
        huLoginRequest.setWxSeqId(wxSeqId);
        huLoginRequest.setOpenId(openId);
        huLoginRequest.setHuSeqId(huSeqId);

        HuLoginResult login = this.huLogin(huLoginRequest);
        if(null == login){
            logger.info("登录接口未响应");
            throw new BusinessException(MonsterBasicRespCode.SESSION_INVALID.getReturnCode(),
                    JiasvBasicRespCode.NETWORK_EXCEPTION.getRespCode(),JiasvBasicRespCode.NETWORK_EXCEPTION.getRespDesc());
        }
        if(!StringUtils.equals(login.getRespCode(), MonsterBasicRespCode.SUCCESS.getReturnCode())){
            logger.info("登录失败");
            throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                    login.getErrCode(),login.getErrDesc());
        }

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setHuRole(login.getHuRole());
        loginInfo.setHouseNo(login.getHouseNo());
        loginInfo.setRepairWorkTime(login.getRepairWorkTime());
        loginInfo.setUrgentTel(login.getUrgentTel());
        loginInfo.setHouseStat(login.getHouseStat());
        loginInfo.setScoreSum(login.getScoreSum());
        //loginInfo.setCustId(login.getCustId());
        loginInfo.setHgjOpenId(login.getHgjOpenId());
        loginInfo.setHuSeqId(login.getHuSeqId());
        loginInfo.setHouseSeqId(login.getHouseSeqId());
        loginInfo.setWxSeqId(login.getWxSeqId());
        loginInfo.setAuthBitMap(login.getAuthBitMap());
        loginInfo.setBuildingName(login.getBuildingName());
        loginInfo.setCommanyShortName(login.getCommanyShortName());
        loginInfo.setNickName(login.getNickName());
        loginInfo.setHeadImgUrl(login.getHeadImgUrl());
        loginInfo.setRepairTimeCnt(login.getRepairTimeCnt());
        loginInfo.setRespCode(login.getRespCode());
        loginInfo.setErrCode(login.getErrCode());
        loginInfo.setErrDesc(login.getErrDesc());
        loginInfo.setPropType(login.getPropType());
        loginInfo.setModuleBitmap(login.getModuleBitmap());
        loginInfo.setHuStat(login.getHuStat());
        loginInfo.setLoginErrDesc(login.getLoginErrDesc());
        loginInfo.setWkTimeTel(login.getWkTimeTel());
        loginInfo.setPoWxId(login.getPoWxId());
        loginInfo.setPoMp(login.getPoMp());
        loginInfo.setIsInfoPub(login.getIsInfoPub());
        //JSONArray funMenu = MainModuleEnum.getFunMenu(loginInfo.getAuthBitMap(), login.getPropType(),login.getHuStat(),false) ;

        loginInfo.setModuleValues(ModuleEnum.moduleValues(loginInfo.getAuthBitMap(),login.getHuStat()));
        String[] addr = getAddr(login.getAreaId(), login.getAreaName(), login.getBuildingName(), login.getHouseNo());
        loginInfo.setCompleteAddr(addr[0]);
        loginInfo.setHideAddr(addr[1]);
        loginInfo.setAreaName(addr[2]);
        loginInfo.setAreaId(login.getAreaId());
        loginInfo.setBuildingId(login.getBuildingId());
       // loginInfo.setFunList(funMenu);
        loginInfo.setPubOrgId(login.getPubOrgId());
        loginInfo.setQpadBitmapList(QpadEnum.getAuthBitMapList(login.getQpadBitmap()));
        loginInfo.setIsRepairTime(login.getIsRepairTime());
        loginInfo.setAdvCfeeMon(login.getAdvCfeeMon());
        return loginInfo;
    }

    private String[] getAddr(String areaId, String areaName, String buildingName, String houseNo) {
        String[] strings = new String[3];
        String comlete = "";
        String hide = "";
        String area = areaName;
        if (StringUtils.equals(areaId,"000")) {
            comlete = buildingName+houseNo;
            hide = buildingName + StringUtils.leftPad("*",houseNo.length()-1,"*");
            area = StringUtils.EMPTY;
        }else{
            comlete = areaName+buildingName+houseNo;
            hide = areaName + StringUtils.leftPad("*",buildingName.length()-1,"*") + StringUtils.leftPad("*",houseNo.length()-1,"*");
        }
        strings[0] = comlete;
        strings[1] = hide;
        strings[2] = area;
        return strings;
    }

    /**
     * 获取用户可操作的位图蓝本
     * @param moduleBitmap  企业配置的权限位图
     * @param authMaskBm 权限屏蔽位图 1代表需要进行屏蔽
     * @return
     */
    private String getModuleBitmap(String moduleBitmap,String authMaskBm){
        int min = Math.min(moduleBitmap.length(), authMaskBm.length());

        String bitmap = StringUtils.EMPTY;
        for (int i = 0; i < min; i++) {
            char c = moduleBitmap.charAt(i);
            char c2 = authMaskBm.charAt(i);
            if(c == '1' && c2 == '0') {
                bitmap = bitmap.concat("1");
            } else if(c == '2' && c2 == '0'){
                bitmap = bitmap.concat("2");
            }else{
                bitmap = bitmap.concat("0");
            }
        }
        return bitmap;
    }

    /**
     * 获取用户的最终的位图
     * @param authBitmap 个人权限位图
     * @param moduleBitmap  企业配置的权限位图
     * @param authMaskBm 权限屏蔽位图 1代表需要进行屏蔽
     * @return
     */
    private String getAuthBitmap(String authBitmap, String moduleBitmap,String authMaskBm){
        int min = Math.min(moduleBitmap.length(), authBitmap.length());

        String bitmap = StringUtils.EMPTY;
        for (int i = 0; i < min; i++) {
            char c = moduleBitmap.charAt(i);
            char c1 = authBitmap.charAt(i);
            char c2 = authMaskBm.charAt(i);
            if(c == '1' && c1 == '1' && c2 == '0'){
                bitmap = bitmap.concat("1");
            }else if(c == '2' && c1 == '1' && c2 == '0'){
                bitmap = bitmap.concat("2");
            }else{
                bitmap = bitmap.concat("0");
            }
        }
        return bitmap;
    }

    public HuLoginResult huLogin(HuLoginRequest huLoginRequest) {
        String openId = huLoginRequest.getOpenId();
        String wxSeqId = huLoginRequest.getWxSeqId();
        String custId = huLoginRequest.getCustId();
        String huSeqId = huLoginRequest.getHuSeqId();
        HuLoginResult huLoginResult = new HuLoginResult();
        if (StringUtils.isBlank(openId)) {
            logger.info("业主登录,关键信息缺失,登录失败");
            huLoginResult.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
            huLoginResult.setErrCode(JiabsvBasicRespCode.DATA_NULL.getRespCode());
            huLoginResult.setErrDesc(JiabsvBasicRespCode.DATA_NULL.getRespDesc());
            return huLoginResult;
        }

        try {
            HuHgjBindExtDo huHgjBindExtDo = this.queryFirstUser(openId, custId, wxSeqId, huSeqId);
            //获取权限信息
            String bitmap = getAuthBitmap(huHgjBindExtDo.getAuthBitmap(), huHgjBindExtDo.getModuleBitmap(),huHgjBindExtDo.getAuthMaskBm());
            //查询管家信息
            String poKeeperSeq = huHgjBindExtDo.getPoKeeperSeq();
            if(StringUtils.isNotBlank(poKeeperSeq)){
                logger.info("该用户配置了管家,查询用户的管家信息",poKeeperSeq);
                PropOperInfoDo propOperInfoDo = loginDaoMapper.selectByPrimaryKey(custId, poKeeperSeq);
                if (propOperInfoDo != null) {
                    logger.info("该用户的管家信息正常");
                    huLoginResult.setPoMp(propOperInfoDo.getPoMp());
                    huLoginResult.setPoWxId(propOperInfoDo.getPoWxId());
                    huLoginResult.setIsInfoPub(propOperInfoDo.getIsInfoPub());
                }
            }
            huLoginResult.setHuRole(huHgjBindExtDo.getHuRole());
            huLoginResult.setRepairWorkTime(huHgjBindExtDo.getRepairWorkTime());
            huLoginResult.setUrgentTel(huHgjBindExtDo.getUrgentTel());
            huLoginResult.setHouseNo(huHgjBindExtDo.getHouseNo());
            huLoginResult.setHouseStat(huHgjBindExtDo.getHouseStat());
            huLoginResult.setScoreSum(huHgjBindExtDo.getScoreSum());
            huLoginResult.setCustId(huHgjBindExtDo.getCustId());
            huLoginResult.setHgjOpenId(huHgjBindExtDo.getHgjOpenId());
            huLoginResult.setHuSeqId(huHgjBindExtDo.getHuSeqId());
            huLoginResult.setHouseSeqId(huHgjBindExtDo.getHouseSeqId());
            huLoginResult.setWxSeqId(huHgjBindExtDo.getWxSeqId());
            huLoginResult.setAuthBitMap(bitmap);
            huLoginResult.setBuildingName(huHgjBindExtDo.getBuildingName());
            huLoginResult.setAreaName(huHgjBindExtDo.getAreaName());
            huLoginResult.setCommanyShortName(huHgjBindExtDo.getCommanyShortName());
            huLoginResult.setNickName(huHgjBindExtDo.getNickName());
            huLoginResult.setHeadImgUrl(huHgjBindExtDo.getHeadImgUrl());
            huLoginResult.setRepairTimeCnt(huHgjBindExtDo.getRepairTimeCnt());
            huLoginResult.setPropType(huHgjBindExtDo.getPropType());
            huLoginResult.setModuleBitmap(getModuleBitmap(huHgjBindExtDo.getModuleBitmap(), huHgjBindExtDo.getAuthMaskBm()));
            huLoginResult.setHuStat(huHgjBindExtDo.getHuStat());
            huLoginResult.setLoginErrDesc(huHgjBindExtDo.getLoginErrDesc());
            huLoginResult.setWkTimeTel(huHgjBindExtDo.getWkTimeTel());
            huLoginResult.setBuildingId(huHgjBindExtDo.getBuildingId());
            huLoginResult.setAreaId(huHgjBindExtDo.getAreaId());
            huLoginResult.setPubOrgId(huHgjBindExtDo.getPubOrgId());
            huLoginResult.setQpadBitmap(huHgjBindExtDo.getQpadBitmap());
            huLoginResult.setIsRepairTime(huHgjBindExtDo.getIsRepairTime());
            huLoginResult.setAdvCfeeMon(huHgjBindExtDo.getAdvCfeeMon());
            huLoginResult.setRespCode(MonsterBasicRespCode.SUCCESS.getReturnCode());
            huLoginResult.setErrCode(JiabsvBasicRespCode.SUCCESS.getRespCode());
            huLoginResult.setErrDesc(JiabsvBasicRespCode.SUCCESS.getRespDesc());
        } catch (BusinessException e){
            huLoginResult.setRespCode(e.getRespCode());
            huLoginResult.setErrDesc(e.getErrDesc());
            huLoginResult.setErrDesc(e.getErrDesc());
        }catch (Exception e) {
            huLoginResult.setRespCode(MonsterBasicRespCode.RESULT_FAILED.getReturnCode());
            huLoginResult.setErrCode(JiabsvBasicRespCode.SYSTEM_EXCEPTION.getRespCode());
            huLoginResult.setErrDesc(JiabsvBasicRespCode.SYSTEM_EXCEPTION.getRespDesc());
        }
        return  huLoginResult;
    }

    public HuHgjBindExtDo queryFirstUser(String hgjOpenId,String custId,String wxSeqId,String huSeqId) {
        if (StringUtils.isBlank(hgjOpenId)) {
            logger.info("查询用户信息,关键信息缺失",hgjOpenId);
            throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                    JiabsvBasicRespCode.DATA_NULL.getRespCode(), JiabsvBasicRespCode.DATA_NULL.getRespDesc());
        }
        List<HuHgjBindExtDo> huHgjBindDoList = loginDaoMapper.queryFirstUser(hgjOpenId, custId, wxSeqId, huSeqId);
        if (CollectionUtils.isEmpty(huHgjBindDoList)) {
            logger.info("用户未绑定,未查询到用户信息,该用户无权限",hgjOpenId);
            String loginErrDesc = StringUtils.EMPTY;
            if(StringUtils.isNotBlank(custId)){
                UsrConfDO usrConfDO = usrConfMapper.selectByPrimaryKey(custId);
                loginErrDesc = usrConfDO.getLoginErrDesc();
            }
            throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                    JiabsvBasicRespCode.USER_NOT_EXIT.getRespCode(), StringUtils.isNotBlank(loginErrDesc)?loginErrDesc:JiabsvBasicRespCode.USER_NOT_EXIT.getRespDesc());
        }
        HuHgjBindExtDo huHgjBindDo = null;
        logger.info("该用户是同一个小区的多用户住户");
        for (HuHgjBindExtDo huHgjBindExtDo : huHgjBindDoList) {
            if(StringUtils.equals(huHgjBindExtDo.getStat(),StatEnum.N.getCode())
                    && !(StringUtils.equals(huHgjBindExtDo.getHuStat(),HouseUsrInfoStatEnum.C.getStat())
                    || StringUtils.equals(huHgjBindExtDo.getHuStat(),HouseUsrInfoStatEnum.D.getStat())
                    || StringUtils.equals(huHgjBindExtDo.getUsrStat(),StatEnum.C.getCode()))){
                logger.info("同小区多住户,取第一个正常的住户");
                huHgjBindDo = huHgjBindExtDo;
                break;
            }
        }
        if(null == huHgjBindDo){
            huHgjBindDo = huHgjBindDoList.get(0);
            if (StringUtils.equals(huHgjBindDo.getUsrStat(), StatEnum.C.getCode())) {
                logger.info( "该企业已经关闭了,不能进行登录");
                throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                        JiabsvBasicRespCode.CUST_CLOSE.getRespCode(), JiabsvBasicRespCode.CUST_CLOSE.getRespDesc());
            }else if(StringUtils.equals(huHgjBindDo.getHuStat(), HouseUsrInfoStatEnum.C.getStat())){
                logger.info("关闭状态时,需要根据住宅类型做不同的提醒");
                throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                        JiabsvBasicRespCode.USER_B_CLOSE.getRespCode(),
                        StringUtils.equals(huHgjBindDo.getPropType(), PropTypeEnum.R.getCode())?
                                JiabsvBasicRespCode.USER_R_CLOSE.getRespDesc():JiabsvBasicRespCode.USER_B_CLOSE.getRespDesc());
            }else if(StringUtils.equals(huHgjBindDo.getHuStat(),HouseUsrInfoStatEnum.D.getStat())){
                logger.info("用户已经注销了");
                throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                        JiabsvBasicRespCode.USER_NOT_BIND.getRespCode(), JiabsvBasicRespCode.USER_NOT_BIND.getRespDesc());
            }else{
                logger.info("指定企业登录,未找到正常账户信息");
                throw new BusinessException(MonsterBasicRespCode.RESULT_FAILED.getReturnCode(),
                        JiabsvBasicRespCode.CUST_CLOSE.getRespCode(), huHgjBindDo.getLoginErrDesc());
            }
        }

        return huHgjBindDo;
    }
}

package com.ej.hgj.service.hu;

import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.constant.Constant;
import com.ej.hgj.dao.hu.CstIntoCardMapper;
import com.ej.hgj.dao.hu.CstIntoHouseDaoMapper;
import com.ej.hgj.dao.hu.CstIntoMapper;
import com.ej.hgj.dao.role.RoleDaoMapper;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.hu.CstIntoHouse;
import com.ej.hgj.entity.role.Role;
import com.ej.hgj.request.hu.HuCheckInRequest;
import com.ej.hgj.service.role.RoleService;
import com.ej.hgj.utils.bill.TimestampGenerator;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.ej.hgj.vo.into.IntoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class HuServiceImpl implements HuService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CstIntoHouseDaoMapper cstIntoHouseDaoMapper;

    @Autowired
    private CstIntoMapper cstIntoMapper;

    @Autowired
    private CstIntoCardMapper cstIntoCardMapper;

    @Override
    public JSONObject updateIntoStatus(JSONObject jsonObject, HuCheckInRequest huCheckInRequest, CstInto cstInto) {
        // 根据微信号与入住状态 1-已入住(租户、产权人) 3-待审核(租户员工、租客、同住人)查出一条入住信息
        CstInto cstIntoByWxOpenId = cstIntoMapper.getByWxOpenIdAndStatus_1_3(huCheckInRequest.getWxOpenId());
        // 入住角色
        Integer intoRole = cstInto.getIntoRole();
        if(cstIntoByWxOpenId == null){
            // 根据角色更新入住状态为已入住 0-租户 2-与产权人
            if(intoRole == Constant.INTO_ROLE_CST || intoRole == Constant.INTO_ROLE_PROPERTY_OWNER){
                // 更新为已入住
                CstInto cInto = new CstInto();
                cInto.setId(huCheckInRequest.getCstIntoId());
                cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
                cInto.setUserName(huCheckInRequest.getUserName());
                cInto.setPhone(huCheckInRequest.getPhone());
                cInto.setIntoStatus(Constant.INTO_STATUS_Y);
                cInto.setUpdateTime(new Date());
                cstIntoMapper.update(cInto);
                jsonObject.put("respCode", Constant.SUCCESS);
                logger.info("---------------"+huCheckInRequest.getCstCode()+"租户、产权人入住成功-----------------");
                // 根据角色更新入住状态为待审核 1-租户员工 3-租客 4-同住人
            }else if(cstInto.getIntoRole() == Constant.INTO_ROLE_ENTRUST  || cstInto.getIntoRole() == Constant.INTO_ROLE_HOUSEHOLD || intoRole == Constant.INTO_ROLE_COHABIT){
                // 更新用户入住状态为待审核
                CstInto cInto = new CstInto();
                cInto.setId(huCheckInRequest.getCstIntoId());
                cInto.setWxOpenId(huCheckInRequest.getWxOpenId());
                cInto.setUserName(huCheckInRequest.getUserName());
                cInto.setPhone(huCheckInRequest.getPhone());
                cInto.setIntoStatus(Constant.INTO_STATUS_A);
                cInto.setUpdateTime(new Date());
                cstIntoMapper.update(cInto);
                // 同时更新入住房间表
                CstIntoHouse cstIntoHouse = new CstIntoHouse();
                cstIntoHouse.setCstIntoId(huCheckInRequest.getCstIntoId());
                cstIntoHouse.setIntoStatus(Constant.INTO_STATUS_A);
                cstIntoHouse.setUpdateTime(new Date());
                cstIntoHouseDaoMapper.updateByCstIntoId(cstIntoHouse);
                jsonObject.put("respCode", Constant.SUCCESS);
                logger.info("---------------"+huCheckInRequest.getCstCode()+"租户员工、租客、同住人入住成功-----------------");
            }
        }else {
            jsonObject.put("respCode", Constant.FAIL_RESULT_CODE);
            jsonObject.put("errDesc", "请勿重复绑定!");
        }

        return jsonObject;
    }

    @Override
    public void updateStatus(HouseInfoVO houseInfoVO) {
        String proNum = houseInfoVO.getProNum();
        String tenantWxOpenId = houseInfoVO.getTenantWxOpenId();
        String cstCode = houseInfoVO.getCstCode();
        // 同意
        if("agree".equals(houseInfoVO.getButtonType())){
            CstInto cstInto = new CstInto();
            cstInto.setId(houseInfoVO.getId());
            cstInto.setIntoStatus(Constant.INTO_STATUS_Y);
            cstInto.setUpdateTime(new Date());
            cstIntoMapper.update(cstInto);

            CstIntoHouse cstIntoHouse = new CstIntoHouse();
            cstIntoHouse.setId(houseInfoVO.getCstIntoHouseId());
            cstIntoHouse.setIntoStatus(Constant.INTO_STATUS_Y);
            cstIntoHouse.setUpdateTime(new Date());
            cstIntoHouseDaoMapper.updateById(cstIntoHouse);

            // 拒绝，移除
        }else if("refuse".equals(houseInfoVO.getButtonType()) || "remove".equals(houseInfoVO.getButtonType())){
            CstIntoHouse cstIntoHouse = new CstIntoHouse();
            cstIntoHouse.setId(houseInfoVO.getCstIntoHouseId());
            cstIntoHouse.setIntoStatus(Constant.INTO_STATUS_U);
            cstIntoHouse.setUpdateTime(new Date());
            cstIntoHouseDaoMapper.updateById(cstIntoHouse);

            // 如果租户员工、租客、亲属绑定房间被全部解除，入住表也解除
            CstInto cs = cstIntoMapper.getById(houseInfoVO.getId());
            List<CstIntoHouse> cstIntoHouseList = cstIntoHouseDaoMapper.getByCstIntoIdAndIntoStatus(houseInfoVO.getId());
            if(cstIntoHouseList.isEmpty() && cs != null && (cs.getIntoRole() == Constant.INTO_ROLE_ENTRUST || cs.getIntoRole() == Constant.INTO_ROLE_HOUSEHOLD) || cs.getIntoRole() == Constant.INTO_ROLE_COHABIT){
                CstInto cstInto = new CstInto();
                cstInto.setId(houseInfoVO.getId());
                cstInto.setIntoStatus(Constant.INTO_STATUS_U);
                cstInto.setUpdateTime(new Date());
                cstIntoMapper.update(cstInto);
            }

            // 移除卡权限
            cstIntoCardMapper.deleteCardPerm(proNum,cstCode,tenantWxOpenId);
        }
    }

    @Override
    public String saveCstIntoInfo(IntoVo intoVo) {
        // 根据入住类型预生成入住信息
        String intoType = intoVo.getIntoType();
        String[] resIds = intoVo.getResId();
        CstInto cstInto = new CstInto();
        String cstIntoId = TimestampGenerator.generateSerialNumber();
        cstInto.setId(cstIntoId);
        cstInto.setProjectNum(intoVo.getOrgId());
        cstInto.setCstCode(intoVo.getCstCode());
        cstInto.setIntoRole(Integer.valueOf(intoType));
        cstInto.setIntoStatus(Constant.INTO_STATUS_N);
        cstInto.setCreateTime(new Date());
        cstInto.setUpdateTime(new Date());
        cstInto.setDeleteFlag(Constant.DELETE_FLAG_NOT);
        cstIntoMapper.save(cstInto);
        // 如果是租户员工、租客、亲属，需要保存房间号
        if("1".equals(intoType) || "3".equals(intoType) || "4".equals(intoType)){
            List<CstIntoHouse> cstIntoHouseList = new ArrayList<>();
            for(int i = 0; i<resIds.length; i++){
                CstIntoHouse cstIntoHouse = new CstIntoHouse();
                cstIntoHouse.setId(TimestampGenerator.generateSerialNumber());
                cstIntoHouse.setCstIntoId(cstIntoId);
                cstIntoHouse.setHouseId(resIds[i]);
                cstIntoHouse.setIntoStatus(Constant.INTO_STATUS_N);
                cstIntoHouse.setCreateTime(new Date());
                cstIntoHouse.setUpdateTime(new Date());
                cstIntoHouse.setDeleteFlag(Constant.DELETE_FLAG_NOT);
                cstIntoHouseList.add(cstIntoHouse);
            }
            cstIntoHouseDaoMapper.insertList(cstIntoHouseList);
        }

        return cstIntoId;
    }
}

package com.ej.hgj.service.hu;



import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.role.Role;
import com.ej.hgj.request.hu.HuCheckInRequest;
import com.ej.hgj.vo.hu.HouseInfoVO;
import com.ej.hgj.vo.into.IntoVo;

import java.util.List;

public interface HuService {

    JSONObject updateIntoStatus(JSONObject jsonObject, HuCheckInRequest huCheckInRequest, CstInto cstInto);

    void updateStatus(HouseInfoVO houseInfoVO);

    String saveCstIntoInfo(IntoVo intoVo);

}

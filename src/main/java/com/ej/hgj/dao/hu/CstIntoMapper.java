package com.ej.hgj.dao.hu;

import com.ej.hgj.entity.ext.HuHgjBindExtDo;
import com.ej.hgj.entity.hu.CstInto;
import com.ej.hgj.entity.user.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CstIntoMapper {

    CstInto getById(String id);

    List<CstInto> getByCstCode(String cstCode);

    CstInto queryPersonData(String wxOpenId, String cstCode, String proNum);

    CstInto getByWxOpenIdAndStatus_1_3(String wxOpenId);

    CstInto getByWxOpenIdAndStatus_1(String wxOpenId);

    List<CstInto> getList(CstInto cstInto);

    List<CstInto> getListByHouseId(String houseId);

    List<CstInto> getByCstCodeAndIntoRole(String cstCode);

    void save(CstInto cstInto);

    void update(CstInto cstInto);

    void delete(String id);
}

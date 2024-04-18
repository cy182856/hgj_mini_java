package com.ej.hgj.dao.hu;

import com.ej.hgj.entity.hu.CstIntoHouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CstIntoHouseDaoMapper {


    List<CstIntoHouse> getList(CstIntoHouse cstIntoHouse);

    void delete(String id);

    void deleteByCstIntoId(String cstIntoId);

    void save(CstIntoHouse cstIntoHouse);

    void deleteByCstCodeAndWxOpenId(String cstCode, String wxOpenId);

    CstIntoHouse getById(String id);

    List<CstIntoHouse> getByCstIntoId(String cstIntoId);

    List<CstIntoHouse> getByCstIntoIdAndIntoStatus(String cstIntoId);

    List<CstIntoHouse> getByWxOpenId(String wxOpenId);

    List<CstIntoHouse> getByCstCodeAndWxOpenId(String cstCode, String wxOpenId);

    void insertList(@Param("list") List<CstIntoHouse> cstIntoHouseList);

    void updateByCstIntoId(CstIntoHouse cstIntoHouse);

    void updateById(CstIntoHouse cstIntoHouse);


}

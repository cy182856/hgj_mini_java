package com.ej.hgj.dao.cst;

import com.ej.hgj.entity.cst.CstPayPer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CstPayPerDaoMapper {

    List<CstPayPer> findByCstCode(String cstCode);



}

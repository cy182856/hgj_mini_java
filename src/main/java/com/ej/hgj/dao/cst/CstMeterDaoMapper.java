package com.ej.hgj.dao.cst;

import com.ej.hgj.entity.cst.CstMeter;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CstMeterDaoMapper {

    List<CstMeter> getList(CstMeter cstMeter);


}

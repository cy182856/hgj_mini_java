package com.ej.hgj.sy.dao.house;

import com.ej.hgj.entity.hu.HgjHouse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SyHouseDaoMapper {

    List<HgjHouse> getListByCstCode(HgjHouse hgjHouse);

}

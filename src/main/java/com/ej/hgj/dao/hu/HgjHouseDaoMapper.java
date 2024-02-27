package com.ej.hgj.dao.hu;

import com.ej.hgj.entity.hu.HgjHouse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface HgjHouseDaoMapper {

    HgjHouse getById(String id);

    List<HgjHouse> getList(HgjHouse hgjHouse);

    List<HgjHouse> getListByCstCode(HgjHouse hgjHouse);


}

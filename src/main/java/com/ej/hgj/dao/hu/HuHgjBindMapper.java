package com.ej.hgj.dao.hu;

import com.ej.hgj.entity.ext.HuHgjBindExtDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
@Mapper
@Component
public interface HuHgjBindMapper {

    List<HuHgjBindExtDo> queryBindUsrByOpenId(@Param("hgjOpenId") String hgjOpenId,@Param("custId")String custId);
}
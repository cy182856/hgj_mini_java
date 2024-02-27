package com.ej.hgj.dao.login;

import com.ej.hgj.entity.ext.HuHgjBindExtDo;
import com.ej.hgj.entity.login.PropOperInfoDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
@Mapper
@Component
public interface LoginDaoMapper {

    List<HuHgjBindExtDo> queryFirstUser(@Param("hgjOpenId") String hgjOpenId, @Param("custId") String custId,
                                        @Param("wxSeqId") String wxSeqId, @Param("huSeqId") String huSeqId);

    PropOperInfoDo selectByPrimaryKey(@Param("custId") String custId, @Param("poSeqId") String poSeqId);

}

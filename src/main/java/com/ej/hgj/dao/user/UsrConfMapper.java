package com.ej.hgj.dao.user;

import com.ej.hgj.entity.user.UsrConfDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UsrConfMapper {

    UsrConfDO selectByPrimaryKey(String custId);

}
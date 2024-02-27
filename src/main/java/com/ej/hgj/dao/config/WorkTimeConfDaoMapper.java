package com.ej.hgj.dao.config;

import com.ej.hgj.entity.config.ProConfig;
import com.ej.hgj.entity.config.WorkTimeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface WorkTimeConfDaoMapper {

    WorkTimeConfig getWorkTime();


}

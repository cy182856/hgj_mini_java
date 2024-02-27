package com.ej.hgj.dao.config;

import com.ej.hgj.entity.config.RepairConfig;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface RepairConfDaoMapper {

    RepairConfig getById(String id);

    List<RepairConfig> getByProjectNum(String id);

    List<RepairConfig> getList(RepairConfig repairConfig);

}

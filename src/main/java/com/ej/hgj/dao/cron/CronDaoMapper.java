package com.ej.hgj.dao.cron;

import com.ej.hgj.entity.cron.Cron;
import com.ej.hgj.entity.role.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CronDaoMapper {

    Cron getByType(String type);

}

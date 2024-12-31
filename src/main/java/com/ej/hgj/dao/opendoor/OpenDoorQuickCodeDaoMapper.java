package com.ej.hgj.dao.opendoor;

import com.ej.hgj.entity.opendoor.OpenDoorQuickCode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface OpenDoorQuickCodeDaoMapper {

    void save(OpenDoorQuickCode openDoorQuickCode);

    List<OpenDoorQuickCode> getListByDay(String wxOpenId);

}
